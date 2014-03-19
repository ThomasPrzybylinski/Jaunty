package workflow;

import java.io.File;

import workflow.eclectic.EclecSetCoverCreator;
import workflow.graph.CompoundEdgeManipulator;

public class EclecWorkflowData {
	private CompoundEdgeManipulator distanceMaker;
	private File directory;
	private EclecSetCoverCreator creator;
	
	
	public void addEdgeAdder(CompoundEdgeManipulator ea) {
		distanceMaker = ea;
	}
	
	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File directory) {
		this.directory = directory;
	}
	public CompoundEdgeManipulator getDistanceMaker() {
		return distanceMaker;
	}
	public EclecSetCoverCreator getCreator() {
		return creator;
	}
	public void setCreator(EclecSetCoverCreator creator) {
		this.creator = creator;
	}
	
	
	

}
