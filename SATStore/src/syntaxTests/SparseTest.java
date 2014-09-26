package syntaxTests;

import java.io.File;
import java.util.List;

import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.random.CNFCreator;
import task.symmetry.local.OnlineCNFDiversity;
import task.symmetry.sparse.SparseOnlineCNFDiversity;
import task.translate.FileDecodable;

public class SparseTest {

	public static void main(String[] args) throws Exception {
		CNFCreator creator = new QueensToSAT(8);
		SparseOnlineCNFDiversity div = new SparseOnlineCNFDiversity(creator);
//		OnlineCNFDiversity div = new OnlineCNFDiversity(creator);
		List<int[]> ret = div.getDiverseSet();
		System.out.println(ret.size());
		
		File f = null;
		if(creator instanceof FileDecodable) {
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
		}
	}

}
