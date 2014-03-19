import initialEclecTests.PrototypesUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import task.formula.QueensToSAT;
import task.sat.SATUtil;
import task.symmetry.SymmetryUtil;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.DNF;


public class SimpleSymTest {

	public static void main(String[] args) throws Exception {
		VariableContext context = VariableContext.defaultContext;

		QueensToSAT create = new QueensToSAT(8);
		CNF cnf = create.encode(context);

		System.out.println(cnf);
		System.out.println(cnf.toNumString());

		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
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
		
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
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
		
		for(int[] model : models) {
			unitLocal.addClause(model);
		}
		
		Collection<int[]> nowModels = SymmetryUtil.breakyByUnitLocalSyms(unitLocal);
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
		
		int[][] lens = PrototypesUtil.doAgreementSym(origModels,dnfForSym.getContext());
//		int[][] lens = StrangeUtil.doTheThing(models,dnfForSym.getContext()); //12 of them
		
		System.out.println(create.decode(origModels.get(0)));
		for(int k = 1; k < lens[0].length; k++) {
			if(lens[0][k] > 1) {
				System.out.println(create.decode(origModels.get(k)));
			}
		}
		
	}
}
