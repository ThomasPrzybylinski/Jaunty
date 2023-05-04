package syntaxTests;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import formula.VariableContext;
import task.formula.LineColoringCreator;
import task.formula.random.CNFCreator;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public class SparseTest {

	public static void main(String[] args) throws Exception {
		CNFCreator creator = new LineColoringCreator(6,3); // new EmorySchedule();//new QueensToSAT(8);// new IdentityCNFCreator("testcnf\\logistics.a.cnf");// new IdentityCNFCreator("testcnf/bmc-galileo-9.cnf","bmc-galileo-9.cnf",true);///// new IdentityCNFCreator("testcnf\\logistics.a.cnf");//
		CNFSparseOnlineCNFDiversity div = new CNFSparseOnlineCNFDiversity(creator.generateCNF(VariableContext.defaultContext));
		div.setPrintProgress(true);
//		div.setUseGlobalSymBreak(false);
//		div.setUseLocalSymBreak(false);
//		div.forceGlobBreakCl = true;
//		div.setBreakFast(true);
		div.setMaxSize(100);
//		OnlineCNFDiversity div = new OnlineCNFDiversity(creator);
		List<int[]> ret = div.getDiverseSet();
		System.out.println(ret.size());
		
		File f = null;
		if(creator instanceof FileDecodable || creator instanceof ConsoleDecodeable) {
			File f1 = new File("SparseTest");
			f = new File(f1, creator.toString());
			f.mkdirs();

			for(File del : f.listFiles()) {
				del.delete();
			}
		}
		
		if(creator instanceof FileDecodable) {
			int num = 0;
			for(int[] i : ret) {
				FileDecodable decoder = (FileDecodable)creator;
				decoder.fileDecoding(f, "model_"+num ,i);
				num++;
			}
		} else if(creator instanceof ConsoleDecodeable) {
			int num = 0;
			for(int[] i : ret) {
				ConsoleDecodeable decoder = (ConsoleDecodeable)creator;
				
				PrintWriter pw = new PrintWriter(new File(f, "model_"+num +".txt"));
				pw.print(decoder.consoleDecoding(i));
				pw.flush();
				pw.close();
				num++;
			}
		}
	}

}
