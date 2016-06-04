package s0549296;

public class Edge2 {
	private Node2 to;
	private float weight;
	
	public Edge2(Node2 toNode, float weight){
		this.to=toNode;
		this.weight=weight;
	}

	public Node2 getTo() {
		return to;
	}

	public float getWeight() {
		return weight;
	}
}
