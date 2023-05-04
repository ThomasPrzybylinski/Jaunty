package task.symmetry.sparse;

import static org.sat4j.core.LiteralsUtils.negLit;
import static org.sat4j.core.LiteralsUtils.posLit;

import java.util.Random;

import org.sat4j.minisat.core.IPhaseSelectionStrategy;

public class RandomPhase implements IPhaseSelectionStrategy {

	private static final long serialVersionUID = 6859102166106895132L;
	Random rand = new Random();
	
	public RandomPhase() {
		
	}
	
	public RandomPhase(int seed) {
		rand = new Random(seed);
	}

    public void assignLiteral(int p) {
    }

    public void init(int nlength) {
    }

    public void init(int var, int p) {
    }

    public int select(int var) {
        if (rand.nextBoolean()) {
            return posLit(var);
        }
        return negLit(var);
    }

    public void updateVar(int p) {
    }

    public void updateVarAtDecisionLevel(int q) {
    }
}

//public class RandomPhase implements IOrder {
//	private IOrder other;
//	Random rand = new Random(1);
//	ILits lits;
//	
//	public RandomPhase(IOrder other) {
//		this.other = other;
//	}
//	
//	@Override
//	public void setLits(ILits lits) {
//		other.setLits(lits);
//		this.lits = lits;
//		
//	}
//
//	@Override
//	public int select() {
//		int selected = other.select();
//		System.out.println(selected);
//		if(selected != lits.UNDEFINED) {
//			selected = rand.nextBoolean() ? selected : -selected;
//			System.out.println(selected);
//			return selected;
//		} else {
//			return lits.UNDEFINED;
//		}
//	}
//
//	@Override
//	public void undo(int x) {
//		other.undo(x);
//	}
//
//	@Override
//	public void updateVar(int p) {
//		other.updateVar(p);
//	}
//
//	@Override
//	public void init() {
//		other.init();
//	}
//
//	@Override
//	public void printStat(PrintWriter out, String prefix) {
//		other.printStat(out,prefix);
//		
//	}
//
//	@Override
//	public void setVarDecay(double d) {
//		other.setVarDecay(d);
//		
//	}
//
//	@Override
//	public void varDecayActivity() {
//		other.varDecayActivity();
//		
//	}
//
//	@Override
//	public double varActivity(int p) {
//		return other.varActivity(p);
//	}
//
//	@Override
//	public void assignLiteral(int p) {
//		other.assignLiteral(p);
//	}
//
//	@Override
//	public void setPhaseSelectionStrategy(IPhaseSelectionStrategy strategy) {
//		other.setPhaseSelectionStrategy(strategy);
//	}
//
//	@Override
//	public IPhaseSelectionStrategy getPhaseSelectionStrategy() {
//		return other.getPhaseSelectionStrategy();
//	}
//
//	@Override
//	public void updateVarAtDecisionLevel(int q) {
//		other.updateVarAtDecisionLevel(q);
//	}
//
//	@Override
//	public double[] getVariableHeuristics() {
//		return other.getVariableHeuristics();
//	}
//
//}
