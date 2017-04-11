package solver.ip;

import ilog.cplex.*;
import ilog.concert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class IPInstance
{
  // IBM Ilog Cplex Solver 
  IloCplex cplex;
	
  int numTests;			// number of tests
  int numDiseases;		// number of diseases
  double[] costOfTest;  // [numTests] the cost of each test
  int[][] A;            // [numTests][numDiseases] 0/1 matrix if test is positive for disease
  List <Integer> variableOrder;
  
  public IPInstance()
  {
	super();
  }
      
  void init(int numTests, int numDiseases, double[] costOfTest, int[][] A)
  {
    assert(numTests >= 0) : "Init error: numtests should be non-negative " + numTests;
    assert(numDiseases >= 0) : "Init error: numtests should be non-negative " + numTests;
    assert(costOfTest != null) : "Init error: costOfTest cannot be null";
    assert(costOfTest.length == numTests) : "Init error: costOfTest length differ from numTests" + costOfTest.length + " vs. " + numTests;
    assert(A != null) : "Init error: A cannot be null";
    assert(A.length == numTests) : "Init error: Number of rows in A differ from numTests" + A.length + " vs. " + numTests;
    assert(A[0].length == numDiseases) : "Init error: Number of columns in A differ from numDiseases" + A[0].length + " vs. " + numDiseases;
    
    this.numTests = numTests;
    this.numDiseases = numDiseases;
    this.costOfTest = new double[numTests];
    this.A = new int[numTests][numDiseases];
    for(int i=0; i < numTests; i++)
      for(int j=0; j < numDiseases; j++)
        this.A[i][j] = A[i][j];
    List<Double> costList = new ArrayList<Double>();
    for(int i=0; i < numTests; i++) {
        this.costOfTest[i] = costOfTest[i];
        costList.add(costOfTest[i]*heuristic(i)*-1);
    }

    
    // Just pass in the list to have its indexes sorted by the natural ordering
    this.variableOrder = sortIndex(costList);
    		
  }
  
  public static <T extends Comparable<T>> List<Integer> sortIndex(final List<T> in) {
	    ArrayList<Integer> index = new ArrayList<>();
	    for (int i = 0; i < in.size(); i++) {
	        index.add(i);
	    }

	    Collections.sort(index, new Comparator<Integer>() {
	        @Override
	        public int compare(Integer idx1, Integer idx2) {
	            return in.get(idx1).compareTo(in.get(idx2));
	        }
	    });

	    return index;
	}
  
  public String toString()
  {
	StringBuffer buf = new StringBuffer();
	buf.append("Number of tests: " + numTests + "\n");
	buf.append("Number of diseases: " + numDiseases + "\n");
	buf.append("Cost of tests: " + Arrays.toString(costOfTest) + "\n");
	buf.append("Variable Order: " + (variableOrder.toString()));
	buf.append("A:\n");
	for(int i=0; i < numTests; i++)
		buf.append(Arrays.toString(A[i]) + "\n");
	return buf.toString();
  }
  
  public double heuristic (int test) {
	 
	  double hVal = 0;
	  for (int j = 0; j < numDiseases; j++) {
		  	hVal += Math.abs(this.A[test][j]);
		  }
	 hVal = 1-2*Math.abs(0.5-hVal/numDiseases);
  	return hVal;
  }
  
  public double solve(searchNode node) throws IloException {
	  
	  try {
		  cplex = new IloCplex();
		  cplex.setOut(null);
		  
		  IloNumVarType [] switcher = new IloNumVarType[2];
		  switcher[0] = IloNumVarType.Int;
		  switcher[1] = IloNumVarType.Float;
		  int flag = 1;
		  
	      IloNumVar[] testUsed = cplex.numVarArray(numTests, 0, 1, switcher[flag]);
	      
	      IloNumExpr objectiveFunction = cplex.numExpr();	
	      objectiveFunction = cplex.scalProd(testUsed, costOfTest);
	      
	      cplex.addMinimize(objectiveFunction);

	      for (int j = 0; j < numDiseases*numDiseases; j++) {
	    	  if (j % numDiseases == j /numDiseases) {
	    		  continue;
	    	  }
	    	  
	    	  IloNumExpr diffConstraint = cplex.numExpr();
	    	  
	    	  for (int i =  0; i < numTests; i++) {
	    		  if (A[i][j/numDiseases] == A[i][j%numDiseases]) {
	    			  continue;
	    		  }
	    		  diffConstraint = cplex.sum(diffConstraint, testUsed[i]); 
	    	  }
	    	  
	    	  cplex.addGe(diffConstraint, 1);
	    	  diffConstraint = cplex.numExpr();

	      }
	      
	      for (int test: node.partialAssigned.keySet()) {
	    	  cplex.addEq(testUsed[test], node.partialAssigned.get(test));
	      }
	      
	      if(cplex.solve()) {
		        double objectiveValue = (cplex.getObjValue()); 
		        
		        for (int i = 0; i < numTests; i ++) {
		        	if (cplex.getValue(testUsed[i]) == 0) {
		        		node.partialAssigned.put(i, 0);
		        	}
		        	else if (cplex.getValue(testUsed[i]) == 1) {
		        		node.partialAssigned.put(i, 1);
		        	}
		        }
		        //System.out.println("LOL"+node.partialAssigned.size());
		       
		        return objectiveValue;
	      }

	      
	  }
	  catch(IloException e) {
	      System.out.println("Error " + e);
	  }
	  return 0;
  }
}