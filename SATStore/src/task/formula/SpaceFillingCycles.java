package task.formula;

import java.util.ArrayList;
import java.util.List;

import task.NChooseRGenerator;
import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import formula.VariableContext;
import formula.simple.CNF;


///CURRENTLY FLAWED!!

public class SpaceFillingCycles implements  CNFCreator, ConsoleDecodeable{
	private int height;
	private int length;
	
	public SpaceFillingCycles(int height, int length) {
		this.height = height;
		this.length = length;
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		//start at (0,0) end at (length-1, height-1) 
		CNF ret = new CNF(context);

		for(int k = 0; k < length; k++) {
			for(int i = 0; i < height; i++) {
				ArrayList<Integer> adj = new ArrayList<Integer>();

				addAdIfValid(k, i, adj, -1, 0);
				addAdIfValid(k, i, adj, 0, -1);
				addAdIfValid(k, i, adj, 1, 0);
				addAdIfValid(k, i, adj, 0, 1);
				
				//Space-Filling. Every cell must be adj to a path cell
				int[] clause = new int[adj.size()];
				for(int j = 0; j < clause.length; j++) {
					clause[j] = adj.get(j);
				}
				ret.addClause(clause);
				
				addPartOfPathMeansTwoNeighbors(k,i,ret,adj);
				
//				//No squares, which are cheating
//				if(validAdjacent(k,i,1,0) && validAdjacent(k,i,0,1)
//						&& validAdjacent(k,i,1,1)) {
//					ret.addClause(-getPathVar(k,i),-getPathVar(k+1,i),-getPathVar(k,i+1),-getPathVar(k+1,i+1));
//					
//				}
				

			}
		}

		return ret;
	}

	private void addPartOfPathMeansTwoNeighbors(int x, int y, CNF ret, List<Integer> adj) {

		//If x,y is part of a path, needs at least 2 neighbors that are paths
		if(adj.size() == 2) {
			//Never 1 neighber, if have two need both
			ret.addClause(-getPathVar(x,y),adj.get(0));
			ret.addClause(-getPathVar(x,y),adj.get(1));

		} else {
			//adj.size >= 3
			for(int k = 0; k < adj.size(); k++) {
				int[] clause = new int[adj.size()];
				for(int i = 0; i < adj.size(); i++) {
					if(i == k) {
						//Will be sorted later
						//Just use this space because it is convenient
						clause[i] = -getPathVar(x,y);
					} else {
						clause[i] = adj.get(i);	
					}
				}
				//clause sorted on add
				ret.addClause(clause);
			}


			NChooseRGenerator ncr = new NChooseRGenerator(adj.size(),3);

			while(ncr.hasMore()) {
				int[] toAdd = new int[3];
				int index = 0;
				int[] next = ncr.getNext();

				for(int var : next) {
					toAdd[index] = -adj.get(var);
					index++;
				}

				ret.addClause(toAdd);
			}
		}
	}

	private void addAdIfValid(int x, int y, ArrayList<Integer> adj, int dx, int dy) {
		if(validAdjacent(x,y,dx,dy)) {
			adj.add(getPathVar(x+dx,y+dy));
		}
	}

	private boolean validAdjacent(int x, int y,int dx,int dy) {
		return x >= 0 && y >= 0 && x < length && y < height && !((dx == 0) && (dy == 0))
				&& (x + dx) >= 0 && (y + dy) >= 0 && (x + dx) < length && (y + dy) < height;
	}

	private int getPathVar(int x, int y) {
		return y*length + x + 1;
	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int k = 0; k < height; k++) {
			for(int i = 0; i < length; i++) {
				int var = getPathVar(i,k);
				
				if(model[var-1] > 0) {
					sb.append('X');
				} else {
					sb.append(' ');
				}
			}
			sb.append(newline);
		}
		
		return sb.toString();

	}

	@Override
	public String toString() {
		return "SpaceCyc("+length+","+height+")";
	}






}
