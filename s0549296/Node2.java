package s0549296;

import java.util.LinkedList;

import org.lwjgl.util.vector.Vector2f;

public class Node2 implements Comparable<Node2> {
	//Werden bei Erstellung gesetzt
	private Vector2f point;
	private LinkedList<Edge2> adjList;
	private float estimatedTotalCost;
	
	//Werden dynamisch bei Wegesuche gesetzt
		private float currentCost = Integer.MAX_VALUE;
		private Node2 before;
	
	public float getEstimatedTotalCost() {
		return estimatedTotalCost;
	}

	public void setEstimatedTotalCost(float estimatedTotalCost) {
		this.estimatedTotalCost = estimatedTotalCost;
	}
	
	
	public Node2(Vector2f point){
		this.point = point;
		adjList = new LinkedList<Edge2>();
	}

	public float getCurrentCost() {
		return currentCost;
	}

	public void setCurrentCost(float currentCost) {
		this.currentCost = currentCost;
	}

	public Node2 getBefore() {
		return before;
	}

	public void setBefore(Node2 before) {
		this.before = before;
	}
	public Vector2f getPoint(){
		return point;
	}
	public LinkedList<Edge2> getAdjList(){
		return this.adjList;
	}
	public void addNode(Edge2 e){
		adjList.add(e);
	}
	
	 public int compareTo(Node2 otherNode) throws ClassCastException {
		    float otherNodeEstimatedCost = ((Node2) otherNode).getEstimatedTotalCost();  
		    return Math.round(this.estimatedTotalCost - otherNodeEstimatedCost);    
		  }

}
