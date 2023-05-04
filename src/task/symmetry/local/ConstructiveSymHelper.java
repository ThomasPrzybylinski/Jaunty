package task.symmetry.local;

import java.util.LinkedList;
import java.util.List;

import formula.simple.ClauseList;
import group.LiteralGroup;
import group.SchreierVector;
import task.symmetry.RealSymFinder;
import util.IntegralDisjointSet;

public class ConstructiveSymHelper {
	private LinkedList<LiteralGroup> varGroups;
	private LinkedList<LiteralGroup> modelGroups;
	private LocalSymClauses curClauses;
	private LinkedList<IntegralDisjointSet> cumulativeModelOrbits;
	
	public ConstructiveSymHelper(ClauseList curClauses) {
		this.curClauses = new LocalSymClauses(curClauses,true);
		this.varGroups = new LinkedList<LiteralGroup>();
		this.modelGroups = new LinkedList<LiteralGroup>();
		this.cumulativeModelOrbits = new LinkedList<IntegralDisjointSet>();
		
		RealSymFinder sym = new RealSymFinder(curClauses);

		LiteralGroup lg = sym.getSymGroup();
		LiteralGroup modelGroup = this.curClauses.getModelGroup(lg);
		SchreierVector globModelVector = new SchreierVector(modelGroup);
		IntegralDisjointSet globModelOrbits = globModelVector.transcribeOrbits(false);
		
		varGroups.add(lg);
		modelGroups.add(modelGroup);
		cumulativeModelOrbits.add(globModelOrbits);
	}
	
	public void addLayer(int literalCondition) {
		this.curClauses.post();
		this.curClauses.addCondition(literalCondition);
		
		RealSymFinder sym = new RealSymFinder(this.curClauses.getCurList(false));

		LiteralGroup lg = sym.getSymGroup();
		LiteralGroup modelGroup = this.curClauses.getModelGroup(lg).reduce();
		SchreierVector modelVector = new SchreierVector(modelGroup);
		IntegralDisjointSet modelOrbits = modelVector.transcribeOrbits(false);
		
		IntegralDisjointSet prevSet = cumulativeModelOrbits.getLast();
		IntegralDisjointSet curSet = new IntegralDisjointSet(prevSet);
		
		for(Integer i : modelOrbits.getRoots()) {
			List<Integer> orbit = modelOrbits.getSetWith(i.intValue());
			
			for(Integer k : orbit) {
				if(k != i) {
					curSet.join(k.intValue(),i.intValue());
				}
			}
		}
		
		varGroups.add(lg);
		modelGroups.add(modelGroup);
		cumulativeModelOrbits.add(curSet);
		
	}
	
	public void pop() {
		if(curClauses.getCurrentCondition().size() > 0) {
			curClauses.pop();
			varGroups.removeLast();
			modelGroups.removeLast();
			cumulativeModelOrbits.removeLast();
		}
	}
	
	public LiteralGroup getCurrentLitGroup() {
		return varGroups.getLast();
	}
	
	public LiteralGroup getCurrentModelGroup() {
		return modelGroups.getLast();
	}
	
	public IntegralDisjointSet getCumulativeModelOrbits() {
		return cumulativeModelOrbits.getLast();
	}
	
	public ClauseList getCurClauses(boolean keepSingleValVars) {
		return curClauses.getCurList(keepSingleValVars);
	
	}
	
	public IntegralDisjointSet getModelOrbitsAtLevel(int level) {
		return cumulativeModelOrbits.get(level);
	}
	
	//TODO: Read-only version?
	public LocalSymClauses getLocalClauses() {
		return curClauses;
	}
	
	public String getLocalSymClauseRep() {
		return curClauses.toString();
	}
}
