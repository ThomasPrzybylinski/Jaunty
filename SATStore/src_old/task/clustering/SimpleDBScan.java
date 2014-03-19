package task.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import util.LitsSet;

//For clustering models. Since there is no such thing as "noise" for SAT models
//the only attribute we set is the radius

//Right now we are using a simple distance metric
public class SimpleDBScan {
	private int radius;
	private ModelDistance measure;

	public SimpleDBScan(int radius) {
		this.radius = radius;
		measure = new SimpleDifference();
	}
	
	public SimpleDBScan(int radius, ModelDistance measure) {
		this.radius = radius;
		this.measure = measure;
	}

	public List<Set<int[]>> getClustering(List<int[]> models) {
		List<Set<int[]>> clusters = new ArrayList<Set<int[]>>();
		List<Set<int[]>> itemCluster = new ArrayList<Set<int[]>>(models.size());

		for(int[] m : models) {
			LitsSet itemSet = new LitsSet(m.length);
			itemSet.add(m);
			itemCluster.add(itemSet);
		}



		for(int k = 0; k < models.size(); k++) {
			int[] curModel = models.get(k);

			for(int i = k+1; i < models.size(); i++) {
				int[] otherModel = models.get(i);

				if(measure.distance(curModel,otherModel) <= radius) {
					Set<int[]> cluster = itemCluster.get(k);
					Set<int[]> otherCluster = itemCluster.get(i);
					cluster.addAll(otherCluster);
					itemCluster.set(i,cluster);
					
					for(int j = 0; j < i; j++) {
						if(itemCluster.get(j) == otherCluster) {
							itemCluster.set(j,cluster);
						}
					}
					
				}
			}
		}

		for(int k = 0; k < itemCluster.size(); k++) {
			Set<int[]> curCluster = itemCluster.get(k);

			boolean add = true;
			for(int i = 0; i < clusters.size(); i++) {
				if(clusters.get(i) == curCluster) {
					add = false;
					break;
				}
			}

			if(add) {
				clusters.add(curCluster);
			}
		}
		return clusters;
	}
}
