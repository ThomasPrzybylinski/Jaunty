package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import formula.BoolFormula;
import formula.Constant;
import formula.Variable;
import hornGLB.AssignmentIter;
import hornGLB.BasicAssignIter;
import task.NChooseRGenerator;

public class TruthTableHelper {
	
	public static String getTruthTables(BoolFormula... forms) {
		return getTruthTables(Arrays.asList(forms));
	}
	
	public static String getTruthTables(Iterable<? extends BoolFormula> forms) {
		StringBuilder sb = new StringBuilder();
		TreeSet<Variable> vars = new TreeSet<Variable>();

		int num = 0;

		sb.append("Formulas:\n");

		for(BoolFormula f : forms) {
			vars.addAll(f.getVars());

			sb.append(num+":"+"\t"+f);
			sb.append("\n");
			num++;
		}


		num = 0;
		sb.append("Vars:\n");

		Variable[] vArray = new Variable[vars.size()];
		for(Variable v : vars) {
			vArray[num]=v;
			sb.append("V"+num+'\t');//+":"+v+"\t");
			num++;
		}
		
		num = 0;
		for(BoolFormula f : forms) {
			vars.addAll(f.getVars());

			sb.append("F"+num);
			sb.append("\t");
			num++;
		}
		
		sb.append("\n");


		AssignmentIter iter = new BasicAssignIter(vars.size());
		while(iter.hasNext()) {
			sb.append("\n");
			int[] curVals = iter.next();

			boolean first = true;
			for(int i : curVals) {
				if(first) {
					first = false;
				} else {
					sb.append('\t');
				}
				sb.append(i);
			}
			
			for(BoolFormula cur : forms) {
				for(int k = 0; k < curVals.length; k++) {
					cur = cur.subst(vArray[k].getPosLit(), curVals[k] == 0 ? false : true);
				}
				cur = cur.reduce();
				if(cur == Constant.TRUE) {
					sb.append('\t');
					sb.append(1);
				} else if(cur == Constant.FALSE) {
					sb.append('\t');
					sb.append(0);
				} else {
					sb.append('\t');
					sb.append("?");
				}
			}
		}


		return sb.toString();
	}
	
	public static List<BitSet> getModels(BoolFormula form) {
		TreeSet<Variable> vars = new TreeSet<Variable>();
		List<BitSet> models = new ArrayList<BitSet>();
		
		
		int num = 0;

		vars.addAll(form.getVars());
		
		Variable[] vArray = new Variable[vars.size()];
		for(Variable v : vars) {
			vArray[num]=v;
			num++;
		}
		
		AssignmentIter iter = new BasicAssignIter(vars.size());
		while(iter.hasNext()) {
			BoolFormula cur = form;
			int[] curVals = iter.next();
			for(int k = 0; k < curVals.length; k++) {
				cur = cur.subst(vArray[k].getPosLit(), curVals[k] == 0 ? false : true);
			}
			cur = cur.reduce();
			if(cur == Constant.TRUE) {
				BitSet b = new BitSet(vars.size());
				
				for(int k = 0; k < curVals.length; k++) {
					if(curVals[k] == 1) {
						b.set(k);
					}
				}
				
				models.add(b);
			}
		}
		return models;
	}
	
	public static BitSet getMinimumModel(BoolFormula form) {
		TreeSet<Variable> vars = new TreeSet<Variable>();
		int num = 0;

		vars.addAll(form.getVars());
		
		Variable[] vArray = new Variable[vars.size()];
		for(Variable v : vars) {
			vArray[num]=v;
			num++;
		}
		
		for(int k = 0; k < vars.size(); k++) {
			NChooseRGenerator gen = new NChooseRGenerator(vars.size(),k);
			while(gen.hasMore()) {
				BoolFormula cur = form;
				int[] positives = gen.getNext();
				Set<Integer> posVars = new TreeSet<Integer>();
				for(int i : positives) {
					posVars.add(i);
				}
				
				for(int i = 0; i < vArray.length; i++) {
					cur = cur.subst(vArray[i].getPosLit(),posVars.contains(i)).reduce();
				}
				
				if(cur == Constant.TRUE) {
					BitSet ret = new BitSet(vars.size());
					for(Integer i : posVars) {
						ret.set(i);
					}
					return ret;
				}
			}
		}
		
		return null;
	}
}
