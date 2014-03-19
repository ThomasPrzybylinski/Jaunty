package initialEclecTests;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import task.formula.QueensToSAT;
import task.sat.SATUtil;
import task.symmetry.SymmetryUtil;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.DNF;
import graph.graphviz.GraphizUtil;


public class CopyOfQueensTest {

	public static void main(String[] args) throws Exception {
		VariableContext context = VariableContext.defaultContext;

		QueensToSAT create = new QueensToSAT(8);
		CNF cnf = create.encode(context);

		System.out.println(cnf);
		System.out.println(cnf.toNumString());

		List<int[]> models = SATUtil.getAllModels(cnf);
		//Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
		List<int[]> origModels = new ArrayList<int[]>(models);
		
		DNF dnfForSym = new DNF(cnf.getContext());
		for(int[] i : models) {
			dnfForSym.addClause(i);
		}
		
		List<int[]> perms = SymmetryUtil.getSyms(cnf);
		//List<int[]> 
		perms = SymmetryUtil.getSyms(dnfForSym);
		
		models = SymmetryUtil.breakModels(models,perms);
		dnfForSym = new DNF(dnfForSym.getContext());
		
		//Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
		int num = 0;
		for(int[] m : models) {
			dnfForSym.addClause(m);
//			System.out.println("Orig");
			System.out.println(create.decode(m));
			create.decodeAndSavePicture(m,"queensUnique"+num+".png");
			num++;
			for(int[] s : perms) {
				//System.out.println(create.decode(PermutationUtil.permute(m,s)));
				//System.out.println(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(s)));
			}
		}
		
		System.out.println(models.size());
		DNF unitLocal = new DNF(dnfForSym.getContext());
		
		for(int[] model : origModels) {
			unitLocal.addClause(model);
		}
		
		Collection<int[]> nowModels = SymmetryUtil.breakyByUnitLocalSyms(unitLocal);
		unitLocal.getClauses().clear();
		for(int[] model : nowModels) {
			unitLocal.addClause(model);
		}
		
		nowModels = SymmetryUtil.breakyByUnitLocalSyms(unitLocal);
		
		unitLocal.getClauses().clear();
		for(int[] model : nowModels) {
			unitLocal.addClause(model);
		}
		
		nowModels = SymmetryUtil.breakyByUnitLocalSyms(unitLocal);
		
		unitLocal.getClauses().clear();
		for(int[] model : nowModels) {
			unitLocal.addClause(model);
		}
		
		nowModels = SymmetryUtil.breakyByUnitLocalSyms(unitLocal);
		
		num = 0;
		for(int[] m : nowModels) {
			dnfForSym.addClause(m);
//			System.out.println("Orig");
			System.out.println(create.decode(m));
			create.decodeAndSavePicture(m,"queensLocal"+num+".png");
			num++;
//			for(int[] s : perms) {
//				System.out.println(create.decode(PermutationUtil.permute(m,s)));
//			}
		}
		System.out.println(nowModels.size());
		
		List<int[]> toStrange = new ArrayList<int[]>();
		toStrange.addAll(nowModels);
		int[][] lens = PrototypesUtil.doAgreementSym(origModels,dnfForSym.getContext());
//		int[][] lens = StrangeUtil.doTheThing(models,dnfForSym.getContext()); //12 of them
		
		int[][] other = new int[lens.length][lens[0].length];
		
		List<Integer> globMods = new ArrayList<Integer>();
		for(int k = 0; k < origModels.size(); k++) {
			for(int i = 0; i < models.size(); i++) {
				if(Arrays.equals(origModels.get(k), models.get(i))) {
					globMods.add(k);
					break;
				}
			}
		}
		
		for(int k = 0; k < lens.length; k++) {
			for(int i = 0; i < lens.length; i++) {
				if(i == k) continue;
//				int index2 = globMods.get(i);
				if(lens[k][i] != 1) {
					other[k][i] = 1;
				}
			}
		}
		
		
		GraphizUtil.saveGraphAsPic(other,"QueenAgreementSymmetryGraph");
		
		System.out.println(create.decode(origModels.get(0)));
		for(int k = 1; k < lens[0].length; k++) {
			if(lens[0][k] > 1) {
				System.out.println(create.decode(origModels.get(k)));
			}
		}
		
	}
}
