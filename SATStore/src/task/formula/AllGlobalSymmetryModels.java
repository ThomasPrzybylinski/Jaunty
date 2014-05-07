package task.formula;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.LiteralPermutation;
import task.formula.random.CNFCreator;
import task.sat.SATUtil;
import task.symmetry.RealSymFinder;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import util.PermutationUtil;
import util.lit.LitsSet;
import workflow.ModelGiver;

public class AllGlobalSymmetryModels implements ModelGiver, ConsoleDecodeable {
	private ModelGiver giver;
	private VariableContext myContext;
	private int numLits;

	public AllGlobalSymmetryModels(ModelGiver giver) {
		super();
		this.giver = giver;
	}


	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		List<int[]> models = giver.getAllModels(context);

		myContext = new VariableContext();
		ClauseList modelList = new ClauseList(myContext);
		modelList.addAll(models);
		numLits = 2*myContext.size();

		RealSymFinder finder = new RealSymFinder(modelList);
		LiteralGroup lg = finder.getSymGroup(); //Should be a strong group.

		LitsSet set = new LitsSet(myContext.size());
		LinkedList<LiteralPermutation> curPerms = new LinkedList<LiteralPermutation>();

		curPerms.addAll(lg.getGenerators());
		set.add((new LiteralPermutation(myContext.size())).asArray());
		ArrayList<LiteralPermutation> uniquePerms = new ArrayList<LiteralPermutation>();
		uniquePerms.add(new LiteralPermutation(myContext.size()));
		//Generate all perms. lg should be a strong group, so there is a better algorithm
		//than what I'm doing.
		while(!curPerms.isEmpty()) {
			LiteralPermutation cur = curPerms.poll();

			for(LiteralPermutation p : lg.getGenerators()) {
				LiteralPermutation other = cur.compose(p);
				if(!set.contains(other.asArray())) {
					set.add(other.asArray());
					curPerms.add(other);
					uniquePerms.add(other);
				}
			}
		}

		set = null;
		curPerms = null;

		//Turn perms into models
		VariableContext mapContext = context;//new VariableContext();
		ClauseList ret = new ClauseList(mapContext);

		for(LiteralPermutation perm : uniquePerms) {
			int[] array = perm.asArray();

			int[] model = new int[numLits*numLits];
			for(int k = 0; k < model.length; k++) {
				model[k] = -(k+1); //all lits start negative
			}

			for(int k = 1; k < array.length; k++) {
				int mapVar = getLitMapVar(k,array[k]);
				model[mapVar-1] = mapVar;
			}

			ret.addClause(model);
		}

		return ret.getClauses();

	}

	int getLitMapVar(int lit1, int lit2) {
		//		int lit1Index = LitUtil.getIndex(lit1,numVars) + (lit1 > 0 ? -1 : 0);
		//		int lit2Index = LitUtil.getIndex(lit2,numVars) + (lit2 > 0 ? -1 : 0);

		int lit1Index = 2*Math.abs(lit1) + (lit1 < 0 ? 1 : 0) - 2;
		int lit2Index = 2*Math.abs(lit2) + (lit2 < 0 ? 1 : 0) - 2;


		return numLits*lit1Index + lit2Index + 1;
	}

	@Override
	public String consoleDecoding(int[] model) {
		int[] perm = new int[numLits+1];
		
		int numVars = numLits/2;
		
		for(int k = 1; k <= numVars; k++) {

			for(int i = -numVars; i <= numVars; i++) {
				if(i == 0) continue;
				if(model[getLitMapVar(k,i)-1] > 0) {
					//					System.out.println(k+"->"+i);
					perm[k] = i;
					break;
				}
			}
		}

		return PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(perm));
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return null;
	}

	@Override
	public String getDirName() {
		return "SymsOf"+giver.getDirName();
	}

}
