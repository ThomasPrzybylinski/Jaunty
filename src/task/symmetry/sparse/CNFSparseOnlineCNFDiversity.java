package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import formula.simple.DNF;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import util.formula.FormulaForAgreement;
import util.lit.LitUtil;
import util.lit.SymBreaker;

public class CNFSparseOnlineCNFDiversity {
	private long implicantTimeout = 0;// 0;//Integer.MAX_VALUE;// 000;
	private int maxClBrk = Integer.MAX_VALUE;
	private long timeOut = Long.MAX_VALUE;
	private int maxSize = Integer.MAX_VALUE;
	private int maxSyms = Integer.MAX_VALUE;

	private CNF cnf;

	private FormulaForAgreement form;
	private SparseModelMapper globMapper;
	private SymBreaker breaker = new SymBreaker();
	private boolean useLocalSymBreak = true;
	private boolean useGlobalSymBreak = true;
	public boolean forceGlobBreakCl = false;
	private boolean testGlob = true;
	private boolean testLocal = true;
	private boolean printProgress = false;
	private boolean breakFast = false;
	private boolean randPhase = true;
	private boolean lexBreaking = true;
	private boolean rebuild = false;

	private int noFindDelay = 10;
	private int localBrkDelay = 10;
	private int numCandidates = 0;
	private int numTests = 0;
	private int numImplTimeout = 0;
	private long totalSolverTime = 0;
	private long totalSymTime = 0;
	private long totalTime = 0;
	private int seed = -2;
	

	private int numVars;

	List<EventStats> stats;

	public static class EventStats {
		long millis;
		int numAdded;
		int numTested;
		int symBreakClausesAdded;
		long numLitsAdded;

		public EventStats(long millis, int numAdded, int numTested,
				int symBreakClausesAdded, long numLitsAdded) {
			super();
			this.millis = millis;
			this.numAdded = numAdded;
			this.numTested = numTested;
			this.symBreakClausesAdded = symBreakClausesAdded;
			this.numLitsAdded = numLitsAdded;
		}

		public long getMillis() {
			return millis;
		}

		public int getNumAdded() {
			return numAdded;
		}

		public int getNumTested() {
			return numTested;
		}

		public int getSymBreakClausesAdded() {
			return symBreakClausesAdded;
		}

		public long getNumLitsAdded() {
			return numLitsAdded;
		}

		public void setMillis(long millis) {
			this.millis = millis;
		}

		public void setNumAdded(int numAdded) {
			this.numAdded = numAdded;
		}

		public void setNumTested(int numTested) {
			this.numTested = numTested;
		}

		public void setSymBreakClausesAdded(int symBreakClausesAdded) {
			this.symBreakClausesAdded = symBreakClausesAdded;
		}

		public void setNumLitsAdded(long numLitsAdded) {
			this.numLitsAdded = numLitsAdded;
		}

	}

	public CNFSparseOnlineCNFDiversity(CNF cnf) {
		this.cnf = cnf;
	}

	public CNFSparseOnlineCNFDiversity(CNF cnf, int maxSims) {
		this(cnf);
		this.maxSyms = maxSims;
	}

	public List<int[]> getDiverseSet() throws ContradictionException,
	TimeoutException {
		return getDiverseSet(null, null);
	}

	public List<int[]> getDiverseSet(Random rand, List<int[]> models)
			throws ContradictionException, TimeoutException {
		stats = new LinkedList<EventStats>();
		int origVars = cnf.getContext().size();
		numVars = origVars;
		totalSolverTime = 0;
		totalSymTime = 0;
		long start = System.currentTimeMillis();

		if (testGlob) {
			globMapper = new SparseModelMapper(cnf);
		}

		form = new FormulaForAgreement(cnf);
		ArrayList<int[]> modelRej = new ArrayList<int[]>();

		CNF fullRep = cnf;
		if (rebuild) {
			fullRep = cnf.getCopy();
			// To not mess up FormulaForAgreement's calculations
			VariableContext newContext = new VariableContext();
			newContext.ensureSize(fullRep.getContext().size());
			fullRep.setContext(newContext);
		}

		ArrayList<int[]> curModels = new ArrayList<int[]>();

		ISolver solver = getSolver(cnf);

		SparseSymFinder finder;
		LiteralGroup globalGroup;
		if (useGlobalSymBreak) {
			addGlobalSymBreak(solver, fullRep);
			if (rebuild) {
				solver.reset();
				solver = getSolver(fullRep);
			}
		}

		long solveStart = System.nanoTime();
		int[] firstModel = getTrueModel(solver.findModel(), origVars);
		totalSolverTime += (System.nanoTime() - solveStart);

		if (firstModel == null) {
			System.out.println("Unsatisfiable Theory");
			System.exit(0);
		}
		
		if(printProgress) {
			tempPrintFunc(firstModel);
		}
		
		curModels.add(firstModel);
		stats.add(new EventStats(System.currentTimeMillis() - start, 1, 1, 0, 0));

		int[] rejectFirstModel = getRejection(firstModel);
		if (rebuild) {
			modelRej.add(rejectFirstModel);
//			fullRep.fastAddClause(rejectFirstModel);
		}
		solver.addClause(new VecInt(rejectFirstModel));

		// solver.clearLearntClauses();

		int[] nextModel = null;

		numCandidates++;
		int lastFound = 0;
		long prevSize = 0;
		if(rebuild) {
			prevSize = fullRep.getClauses().size();
		}
		while (true) {
			if ((System.currentTimeMillis() - start) > timeOut) {
				break;
			}
			lastFound++;
			if (printProgress) {
				System.out.println(curModels.size() + "/" + numCandidates);
			}

			if (rebuild) {
				if(useGlobalSymBreak || useLocalSymBreak) {
					System.out.println("C:" + fullRep.getClauses().size());
					if(fullRep.getClauses().size() > 400000 && fullRep.getClauses().size() >= 1.5*prevSize) {
						if(printProgress) {
							System.out.println("B:" + fullRep.getDeepSize());
							System.out.println("C:" + fullRep.getClauses().size());
						}
						fullRep.sort();
						prevSize = fullRep.getClauses().size();
						FormulaForAgreement toSum = new FormulaForAgreement(fullRep);
						fullRep = toSum.doSubsumption();
						
						if(printProgress) {
							System.out.println("B:" + fullRep.getDeepSize());
							System.out.println("C:" + fullRep.getClauses().size());
						}
						solver.reset();
						solver = getSolver(fullRep);
						for(int[] i : modelRej) {
							solver.addClause(new VecInt(i));
						}
					}
				}
				
			}

			solveStart = System.nanoTime();
			nextModel = solver.findModel();
			totalSolverTime += (System.nanoTime() - solveStart);

			if (nextModel == null)
				break;
			if ((System.currentTimeMillis() - start) > timeOut) {
				break;
			}

			nextModel = getTrueModel(nextModel, origVars);
			numCandidates++;

			if(printProgress) {
				tempPrintFunc(nextModel);
			}
			
			boolean add = true;

			int[] rejectModel = getRejection(nextModel);

			if (rebuild) {
//				fullRep.fastAddClause(rejectModel);
				modelRej.add(rejectModel);
			}
			solver.addClause(new VecInt(rejectModel));

			for (int k = curModels.size() - 1; k >= 0; k--) {
				if ((System.currentTimeMillis() - start) > timeOut) {
					break;
				}

				boolean breaking = (useLocalSymBreak) && (lastFound > localBrkDelay);

				int[] oldModel = curModels.get(k);

				int[] agree = LitUtil.getAgreement(oldModel, nextModel);
				// if(printProgress) System.out.println("Getting Rep");
				ClauseList rep = getRepFromAgreement(cnf, agree);
				if(printProgress) {
					tempPrintFunc(rep);
				}

				if (rep == FormulaForAgreement.EMPTY || rep.size() <= 2) {
					// Can be empty if only two models
					add = false;
				} else {
					long symStart = System.nanoTime();
					LiteralPermutation rejPerm = null;
					if (add) {
						// if(printProgress)
						// System.out.println("Processing Symmetry");
						rejPerm = processSymmetry(oldModel, nextModel, rep, agree);
						add &= (rejPerm == null);
						
						if(printProgress && rejPerm != null) {
							System.out.println(rejPerm);
							tempPrintFunc(rejPerm.asArray());
							
						}
						
//						if(rejPerm != null && (new ModelComparator()).compare(oldModel,nextModel) > 0) {
//							//Will only happen once
//							curModels.set(k,nextModel);
//						}
					}

					// if(printProgress) System.out.println("Breaking Symmetry");
					doSymBreaking(solver, fullRep, agree, rep, rejPerm, breaking);
					totalSymTime += (System.nanoTime() - symStart);
				}



				if (!add && (!breaking || breakFast))
					break;

				// if(!add) {// && solver.nConstraints() <= 10*origCNF.size()) {
				// break;
				// }
			}
			if ((System.currentTimeMillis() - start) > timeOut) {
				numCandidates--;
				break;
			}

			if (add) {
				// if(printProgress) System.out.println("Adding");
				lastFound = 0;
				curModels.add(nextModel);
				stats.add(new EventStats(System.currentTimeMillis() - start,
						curModels.size(), numCandidates, breaker
						.getNumClausesAdded(), breaker
						.getNumLitsAdded()));
				// System.out.println(curModels.size());
				if (curModels.size() == maxSize)
					break;
			} else if(noFindDelay <= 0 || (lastFound > noFindDelay && lastFound%noFindDelay == 0)) {
				stats.add(new EventStats(System.currentTimeMillis() - start,
						curModels.size(), numCandidates, breaker
						.getNumClausesAdded(), breaker
						.getNumLitsAdded()));
			}
		}
		solver.reset();

		totalTime = System.currentTimeMillis() - start;

		// if((System.currentTimeMillis() - start) > timeOut) {
		// stats.add(new
		// EventStats(System.currentTimeMillis()-start,curModels.size(),numChecked,
		// breaker.getNumClausesAdded(), breaker.getNumLitsAdded()));
		// }

		return curModels;
	}

	private void tempPrintFunc(ClauseList rep) {
		VariableContext other = tempCol();
		rep.setContext(other);
		System.out.println(rep);
	}
	
	private void tempPrintFunc(int[] mod) {
		boolean perm = false;
		if(mod[0] == 0) { //perm
			perm = true;
			mod[0] = mod[1];
		}
		VariableContext other = tempCol();
		ClauseList cl = new ClauseList(other);
		cl.addClause(mod);
		System.out.println(cl);
		
		if(perm) {
			mod[0] = 0;
		}
	}

	private VariableContext tempCol() {
		VariableContext other = new VariableContext();
		for(int k = 1; k <=18; k++) {
			char col = ' ';
			if(k%3 == 1) {
				col = 'R';
			} if(k%3 == 2) {
				col = 'G';
			} if(k%3 == 0) {
				col = 'B';
			}
			other.getOrCreateVar(col + "" + (k-1)/3);
		}
		return other;
	}

	public List<int[]> removeInvalid(List<int[]> curModels) {
		numVars = curModels.get(0).length;
		totalSolverTime = 0;
		totalSymTime = 0;

		if (testGlob) {
			globMapper = new SparseModelMapper(cnf);
		}

		form = new FormulaForAgreement(cnf);

		List<int[]> ret = new ArrayList<int[]>(curModels.size());
		ret.add(curModels.get(0));
		for(int i = 1; i < curModels.size(); i++) {
			int[] nextModel = curModels.get(i);
			boolean add= true;
			for (int k = ret.size()-1; k >= 0; k--) {
				int[] oldModel = ret.get(k);

				int[] agree = LitUtil.getAgreement(oldModel, nextModel);
				// if(printProgress) System.out.println("Getting Rep");
				ClauseList rep = getRepFromAgreement(cnf, agree);

				if (rep == FormulaForAgreement.EMPTY || rep.size() <= 2) {
					// Can be empty if only two models
					add = false;
					continue;
				}

				long symStart = System.nanoTime();
				LiteralPermutation rejPerm = null;
				if (add) {
					// if(printProgress)
					// System.out.println("Processing Symmetry");
					rejPerm = processSymmetry(oldModel, nextModel, rep, agree);
					add &= (rejPerm == null);
				}
				totalSymTime += (System.nanoTime() - symStart);

				if (!add && (!useLocalSymBreak || breakFast))
					break;
			}
			if (add) {
				ret.add(nextModel);
			}
		}
		return ret;

	}

	private void doSymBreaking(ISolver solver, ClauseList rep, int[] agree,
			ClauseList reducedCNF, LiteralPermutation rejPerm, boolean breaking)
					throws ContradictionException {
		ArrayList<int[]> all = new ArrayList<int[]>();
		SparseSymFinder finder;

		if (useLocalSymBreak || (globalRejection && useGlobalSymBreak)) {
			if (rejPerm != null) {
				if (globalRejection || forceGlobBreakCl) {
					if (lexBreaking) {
						all.addAll(breaker.getFullBreakingClauseForPerm(
								new int[] {}, rejPerm, maxClBrk));
					} else {
						all.addAll(breaker.getAltBreakingClauseForPerm(
								new int[] {}, rejPerm));
					}
				} else {
					if (lexBreaking) {
						all.addAll(breaker.getFullBreakingClauseForPerm(agree,
								rejPerm, maxClBrk));
					} else {
						all.addAll(breaker.getAltBreakingClauseForPerm(agree,
								rejPerm));
					}
					// // finder.addKnownSubgroup(new
					// NaiveLiteralGroup(rejPerm));
				}
			}

			if(breaking) {
				//Do slower sym breaking stuff
				finder = new SparseSymFinder(reducedCNF);
				finder.setMaxSyms(maxSyms);
				LiteralGroup lg = finder.getSymGroup();

				lg = stripUseless(lg, numVars);

				// LiteralGroup lg = useJBliss(reducedCNF);
				if (forceGlobBreakCl) {
					if (lexBreaking) {
						all.addAll(breaker.getFullSymBreakClauses(lg, new int[] {},
								maxClBrk));
					} else {
						all.addAll(breaker.getAlternateSymBreakClauses(lg,
								new int[] {}));
					}
				} else {

					if (lexBreaking) {
						all.addAll(breaker.getFullSymBreakClauses(lg, agree,
								maxClBrk));
					} else {
						all.addAll(breaker.getAlternateSymBreakClauses(lg, agree));
					}
				}
			}
		}

		if (rebuild) {
			rep.fastAddAll(all);
			int diff = rep.getContext().size() - solver.nVars();
			if(diff > 0) {
				solver.newVar(rep.getContext().size());
			}
		}


		for (int[] cl : all) {
			solver.addClause(new VecInt(cl));
		}

	}

	private LiteralGroup stripUseless(LiteralGroup lg, int numOrigVars) {
		ArrayList<LiteralPermutation> newGens = new ArrayList<LiteralPermutation>(
				lg.getGenerators().size());

		for (LiteralPermutation lp : lg.getGenerators()) {
			newGens.add(stripUseless(lp, numOrigVars));
		}

		return new NaiveLiteralGroup(newGens);
	}

	private LiteralPermutation stripUseless(LiteralPermutation lp,
			int numOrigVars) {
		if (lp.size() == numOrigVars)
			return lp;

		int[] newArray = new int[numOrigVars + 1];
		System.arraycopy(lp.asArray(), 0, newArray, 0, newArray.length);
		return new LiteralPermutation(newArray);
	}

	private void addGlobalSymBreak(ISolver solver, ClauseList rep)
			throws ContradictionException {
		SparseSymFinder finder;
		LiteralGroup globalGroup;
		long symStart = System.nanoTime();
		finder = new SparseSymFinder(cnf);
		finder.setMaxSyms(maxSyms);
		globalGroup = finder.getSymGroup();
		ArrayList<int[]> all = new ArrayList<int[]>();
		// LiteralGroup globalGroup2 = useJBliss(origCNF);
		// globalGroup = globalGroup2;

		// LocalSymClauses cl = new LocalSymClauses(origCNF,false);
		// LiteralGroup m1 = cl.getModelGroup(globalGroup);
		// LiteralGroup m2 = cl.getModelGroup(globalGroup2);

		if (lexBreaking) {
			all.addAll(breaker
					.getFullSymBreakClauses(globalGroup, new int[] {}, maxClBrk));
		} else {
			all.addAll(breaker.getAlternateSymBreakClauses(globalGroup,
					new int[] {}));
		}

		totalSymTime += (System.nanoTime() - symStart);

		if (!useGlobalSymBreak) {
			globalGroup = null;
			finder = null;
		}

		if (rebuild) {
			rep.fastAddAll(all);
			int diff = rep.getContext().size() - solver.nVars();
			if(diff > 0) {
				solver.newVar(rep.getContext().size());
			}
		}
		for (int[] cl : all) {
			solver.addClause(new VecInt(cl));
		}

	}

	@SuppressWarnings("rawtypes")
	private RandomPhase thePhase = null;
	private ISolver getSolver(CNF toSolve) throws ContradictionException {
		ISolver solver = toSolve.getSolverForCNFEnsureVariableUIDsMatch();

//		if (!rebuild && (useGlobalSymBreak || useLocalSymBreak) && lexBreaking) {
//			solver.newVar(numVars + numVars * numVars); // to
//		}

		if (randPhase) {
			if(thePhase == null) thePhase = new RandomPhase(seed);
			ICDCL engine = ((ICDCL) solver.getSolvingEngine());
			engine.getOrder().setPhaseSelectionStrategy(thePhase); // );//
			// engine.getOrder()));
		}
		boolean keepHot = false;//DO NOT CHANGE!
		solver.setKeepSolverHot(keepHot);
		return solver;
	}

	private boolean globalRejection = false;

	private LiteralPermutation processSymmetry(int[] oldModel, int[] nextModel,
			ClauseList reducedCNF, int[] agreement) {

		boolean similar = true; // returns !similar
		globalRejection = false;

		LiteralPermutation rej = null;

		if (testGlob) {
			similar = globMapper.canMap(oldModel, nextModel);

			if (similar) {
				globalRejection = true;
				rej = globMapper.getFoundPerm();
			}
		}

		if (testLocal && !similar) {
			int[] oldModelNoAg = removeAgreement(oldModel, agreement);
			int[] newModelNoAg = removeAgreement(nextModel, agreement);

			SparseModelMapper mapper = new SparseModelMapper(reducedCNF);

			if (mapper.canMap(oldModelNoAg, newModelNoAg)) {
				rej = mapper.getFoundPerm();
			}
		}
		if (rej != null) {
			rej = stripUseless(rej, numVars);
		}
		return rej;
	}

	private int getNumPos(int[] nextModel) {
		int num = 0;
		for (int i : nextModel) {
			if (i > 0)
				num++;
		}
		return num;
	}

	private static int[] removeAgreement(int[] oldModel, int[] agreement) {
		int[] ret = new int[oldModel.length - agreement.length];
		int retIndex = 0;
		int agreementIndex = 0;

		for (int k = 0; k < oldModel.length; k++) {
			if (agreementIndex < agreement.length
					&& oldModel[k] == agreement[agreementIndex]) {
				agreementIndex++;
			} else {
				ret[retIndex] = oldModel[k];
				retIndex++;
			}

		}

		return ret;
	}

	private CNF getRepFromAgreement(CNF function, int[] agree) {
		numTests++;
		if (agree.length == 0)
			return function;
		CNF agreeCNF = form.getCNFFromAgreement(agree); // function.substAll(agree);

		if (implicantTimeout <= 0 || agreeCNF == form.EMPTY)
			return agreeCNF;

		// System.out.println(agree.length);
		DNF implicants = new DNF(function.getContext());
		boolean timeout = false;
		try {
			ISolver solve = agreeCNF.getSolverForCNFEnsureVariableUIDsMatch();
			solve.setTimeoutMs(implicantTimeout);

			int num = 0;
			solve.setKeepSolverHot(false);
			long start = System.currentTimeMillis();
			while (solve.isSatisfiable()) {
				if (System.currentTimeMillis() - start > implicantTimeout) {
					timeout = true;
					if (printProgress && implicantTimeout != 0) {
						System.out.println("TIMEOUT!");
					}
					break;
				}

				num++;
				int[] temp = solve.model(); //solve.primeImplicant();// 

				int size = 0;
				for (int k = 0; k < temp.length; k++) {
					if (temp[k] != 0) {
						size++;
					} else {
						break;
					}
				}

				int[] implicant = new int[size];
				System.arraycopy(temp, 0, implicant, 0, size);
				implicants.fastAddClause(implicant);
				solve.addClause(new VecInt(getRejection(implicant)));

				if (printProgress && num % 1000 == 0) {
					System.out.print(num + " ");
					System.out.println(implicant.length
							- implicants.getContext().size());
				}

			}
		} catch (ContradictionException e) {
			// Obviously unsatisfiable, so is ok
			// won't happen until it finds at least one implicant

			// e.printStackTrace();
		} catch (TimeoutException te) {
			// te.printStackTrace();
			if (printProgress && implicantTimeout != 0) {
				System.out.println("TIMEOUT!");
			}
			timeout = true;
		}
		//

		if (!timeout) {
			CNF ret = new CNF(implicants.getContext());
			ret.addAll(implicants.getClauses()); // All lies, but since we are
			// only doing symmetries
			// it's ok

			if (implicants.size() <= 2)
				return ret;

//			CNF ret2 = DNFToCNFCreator.getCNFFromDNF(ret, true);
			CNF ret2 = ret;
			
			return ret2;
		} else {
			numImplTimeout++;
		}
		return agreeCNF;
	}

	private static int[] getTrueModel(int[] findModel, int origVars) {
		if (findModel == null)
			return null;
		int[] ret = new int[origVars];
		System.arraycopy(findModel, 0, ret, 0, origVars);
		return ret;
	}

	private static int[] getTrueModel(int[] findModel, int[] exampleModel, int origVars) {
		if (findModel == null)
			return null;

		int[] ret = new int[origVars];
		int modInd = 0;

		for(int k = 0; k < ret.length; k++) {
			if(Math.abs(findModel[modInd]) == (k+1)) {
				ret[k] = findModel[modInd];
				modInd++;
			} else {
				ret[k] = exampleModel[k]; //This can happen if we do subsumption/unit propagation stuff
			}
		}
		return ret;
	}

	private static int[] getRejection(int[] firstModel) {
		int[] i = new int[firstModel.length];

		for (int k = 0; k < i.length; k++) {
			i[k] = -firstModel[k];
		}

		return i;
	}

	public List<EventStats> getStats() {
		return stats;
	}

	public boolean isTestGlob() {
		return testGlob;
	}

	public void setTestGlob(boolean testGlob) {
		this.testGlob = testGlob;
	}

	public boolean isTestLocal() {
		return testLocal;
	}

	public void setTestLocal(boolean testLocal) {
		this.testLocal = testLocal;
	}

	public int getMaxSyms() {
		return maxSyms;
	}

	public void setMaxSyms(int maxSyms) {
		this.maxSyms = maxSyms;
	}

	public boolean isLexBreaking() {
		return lexBreaking;
	}

	public void setLexBreaking(boolean lexBreaking) {
		this.lexBreaking = lexBreaking;
	}

	public boolean isUseLocalSymBreak() {
		return useLocalSymBreak;
	}

	public boolean isRandPhase() {
		return randPhase;
	}

	public void setRandPhase(boolean randPhase) {
		this.randPhase = randPhase;
	}

	public void setUseLocalSymBreak(boolean useLocalSymBreak) {
		this.useLocalSymBreak = useLocalSymBreak;
	}

	public boolean isUseGlobalSymBreak() {
		return useGlobalSymBreak;
	}

	public void setUseGlobalSymBreak(boolean useGlobalSymBreak) {
		this.useGlobalSymBreak = useGlobalSymBreak;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public boolean isBreakFast() {
		return breakFast;
	}

	public void setBreakFast(boolean breakFast) {
		this.breakFast = breakFast;
	}

	public long getTotalSolverTime() {
		return totalSolverTime;
	}

	public long getTotalSymTime() {
		return totalSymTime / 1000000;
	}

	public boolean isPrintProgress() {
		return printProgress;
	}

	public void setPrintProgress(boolean printProgress) {
		this.printProgress = printProgress;
	}

	public long getImplicantTimeout() {
		return implicantTimeout;
	}

	public void setImplicantTimeout(long implicantTimeout) {
		this.implicantTimeout = implicantTimeout;
	}

	public int getMaxClBrk() {
		return maxClBrk;
	}

	public void setMaxClBrk(int maxClBrk) {
		this.maxClBrk = maxClBrk;
	}

	public int getNumTests() {
		return numTests;
	}

	public int getNumImplTimeout() {
		return numImplTimeout;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public int getNumCandidates() {
		return numCandidates;
	}

	public int getNumBrkCl() {
		return breaker.getNumClausesAdded();
	}

	public long getNumBrkLit() {
		return breaker.getNumLitsAdded();
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public int getLocalBrkDelay() {
		return localBrkDelay;
	}

	public void setLocalBrkDelay(int localBrkDelay) {
		this.localBrkDelay = localBrkDelay;
	}

	public int getNoFindDelay() {
		return noFindDelay;
	}

	public void setNoFindDelay(int brkDelay) {
		this.noFindDelay = brkDelay;
	}

	
	
}
