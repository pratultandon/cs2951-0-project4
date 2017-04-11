package solver.ip;

import ilog.concert.IloException;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map.Entry;

public class BNBSearch {
	
	Deque<searchNode> searchStack = new ArrayDeque<searchNode>();
	double bestVal = Double.MAX_VALUE;
	searchNode currentBest = new searchNode();
	IPInstance solveRel = new IPInstance(); 
	Deque<searchNode> visited = new ArrayDeque<searchNode>();
	
	public BNBSearch(IPInstance solveRel) {
		this.solveRel = solveRel;
		searchNode rootNode = new searchNode();
		this.searchStack.push(rootNode);
	};

	public int solveIP() throws IloException {
		while (!this.searchStack.isEmpty()) {
			searchNode branchNode = this.searchStack.pop();
			boolean isVisited = false;
			for (searchNode tempNode: this.visited) {
				if (branchNode.partialAssigned.equals(tempNode.partialAssigned)){
					isVisited = true;
					break;
				}
			}
			
			if (!isVisited) {
				visited.add(new searchNode(branchNode));
				double bound = solveRel.solve(branchNode);
				if (bound > bestVal || bound == 0) {
					//System.out.println(searchStack.size());
				}
				if (bound < bestVal && bound!=0) {
					if (branchNode.partialAssigned.size() == solveRel.numTests) {
						//System.out.println("YAY");
						this.bestVal = bound; 
						this.currentBest = branchNode;
					}
				}
				if (bound < bestVal && bound!=0) {
					if (branchNode.partialAssigned.size() != solveRel.numTests) {
						int varToSplit = getSplitVariable(branchNode);
						if (varToSplit != -1) {
							searchNode left = new searchNode(branchNode);
							searchNode right = new searchNode(branchNode);
							left.partialAssigned.put(varToSplit, 0);
							right.partialAssigned.put(varToSplit, 1);
							this.searchStack.push(left);
							this.searchStack.push(right);
						}
						
					}
				}
			}
		}
		return (int) bestVal;
	}
	
	public int getSplitVariable(searchNode node) throws IloException {
	for (int i = 0; i < solveRel.variableOrder.size(); i++) {
			if (!(node.partialAssigned.containsKey(solveRel.variableOrder.get(i)))){
				return solveRel.variableOrder.get(i);
			}
		}
		return -1;
	}
	
}
