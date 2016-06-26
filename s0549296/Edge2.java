package s0549296;

public class Edge2 {
	private Node to;
	private float weight;
	
	public Edge2(Node toNode, float weight){
		this.to=toNode;
		this.weight=weight;
	}

	public Node getTo() {
		return to;
	}

	public float getWeight() {
		return weight;
	}
}
