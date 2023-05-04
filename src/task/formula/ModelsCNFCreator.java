package task.formula;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.random.CNFCreator;
import task.translate.FileDecodable;
import util.lit.LitUtil;
import util.lit.LitUtil.RemovalDecoding;
import workflow.ModelGiver;

public class ModelsCNFCreator implements CNFCreator, FileDecodable {

	ModelGiver giver;
	int origVars;
	ArrayList<int[]> prevMods;
	private boolean simplifyVars = false;
	RemovalDecoding decoding = null;
	
	public ModelsCNFCreator(ModelGiver giver) {
		this(giver,false);
	}
	
	public ModelsCNFCreator(ModelGiver giver, boolean simplifyVars) {
		this.giver = giver;
		prevMods = new ArrayList<int[]>();
		this.simplifyVars = simplifyVars;
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		VariableContext origContext = new VariableContext();
		List<int[]> models;
		try {
			models = giver.getAllModels(origContext);
		} catch(TimeoutException te) {
			return null;
		}
		
		return getModelsCNF(context, origContext, models);

	}

	public CNF getModelsCNF(VariableContext context,
			VariableContext origContext, List<int[]> models) {
		if(simplifyVars) {
			decoding = LitUtil.getSingleValAndEquivVarDecoding(models,origContext);
			models = LitUtil.removeSingleValAndEquivVars(models,origContext,decoding);
		}
		
		if(prevMods != null) {
			prevMods.clear();
			prevMods.addAll(models);
		}
		
		origVars = origContext.size();

		return getCNFFromModles(context, origContext, models);
	}

	public static CNF getCNFFromModles(VariableContext context,
			VariableContext origContext, List<int[]> models) {
		int origVars = origContext.size();
		context.ensureSize(origContext.size()+models.size());

		CNF ret = new CNF(context);

		int[] mustBeAModel = new int[models.size()];

		for(int k = 0; k < models.size(); k++) {
			for(int i = k+1; i < models.size(); i++) {
				//Only one model
				ret.fastAddClause(-getModelVar(k,origVars),-getModelVar(i,origVars));
			}
			mustBeAModel[k] = getModelVar(k,origVars);
		}
		ret.fastAddClause(mustBeAModel);

		int modelInd = 0;
		for(int[] model : models) {
			for(int k = 0; k < model.length; k++) {
				//If the var is not assigned to what the model should be
				//then this model cannot be the model
				//That is (NOT model[k] implies NOT the modelInd-nth model
				ret.fastAddClause(model[k],-getModelVar(modelInd,origVars));
			}
			modelInd++;
		}

		ret.sort();

		return ret;
	}
	
	public int getOrigVars() {
		return origVars;
	}

	private int getModelVar(int model) {
		return getModelVar(model,origVars);
	}
	
	private static int getModelVar(int model, int origVars) {
		return origVars+model+1;
	}
	
	public ArrayList<int[]> getPrevMods() {
		return prevMods;
	}

	public void setPrevMods(ArrayList<int[]> prevMods) {
		this.prevMods = prevMods;
	}

	public ModelGiver getGiver() {
		return giver;
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		int[] realModel = getTrueModel(model,origVars);

		if(giver instanceof FileDecodable) {
			((FileDecodable)giver).fileDecoding(dir,filePrefix,realModel);
		}

		//Else Throw exception?
	}

	//Ignore added symmetry breaking vars
	private static int[] getTrueModel(int[] findModel, int origVars) {
		int[] ret = new int[origVars];
		System.arraycopy(findModel,0,ret,0,origVars);
		return ret;
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		int[] realModel = getTrueModel(model,origVars);
		if(decoding != null) {
			realModel = LitUtil.reverseDecode(realModel, decoding);
		}
		if(giver instanceof FileDecodable) {
			((FileDecodable)giver).fileDecoding(filePrefix,realModel);
		}
		//Else Throw exception?

	}
	
	@Override
	public String toString() {
		return "CNF_"+giver.getDirName();
	}
}
