package task.formula;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import util.PermutationUtil;
import util.lit.IntToIntLinkedHashMap;
import util.lit.LitsMap;
import util.lit.IntToIntLinkedHashMap.EntryIter;
import util.lit.LitSorter;
import workflow.decoder.RectangleBWPictureDecoder;
import formula.VariableContext;
import formula.simple.CNF;
import group.LiteralPermutation;

public class AllSquaresCNF implements CNFCreator, ConsoleDecodeable,
FileDecodable {
	private final int size;

	public AllSquaresCNF(int size) {
		this.size = size;
	}


	private int getVar(int x, int y) {
		return y*size + x + 1;
	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int x = 0; x< size; x++) {
			for(int y = 0; y < size; y++) {
				int index = getVar(x,y)-1;

				if(model[index] > 0) {
					sb.append('X');
				} else {
					sb.append(' ');
				}
			}

			sb.append(ConsoleDecodeable.newline);
		}

		return sb.toString();
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		File f = new File(dir, filePrefix + ".png");
		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,size,size),"png",f);

	}

	@Override
	public String toString() {
		return "AllFilledSquares="+size;
	}

	private boolean onEdge(int xOry) {
		return (xOry == 0 || xOry == size-1);
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		CNF ret = new CNF(context);
		context.ensureSize(size*size);
		
		int[] atLeastOne = new int[size*size];

		//every cell is either unfilled, isolated, a line or a corner
		for(int x = 0; x < size; x++) {
			for(int y = 0; y < size; y++) {
				int curVar = getVar(x,y);

				atLeastOne[curVar-1] = curVar;
				
				onlyValidStructure(ret, x, y);

				handleIsolated(ret, x, y, curVar);

				singleCornerEachType(ret, x, y);

				ensureEquadistantCorners(ret,x,y);

			}
		}
		
		ret.fastAddClause(atLeastOne);
		ret.sort();
		ret = ret.reduce().trySubsumption();
		
		
		LitsMap<Object> allCl = new LitsMap<Object>(size*size);
		//ensure globs
		LinkedList<int[]> toCompute = new LinkedList<int[]>();
		

		
		for(int[] cl : ret.getClauses()) {
			allCl.put(cl,null);
			toCompute.add(cl);
		}
		
		int[] rotPerm = new int[size*size+1];
		int[] reflPerm = new int[size*size+1];
		
		for(int x2 = 0; x2 < size; x2++) {
			for(int y2 = 0; y2 < size; y2++) {
				int var = getVar(x2,y2);
				int rotVar = getVar(size-1-y2,x2);
				int reflVar = getVar(y2,x2);
				rotPerm[var] = rotVar;
				reflPerm[var] = reflVar;
			}
		}
		
		LiteralPermutation d1 = new LiteralPermutation(rotPerm);
		LiteralPermutation d2 = new LiteralPermutation(reflPerm);
		
		while(!toCompute.isEmpty()) {
			int[] cl = toCompute.poll();
			
			int[] symed = PermutationUtil.permuteClause(cl,rotPerm);
			
			if(!allCl.contains(symed)) {
				toCompute.push(symed);
				ret.fastAddClause(symed);
				allCl.put(symed,null);
			}
			
			symed = PermutationUtil.permuteClause(cl,reflPerm);
			
			if(!allCl.contains(symed)) {
				toCompute.push(symed);
				ret.fastAddClause(symed);
				allCl.put(symed,null);
			}
		}
		
		ret.sort();
		System.out.println(ret.getDeepSize());
	 	ret = ret.trySubsumption();
	 	System.out.println(ret.getDeepSize());
		return ret;
	}


	private void ensureEquadistantCorners(CNF ret, int x, int y) {
		IntToIntLinkedHashMap map = new IntToIntLinkedHashMap();
		if(validPt(x+1,y) && validPt(x,y+1)) {
			//Can be upper-left corner
			
			for(int dist = 1; dist+y < size; dist++) {
				int botLeftY = y+dist;
				int upperRightX = x+dist;
				
				map.clear();
				map.put(-getVar(x,y),0);
				map.put(-getVar(x+1,y),0);
				map.put(-getVar(x,y+1),0);
				
				map.put(-getVar(x,botLeftY),0);
				map.put(-getVar(x+1,botLeftY),0);
				map.put(-getVar(x,botLeftY-1),0);
	
				
				if(validPt(upperRightX,y)) {
					//If this and botLeft, then upper-right
					
					if(!map.contains(-getVar(upperRightX,y))) {
						int[] toAdd = new int[map.size()+1];
						toAdd[0] = getVar(upperRightX,y);
						EntryIter iter = map.getIter();
						int addInd = 1;

						while(iter.hasNext()) {
							toAdd[addInd] = iter.next().getKey();
							addInd++;
						}

						LitSorter.inPlaceSort(toAdd);
						ret.fastAddClause(toAdd);

					}
					
					if(!map.contains(-getVar(upperRightX-1,y))) {
						int[] toAdd = new int[map.size()+1];
						toAdd[0] = getVar(upperRightX-1,y);
						EntryIter iter = map.getIter();
						int addInd = 1;

						while(iter.hasNext()) {
							toAdd[addInd] = iter.next().getKey();
							addInd++;
						}

						LitSorter.inPlaceSort(toAdd);
						ret.fastAddClause(toAdd);
					}
					
					if(!map.contains(-getVar(upperRightX,y+1))) {
						int[] toAdd = new int[map.size()+1];
						toAdd[0] = getVar(upperRightX,y+1);
						EntryIter iter = map.getIter();
						int addInd = 1;

						while(iter.hasNext()) {
							toAdd[addInd] = iter.next().getKey();
							addInd++;
						}

						LitSorter.inPlaceSort(toAdd);
						ret.fastAddClause(toAdd);
					}
					
				} else {
					//bot left corner also invalid

					
					EntryIter iter = map.getIter();
					int[] toAdd = new int[map.size()];
					int addInd = 0;

					while(iter.hasNext()) {
						toAdd[addInd] = iter.next().getKey();
						addInd++;
					}

					LitSorter.inPlaceSort(toAdd);
					ret.fastAddClause(toAdd);
				}
			}
		}
	}


	private void singleCornerEachType(CNF ret, int x, int y) {
		//only one corner of each type
		List<int[]> validCorners1 = getRelativeValidCorners(x,y);

		for(int x2 = 0; x2 < size; x2++) {
			for(int y2 = 0; y2 < size; y2++) {
				if(x2 == x && y2 == y) continue;

				List<int[]> validCorners2 = getRelativeValidCorners(x2,y2);

				IntToIntLinkedHashMap map = new IntToIntLinkedHashMap();
				for(int k = 0; k < validCorners1.size(); k++) {
					int[] corner1 = validCorners1.get(k);
					for(int i = 0; i < validCorners2.size(); i++) {
						int[] corner2 = validCorners2.get(i);
						if(Arrays.equals(corner1,corner2)) {
							map.clear();;
							map.put(-getVar(x,y),0);
							map.put(-getVar(x+corner1[0],y),0);
							map.put(-getVar(x,y+corner1[1]),0);
							map.put(-getVar(x2,y2),0);
							map.put(-getVar(x2+corner2[0],y2),0);
							map.put(-getVar(x2,y2+corner2[1]),0);

							int[] toAdd = new int[map.size()];
							EntryIter iter = map.getIter();
							int addInd = 0;

							while(iter.hasNext()) {
								toAdd[addInd] = iter.next().getKey();
								addInd++;
							}

							LitSorter.inPlaceSort(toAdd);
							ret.fastAddClause(toAdd);
						}
					}
				}
			}
		}
	}


	//WARNING: Confusing (if statement and toAdd not in same order)
	private List<int[]> getRelativeValidCorners(int x, int y) {
		int curVar = getVar(x,y);

		ArrayList<int[]> ret = new ArrayList<int[]>(4);

		if(validPt(x,y-1) && validPt(x-1,y)) {
			int[] toAdd = new int[]{-1,-1};
			ret.add(toAdd);
		}

		if(validPt(x,y+1) && validPt(x-1,y)) {
			int[] toAdd = new int[]{-1,1};
			ret.add(toAdd);
		}

		if(validPt(x,y-1) && validPt(x+1,y)) {
			int[] toAdd = new int[]{1,-1};
			ret.add(toAdd);
		}

		if(validPt(x,y+1) && validPt(x+1,y)) {
			int[] toAdd = new int[]{1,1};
			ret.add(toAdd);
		}

		return ret;
	}


	private void handleIsolated(CNF ret, int x, int y, int curVar) {
		ArrayIntList list = new ArrayIntList(4);
		addIfValid(x,y-1,list);
		addIfValid(x-1,y,list);
		addIfValid(x+1,y,list);
		addIfValid(x,y+1,list);

		//if isolated, everyone else false
		for(int x2 = 0; x2 < size; x2++) {
			for(int y2 = 0; y2 < size; y2++) {
				int var = getVar(x2,y2);
				if(var == curVar || list.contains(var)) {
					continue;
				}

				//isolated implies this cell unfilled
				int[] toAdd = new int[list.size()+2];
				toAdd[0] = -curVar;
				toAdd[1] = -var;

				for(int k = 2; k < toAdd.length; k++) {
					toAdd[k] = list.get(k-2);
				}

				LitSorter.inPlaceSort(toAdd);
				ret.fastAddClause(toAdd);

			}
		}
	}

	private void addIfValid(int x, int y, IntList adj) {
		if(validPt(x, y)) {
			adj.add(getVar(x,y));
		}
	}


	private boolean validPt(int x, int y) {
		return x >= 0 && x < size && y >= 0 && y < size;
	}

	private void onlyValidStructure(CNF ret, int x, int y) {
		if(onEdge(x)) {
			if(onEdge(y)) {
				//corner
				int mid = getVar(x,y);
				int vert = getVar(x,y+ (y == 0 ? 1 : -1));
				int horz = getVar(x+ (x == 0 ? 1 : -1),y);

				//mid and another implies one more
				int[] toAdd = new int[]{-mid,-vert,horz};
				LitSorter.inPlaceSort(toAdd);
				ret.fastAddClause(toAdd);

				toAdd = new int[]{-mid,vert,-horz};
				LitSorter.inPlaceSort(toAdd);
				ret.fastAddClause(toAdd);

			} else {
				//mid and another implies one more
				int top = getVar(x,y-1);
				int mid = getVar(x,y);
				int bot = getVar(x,y+1);
				int other = getVar(x+ (x == 0 ? 1 : -1),y);

				int[] toAdd = new int[]{-mid,-top,bot,other};
				LitSorter.inPlaceSort(toAdd);
				ret.fastAddClause(toAdd);

				toAdd = new int[]{-mid,top,-bot,other};
				LitSorter.inPlaceSort(toAdd);
				ret.fastAddClause(toAdd);

				toAdd = new int[]{-mid,top,bot,-other};
				LitSorter.inPlaceSort(toAdd);
				ret.fastAddClause(toAdd);

				//No more than 3
				toAdd = new int[]{-mid,-top,-bot,-other};

			}
		} else if(onEdge(y)) {
			//mid and another implies one more
			int left = getVar(x-1,y);
			int mid = getVar(x,y);
			int right = getVar(x+1,y);
			int other = getVar(x,y+ (y == 0 ? 1 : -1));

			int[] toAdd = new int[]{-mid,-left,right,other};
			LitSorter.inPlaceSort(toAdd);
			ret.fastAddClause(toAdd);

			toAdd = new int[]{-mid,left,-right,other};
			LitSorter.inPlaceSort(toAdd);
			ret.fastAddClause(toAdd);

			toAdd = new int[]{-mid,left,right,-other};
			LitSorter.inPlaceSort(toAdd);
			ret.fastAddClause(toAdd);

			//No more than 3
			toAdd = new int[]{-mid,-left,-right,-other};
		} else {
			int top = getVar(x,y-1);
			int left = getVar(x-1,y);
			int mid = getVar(x,y);
			int right = getVar(x+1,y);
			int bot = getVar(x,y+1);

			//mid and another implies one more
			ret.fastAddClause(-top,left,-mid,right,bot);
			ret.fastAddClause(top,-left,-mid,right,bot);
			ret.fastAddClause(top,left,-mid,-right,bot);
			ret.fastAddClause(top,left,-mid,right,-bot);


			//No more than 3
			ret.fastAddClause(-top,-left,-mid,-right);
			ret.fastAddClause(-top,-mid,-right,-bot);
			ret.fastAddClause(-top,-left,-right,-bot);
			ret.fastAddClause(-top,-left,-mid,-bot);
		}
	}

}
