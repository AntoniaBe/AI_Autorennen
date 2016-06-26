package s0549296;

import java.util.LinkedList;

import org.lwjgl.util.vector.Vector2f;

public class Node implements Comparable<Node> {
	
	private Vector2f point;
	private float estimatedTotalCost;
	private float currentCost = Integer.MAX_VALUE;
	private LinkedList<Edge2> adjList;
	private Node before;

	public float getEstimatedTotalCost() {
		return estimatedTotalCost;
	}

	public void setEstimatedTotalCost(float estimatedTotalCost) {
		this.estimatedTotalCost = estimatedTotalCost;
	}

	public Node(Vector2f point) {
		this.point = point;
		adjList = new LinkedList<Edge2>();
	}

	public float getCurrentCost() {
		return currentCost;
	}

	public void setCurrentCost(float currentCost) {
		this.currentCost = currentCost;
	}

	public Node getBefore() {
		return before;
	}

	public void setBefore(Node before) {
		this.before = before;
	}

	public Vector2f getPoint() {
		return point;
	}

	public LinkedList<Edge2> getAdjList() {
		return this.adjList;
	}

	public void addNode(Edge2 e) {
		adjList.add(e);
	}

	public int compareTo(Node otherNode) throws ClassCastException {
		float otherNodeEstimatedCost = ((Node) otherNode).getEstimatedTotalCost();
		return Math.round(this.estimatedTotalCost - otherNodeEstimatedCost);
	}

}
