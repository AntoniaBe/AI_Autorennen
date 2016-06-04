package s0549296;

import java.util.LinkedList;

import org.lwjgl.util.vector.Vector2f;

public class Node {
	//Werden bei Erstellung gesetzt
	private Vector2f mp;
	private boolean driveable;
	private LinkedList<Edge> adjList;
	
	//Werden dynamisch bei Wegesuche gesetzt
	private int currentCost = Integer.MAX_VALUE;
	private Node before;
	
	public Node(Vector2f mp){
		this.mp = mp;
		adjList = new LinkedList<Edge>();
	}

	public int getCurrentCost() {
		return currentCost;
	}

	public void setCurrentCost(int currentCost) {
		this.currentCost = currentCost;
	}

	public Node getBefore() {
		return before;
	}

	public void setBefore(Node before) {
		this.before = before;
	}
	public Vector2f getMp(){
		return mp;
	}
	public LinkedList<Edge> getAdjList(){
		return this.adjList;
	}
	public void addNode(Edge e){
		adjList.add(e);
	}

	public boolean isDriveable() {
		return driveable;
	}

	public void setDriveable(boolean driveable) {
		this.driveable = driveable;
	}
	
	
}
