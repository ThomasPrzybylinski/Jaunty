package formula;

import java.util.Set;
import java.util.TreeSet;

public class AllOfNode extends RITNode {

	protected Set<Literal> vars;
	
	public AllOfNode() {
		this.vars = new TreeSet<Literal>();
	}
	
	public void addVar(Literal v) {
		vars.add(v);
	}
	
	
	@Override
	public void graphizString(StringBuilder sb, int level) {
		if(children.size() > 0) {
			sb.append(this.unique(level)).append(" -> ");
			if(children.size() == 1) {
				sb.append(children.get(0).unique(level+1)).append(";");
			} else {
				sb.append("{");
				for(RITNode node : children) {
					sb.append(node.unique(level+1)).append(";");
				}
				sb.append("}");
			}
			sb.append("\n");
		}
		for(RITNode node : children) {
			node.graphizString(sb,level+1);
		}
	}

	@Override
	protected String unique(int level) {
		String varStr;
		if(lit == null && vars.size() == 0) {
			varStr = "ROOT"; 
		} else {
			varStr = "_ALL_";
			for(Literal var : vars) {
				varStr += var.niceString();
				varStr += "_";
			}
			varStr += "_";
		}
		return (varStr+"_")+myNum;
	}

	@Override
	public void setLit(Literal var) {
		// TODO Auto-generated method stub
		super.setLit(var);
	}
	

}
