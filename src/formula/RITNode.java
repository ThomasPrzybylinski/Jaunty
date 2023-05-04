package formula;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class RITNode {
	protected List<RITNode> children;
	protected Literal lit;
	
	protected int myNum = 0;
	private static int num = 0;

	public RITNode() {
		children = new ArrayList<RITNode>();
		lit = null;
		myNum = ++num;
	}

	public Literal getLiteral() {
		return lit;
	}
	public void setLit(Literal lit) {
		this.lit = lit;
	}

	public void addNode(RITNode toAdd) {
		children.add(toAdd);
	}
	
	public void addNodes(RITNode[] toAdd) {
		for(RITNode node : toAdd) {
			children.add(node);
		}
	}

	public List<RITNode> getChildren() {
		return  Collections.unmodifiableList(children);
	}

	public int size() {
		int size = 1;
		for(RITNode r : children) {
			size += r.size();
		}
		return size;
	}
	
	public int numLeaves() {
		if(children.size() == 0) {
			return 1;
		} else{
			int num = 0;
			for(RITNode r : children) {
				num += r.numLeaves();
			}
			return num;
		}
	}
	
	public String graphizString() {
		StringBuilder sb = new StringBuilder();
		sb.append("digraph rit {\n");
		graphizString(sb,0);
		sb.append("}");
		
		return sb.toString();
	
	}
	
	public void graphizString(StringBuilder sb, int level) {
		if(children.size() > 0) {
			sb.append('"'+ this.unique(level)+'"').append(" -> ");
			if(children.size() == 1) {
				sb.append('"'+children.get(0).unique(level+1)+'"').append(";");
			} else {
				sb.append("{");
				for(RITNode node : children) {
					sb.append('"'+node.unique(level+1)+'"').append(";");
				}
				sb.append("}");
			}
			sb.append("\n");
		}
		for(RITNode node : children) {
			node.graphizString(sb,level+1);
		}
	}
	
	public List<int[]> getAllSolns(int numVars) {
		List<int[]> solns = new ArrayList<int[]>();
		
		for(RITNode node : children) {
			node.getAllSolns(numVars,solns,new int[numVars],0);
		}
		return solns;
	}
	
	public List<int[]> getAllSolns(int numVars, List<int[]> list, int[] cur, int ind) {
		cur[ind] = lit.isPos() ?  1 : -1;
		if(children.size() == 0) {
			list.add(cur);
		} else {
			
			children.get(0).getAllSolns(numVars,list,cur,ind+1);
			for(int k = 1; k < children.size(); k++) {
				int[] newCur = new int[numVars];
				System.arraycopy(cur,0,newCur,0,ind+1);
				children.get(k).getAllSolns(numVars,list,newCur,ind+1);
			}
		}
		
		return list;
		
	}
	
	protected String unique(int level) {
		String varStr = lit == null ? "ROOT" : lit.niceString();
		return ("_"+varStr+"_")+myNum;
		
	}
	
	public void compress() {
		if(this.children.size() == 0) return;
		compressOneOf();
		
		
		compressAllOf();
		
		
		
		
		/*for(int k = 0; k < children.size(); k++) {
			RITNode child = children.get(k);
			if(child.children.size() == 0) continue;
			Set<Literal> vs = child.leaves();
			if(child.leaves() != null) {
				OneOfNode replace = new OneOfNode();
				replace.var = child.var;
				replace.myNum = child.myNum;
				for(Variable v : vs) {
					replace.addVar(v);
				}
				
				child.children.clear();
				child.children.add(replace);
			} else {
				child.compress();
			}
		}*/
	}

	private void compressAllOf() {
		Set<Literal> compSet;
		for(int k = 0; k < this.children.size(); k++) {
			RITNode c = this.children.get(k);
			compSet = c.allOfSet();
			
			if(compSet != null && compSet.size() > 1) {
				AllOfNode replace = new AllOfNode();
				replace.lit = c.lit;
				replace.myNum = c.myNum;
				for(Literal v : compSet) {
					replace.addVar(v);
				}
				
				this.children.set(k,replace);
			} else {
				c.compressAllOf();
			}
		}
	}

	private void compressOneOf() {
		Set<Literal> compSet = this.oneOfSet();
		if(compSet == null || compSet.size() == 1) {
			for(RITNode child : children) {
				child.compressOneOf();
			}
		} else {
			OneOfNode replace = new OneOfNode();
			replace.lit = this.lit;
			replace.myNum = this.myNum;
			for(Literal v : compSet) {
				replace.addVar(v);
			}
			
			this.children.clear();
			this.children.add(replace);
		}
	}
	
	
	protected Set<Literal> allOfSet() {
		Set<Literal> ret = null;
		if(this.children.size() == 0) {
			ret = new TreeSet<Literal>();
			if(this.lit == null) {
				return null; //Root node
			}
			ret.add(this.lit);
		} else if(this.children.size() == 1) {
			Set<Literal> childStuff = this.children.get(0).allOfSet();
			if(childStuff != null) {
				 childStuff.add(this.lit);
				 ret = childStuff;
			}
		}		
		return ret;
	}
	
	protected Set<Literal> oneOfSet() {
		Set<Literal> ret = new TreeSet<Literal>();
		if(this.children.size() == 0) {
			if(this.lit == null) {
				return null; //Root node
			}
			ret.add(this.lit);
		} else if(this.children.size() == 1) {
			if(this.children.get(0).children.size() == 0) {
				ret.add(this.children.get(0).lit);
			} else {
				ret = null;
			}
		}else if(this.children.size() >= 2) {
			RITNode leaf = null;
			RITNode other = null;
			if(this.children.get(0).children.size() == 0) {
				leaf = this.children.get(0);
				other = this.children.get(1);
			} else if(this.children.get(1).children.size() == 0) {
				leaf = this.children.get(1);
				other = this.children.get(0);
			}
			if(leaf != null) {
				ret.add(leaf.lit);
				Set<Literal> sub = other.oneOfSet();
				if(sub != null) {
					ret.addAll(sub);
				} else {
					ret = null;
				}
			} else {
				ret = null;
			}
		}
		
		return ret;
	}
	
	protected Set<Literal> leaves() {
		Set<Literal> ret = new TreeSet<Literal>();
		if(this.children.size() == 0) {
			ret.add(this.lit);
		} else {
			for(RITNode child : children) {
				Set<Literal> childVars = child.leaves();
				if(childVars == null) return null;
				for(Literal v : childVars) {
					if(ret.contains(v.negate())) {
						return null;
					} else {
						ret.add(v);
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	public String toString() {
		String s = lit+"";
		if(children.size() > 0) {
			s += " (";
			for(RITNode r : children) {
				s += r.toString() + " ";
			}
			s += ")";
		}

		return s;
	}



}
