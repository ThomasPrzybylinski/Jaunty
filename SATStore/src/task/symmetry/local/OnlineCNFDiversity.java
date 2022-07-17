package task.symmetry.local;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import task.formula.FormulaCreatorRandomizer;
import task.formula.ModelsCNFCreator;
import task.formula.random.CNFCreator;
import task.symmetry.ModelMapper;
import task.symmetry.RealSymFinder;
import util.lit.LitUtil;
import util.lit.SymBreaker;
import fi.tkk.ics.jbliss.Reporter;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;

public class OnlineCNFDiversity {
	private CNFCreator creator;

	private ModelMapper globMapper;
	private SymBreaker breaker = new SymBreaker();
	private boolean useLocalSymBreak = true;
	private boolean useGlobalSymBreak = true;
	private boolean globMode = false;
	private long timeOut = Long.MAX_VALUE;
	private int maxSize = Integer.MAX_VALUE;
	private boolean breakFast = false;

	private int[] initial = null;
	private long totalSolverTime = 0;
	private long totalSymTime = 0;

	public OnlineCNFDiversity(CNFCreator creat) {
		this.creator = creat;
	}

	public OnlineCNFDiversity(CNFCreator creat, int[] initial) {
		this.creator = creat;
		this.initial = initial;
	}

	public List<int[]> getDiverseSet() throws ContradictionException, TimeoutException {
		return getDiverseSet(null);
	}

	public List<int[]> getDiverseSet(Random rand) throws ContradictionException, TimeoutException{
		CNFCreator generator = creator;
		VariableContext context = new VariableContext();


		FormulaCreatorRandomizer randomizer = null;
		if(rand != null) {
			randomizer = new FormulaCreatorRandomizer(creator,rand);
			randomizer.setupRandomnessAlready(context);

			if(initial != null) {
				initial = randomizer.translateToInternal(initial);
			}

			generator = randomizer;
		}


		CNF function = generator.generateCNF(context);
		int origVars = context.size();

		CNF origCNF = function;

		ClauseList debug = null;
		if(creator instanceof ModelsCNFCreator) {
			ModelsCNFCreator mcc = ((ModelsCNFCreator)creator);
			if(mcc.getPrevMods() != null) {
				List<int[]> mods = mcc.getPrevMods();
				//Our "true true" original vars is different in this case
				origVars = mcc.getOrigVars();
				origCNF = getEquaSymmetricModelsCNF(randomizer, origVars, mods);
			}
		}

		return getDivSetFromCNF(randomizer, function, origVars, origCNF);
	}

	private CNF getEquaSymmetricModelsCNF(FormulaCreatorRandomizer randomizer,
			int origVars, List<int[]> mods) {
		totalSolverTime = 0;
		CNF origCNF;
		ClauseList debug;
		origCNF = new CNF(new VariableContext());
		debug = new ClauseList(VariableContext.defaultContext);

		for(int[] i : mods) {
			i = i.clone();
			if(randomizer != null) {
				i = randomizer.translateToInternal(i);
			}

			debug.addClause(i.clone());
			for(int k = 0; k < i.length; k++) {
				i[k] = -i[k];
			}
			origCNF.fastAddClause(i);
		}
		origCNF.getContext().ensureSize(origVars);
		origCNF.sort();
		return origCNF;
	}

	public List<int[]> getDivSetFromCNF(FormulaCreatorRandomizer randomizer,
			CNF function, int origVars, CNF origCNF)
					throws ContradictionException, TimeoutException {
		totalSolverTime = 0;
		totalSymTime = 0;
		long start = System.currentTimeMillis();
		globMapper = new ModelMapper(origCNF);

		ArrayList<int[]> curModels = new ArrayList<int[]>();

		ISolver solver = function.getSolverForCNFEnsureVariableUIDsMatch();

		RealSymFinder finder;
		LiteralGroup globalGroup;
		if(useGlobalSymBreak) {
			long symStart = System.nanoTime();
			finder = new RealSymFinder(origCNF);
			globalGroup =  finder.getSymGroup();
//			LiteralGroup globalGroup2 = useJBliss(origCNF);
//			globalGroup = globalGroup2;
			
//			LocalSymClauses cl = new LocalSymClauses(origCNF,false);
//			LiteralGroup m1 = cl.getModelGroup(globalGroup);
//			LiteralGroup m2 = cl.getModelGroup(globalGroup2);
			for(int[] cl : breaker.getFullSymBreakClauses(globalGroup, new int[]{})) {
				solver.addClause(new VecInt(cl));
			}
			totalSymTime += (System.nanoTime()-symStart);
		}

		long solveStart = System.nanoTime();
		int[] firstModel;
		if(initial == null) {
			firstModel = getTrueModel(solver.findModel(),origVars);
		} else {
			firstModel = initial;
		}
		totalSolverTime += (System.nanoTime() - solveStart);
		
		if(firstModel == null)  {
			System.out.println("Unsatisfiable Theory");
			System.exit(0);
		}
		curModels.add(firstModel);

		int[] rejectFirstModel = getRejection(firstModel);
		solver.addClause(new VecInt(rejectFirstModel));
		//		solver.clearLearntClauses();


		int[] nextModel;
		int numChecked = 0;
		while(true) {
			numChecked++;
//			System.out.println(curModels.size() +"/" +numChecked);
			solveStart = System.nanoTime();
			nextModel = solver.findModel();
			totalSolverTime += (System.nanoTime() - solveStart);
			if(nextModel == null) break;
			if((System.currentTimeMillis() - start) > timeOut) break;
			
			
			nextModel = getTrueModel(nextModel,origVars);
			boolean add = true;

			int[] rejectModel = getRejection(nextModel);
			solver.addClause(new VecInt(rejectModel));
			//			solver.clearLearntClauses();

			//			for(int[] oldModel : curModels) {
			//			for(int k = 0; k < curModels.size(); k++) {
			for(int k = curModels.size()-1; k >= 0 ; k--) {
				int[] oldModel = curModels.get(k);
				int[] agree = LitUtil.getAgreement(oldModel,nextModel);
				CNF reducedCNF = getFormulaFromAgreement(origCNF,agree);

				if(reducedCNF.size() == 0) {
					add = false;
					continue;
				}
				
				long symStart = System.nanoTime();
				LiteralPermutation rejPerm = null;
				if(add) {
					rejPerm = processSymmetry(oldModel,nextModel,reducedCNF, agree);
					add &= (rejPerm == null);
				}

				if(useLocalSymBreak) {
					if(rejPerm != null) {
						if(globalRejection) {
							for(int[] cl : breaker.getFullBreakingClauseForPerm(new int[]{},rejPerm)) {
								solver.addClause(new VecInt(cl));
							}
						} else {
							for(int[] cl : breaker.getFullBreakingClauseForPerm(agree,rejPerm)) {
								solver.addClause(new VecInt(cl));
							}
							//						//						finder.addKnownSubgroup(new NaiveLiteralGroup(rejPerm));
						}
					}

					finder = new RealSymFinder(reducedCNF);
					LiteralGroup lg =  finder.getSymGroup();
//					LiteralGroup lg = useJBliss(reducedCNF);
					for(int[] cl : breaker.getFullSymBreakClauses(lg,agree)) {
						solver.addClause(new VecInt(cl));
					}
				}
				totalSymTime += (System.nanoTime() - symStart);
				
				if(!add && (!useLocalSymBreak || breakFast)) break;

				//								if(!add) {// && solver.nConstraints() <= 10*origCNF.size()) {
				//									break;
				//								}
			}
			if(add) {
				curModels.add(nextModel);
				if(curModels.size() == maxSize) break;
			}
		}
		solver.reset();
		if(randomizer != null) {
			for(int k = 0; k <curModels.size(); k++) {
				curModels.set(k, randomizer.translateToOrig(curModels.get(k)));
			}
		}
		return curModels;
	}
	
	private class Report implements Reporter {
		List<LiteralPermutation> perms;
		int numVars;
		
		public Report(int numVars) {
			this.numVars = numVars;
			perms = new LinkedList<LiteralPermutation>();
		}
		@Override
		public void report(Map arg0, Object arg1) {
			int[] i = new int[numVars+1];
			for(int k = 1; k <= numVars; k++) {
				i[k] = (Integer)arg0.get(k);
			}
			perms.add( new LiteralPermutation(i));
		}
		
		public LiteralGroup getGroup() {
			return new NaiveLiteralGroup(perms);
		}

		
	}

//	private LiteralGroup useJBliss(CNF reducedCNF) {
//		Graph<Integer> g = new Graph<Integer>();
//		g.add_vertex(0); //Only clauses adj to 0
//		for(int i = 1; i <= reducedCNF.getContext().size(); i++) {
//			g.add_vertex(i);
//			g.add_vertex(-i);
//			g.add_edge(i,-i);
//		}
//		
//		for(int i = 1; i <= reducedCNF.size(); i++) {
//			
//			
//			int[] cl = reducedCNF.getClauses().get(i-1);
////			if(cl.length == 2) {
////				g.add_edge(cl[0],cl[1]);
////			} else {
//				int ver = reducedCNF.getContext().size()+i;
//				g.add_vertex(ver);
//				g.add_edge(ver,0);
//				for(int lit : cl) {
//					g.add_edge(lit,ver);
//				}
////			}
//		}
//		Report rep = new Report(reducedCNF.getContext().size());
//		g.find_automorphisms(rep,null);
//		return rep.getGroup().reduce();
//	}

	private boolean globalRejection = false;
	private LiteralPermutation processSymmetry(int[] oldModel, int[] nextModel,
			CNF reducedCNF, int[] agreement) {

		boolean similar = true; //returns !similar
		globalRejection = false;
		int[] oldModelNoAg = removeAgreement(oldModel,agreement);
		int[] newModelNoAg = removeAgreement(nextModel,agreement);

		ModelMapper mapper = new ModelMapper(reducedCNF);
		if(!globMode) {
			similar = mapper.canMap(oldModelNoAg,newModelNoAg);
		}

		if(!similar) {
			globalRejection = true;
			mapper = globMapper;
			similar = mapper.canMap(oldModel,nextModel);
		} else {
			return mapper.getFoundPerm();
		}

		return similar ? mapper.getFoundPerm() : null; 
	}

	private static int[] removeAgreement(int[] oldModel, int[] agreement) {
		int[] ret = new int[oldModel.length-agreement.length];
		int retIndex = 0;
		int agreementIndex = 0;

		for(int k = 0; k < oldModel.length; k++) {
			if(agreementIndex < agreement.length && oldModel[k] == agreement[agreementIndex]) {
				agreementIndex++;
			} else {
				ret[retIndex] = oldModel[k];
				retIndex++;
			}

		}

		return ret;
	}

	private static CNF getFormulaFromAgreement(CNF function, int[] agree) {
		if(agree.length == 0) return function;
		CNF curFunction = function.substAll(agree);
		return curFunction.trySubsumption();//.trySubsumption().reduce();//
	}

	private static int[] getTrueModel(int[] findModel, int origVars) {
		if(findModel == null) return null;
		int[] ret = new int[origVars];
		System.arraycopy(findModel,0,ret,0,origVars);
		return ret;
	}

	private static int[] getRejection(int[] firstModel) {
		int[] i = new int[firstModel.length];

		for(int k = 0; k < i.length; k++) {
			i[k] = -firstModel[k];
		}

		return i;
	}

	public boolean isUseLocalSymBreak() {
		return useLocalSymBreak;
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

	public boolean isGlobMode() {
		return globMode;
	}

	public void setGlobMode(boolean globMode) {
		this.globMode = globMode;
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
		return totalSymTime/1000000;
	}
	
	
	
	
}
