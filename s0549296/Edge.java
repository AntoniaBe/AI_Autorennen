package s0549296;

public class Edge {
	private Node to;
	private int weight;
	
	public Edge(Node to, int weight){
		this.to=to;
		this.weight=weight;
	}

	public Node getTo() {
		return to;
	}

	public int getWeight() {
		return weight;
	}
}
