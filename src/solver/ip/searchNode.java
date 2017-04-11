package solver.ip;

import java.util.HashMap;

public class searchNode {
	  HashMap<Integer, Integer> partialAssigned = new HashMap<Integer, Integer>();
	  
	  public searchNode() {
		  super();
	  }
	  public searchNode(searchNode makeCopy) {
		  for (int test: makeCopy.partialAssigned.keySet()) {
		    	this.partialAssigned.put(test, makeCopy.partialAssigned.get(test));
		    }
		  }

}
