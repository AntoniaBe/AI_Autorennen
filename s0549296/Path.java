package s0549296;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.lwjgl.util.vector.Vector2f;

public class Path {
	
	private ArrayList<Vector2f> path;
	private ArrayList<Node> nodePath;
	private Graph graph;
	private static final float MAX_SEGMENT_LENGTH = 35f;
	
	public Path(Graph graph){
		this.graph = graph;
	}
	/**
	 * Uses the A* algorithm to calculate the shortest path from A to B
	 */
	public void findShortestPath(Vector2f start, Vector2f end){
		//Initialize start and end node
		path = new ArrayList<Vector2f>();
		nodePath = new ArrayList<Node>();
		Node startN = new Node(start);
		Node endN = new Node(end);
		startN.setEstimatedTotalCost(calcHeuristic(startN, endN));
		startN.setCurrentCost(0);
		endN.setEstimatedTotalCost(0);
		
		createEdges(graph.getNodes(), startN, endN);
		
		//Überprüfe Sortierung, Head = möglichst klein
		ArrayList<Node> openList = new ArrayList<Node>();
		openList.add(startN);
		List<Node> closedList = new LinkedList<Node>();
		
		Node current = null;
		while(!openList.isEmpty()){
			current = openList.get(searchMinimum(openList));
			if(current==endN){
				break;
			}
			LinkedList<Edge2> edges = current.getAdjList();
			for(Edge2 e : edges){
				Node endNode = e.getTo();
				endNode.setCurrentCost(current.getCurrentCost()+e.getWeight());
				//To-Node is already in closed, test if the calculated new cost is smaller than the already set cost
				if(closedList.contains(endNode)){
					int index = closedList.indexOf(endNode);
					Node endNodeRecord = closedList.get(index);
					if(endNodeRecord.getCurrentCost() <= endNode.getCurrentCost())
						continue;
					closedList.remove(index);
					endNode.setEstimatedTotalCost(endNodeRecord.getEstimatedTotalCost());
					endNode.setBefore(current);
					openList.add(endNode);
					//To-Node ist already in Open, test if the calculated new cost is smaller than the already set cost
				}else if (openList.contains(endNode)){
					int index = openList.indexOf(endNode);
					Node endNodeRecord = openList.get(index);
					if(endNodeRecord.getCurrentCost() <= endNode.getCurrentCost())
						continue;
					endNode.setEstimatedTotalCost(endNodeRecord.getEstimatedTotalCost());
					endNode.setBefore(current);
				}else{
					endNode.setBefore(current);
					endNode.setEstimatedTotalCost(endNode.getCurrentCost() + calcHeuristic(endNode, endN));
					if(!openList.contains(endNode)){
						openList.add(endNode);
					}
					
				}
			}
			openList.remove(openList.indexOf(current));
			closedList.add(current);
		}
		
		if(current!=endN){
			path=null;
			nodePath = null;
		}else{
			while(current!=startN){
				path.add(new Vector2f(current.getPoint().x, current.getPoint().y));
				nodePath.add(current);
				current = current.getBefore();
			}
			if(!path.isEmpty())
			path.add(new Vector2f(current.getPoint().x, current.getPoint().y));
			path = reversePath();
			nodePath = reverseNodePath();
		}
	}
	
	public ArrayList<Vector2f> betterPath(ArrayList<Vector2f> path){
		ArrayList<Vector2f> newPath = new ArrayList<Vector2f>();
		for(int i = 0; i+1< path.size(); i++){
			newPath.add(path.get(i));
			Vector2f rv = Vector2f.sub(path.get(i+1), path.get(i), null);
			newPath = divideSegment(newPath, rv);
		}
		newPath.add(path.get(path.size()-1));
		return newPath;
	}
	
	private ArrayList<Vector2f> divideSegment(ArrayList<Vector2f> newPath, Vector2f rv) {
		int index = 0;
		while(rv.length()>MAX_SEGMENT_LENGTH){
			rv = (Vector2f) rv.scale(0.5f);
			index++;
		}
		int num = 0;
		for(int i = 0; i < index; i++){
			num += Math.pow(2, i);
		}
		for(int i = 0; i< num; i++){
			newPath.add(Vector2f.add(newPath.get(newPath.size()-1), rv, null));
		}
		return newPath;
	}
	
	private ArrayList<Vector2f> reversePath(){
		Stack<Vector2f> stack = new Stack<Vector2f>();
		ArrayList<Vector2f> reversedPath = new ArrayList<Vector2f>(); 
		for(int i = 0; i<path.size(); i++){
			stack.push(path.get(i));
		}
		while(!stack.isEmpty()){
			reversedPath.add(stack.pop());
		}
		return reversedPath;
	}
	
	private ArrayList<Node> reverseNodePath(){
		Stack<Node> stack = new Stack<Node>();
		ArrayList<Node> reversedPath = new ArrayList<Node>(); 
		for(int i = 0; i<nodePath.size(); i++){
			stack.push(nodePath.get(i));
		}
		while(!stack.isEmpty()){
			reversedPath.add(stack.pop());
		}
		return reversedPath;
	}
	
	private int searchMinimum(ArrayList<Node> openList){
		int index = -1;
		float min = Float.MAX_VALUE;
		for(int i = 0; i<openList.size(); i++){
			float cost = openList.get(i).getEstimatedTotalCost();
			if(cost<min){
				min=cost;
				index = i;
			}
		}
		return index;
	}
	
	private float calcHeuristic(Node currentN, Node endN){
		Vector2f current = currentN.getPoint();
		Vector2f end = endN.getPoint();
		Vector2f rv = Vector2f.sub(end, current, null);
		return rv.length();
	}
	
	private void createEdges(ArrayList<Node> nodes, Node startN, Node endN){
		for(int i = 0; i<nodes.size(); i++){
			Node currentN = nodes.get(i);
			if(!graph.intersectsObstacle(startN, currentN)&& !graph.intersectsSlowZone(startN, currentN)){
				float weight = Vector2f.sub(currentN.getPoint(), startN.getPoint(), null).length() 
						+ graph.calcAdditionalWeight(startN, currentN, graph.getSlowZoneIntersections(startN, currentN)) 
						- graph.calcLessWeight(startN, currentN, graph.getFastZoneIntersections(startN, currentN));
				startN.addNode(new Edge2(currentN, weight));
			}
			
			if(!graph.intersectsObstacle(currentN, endN)&& !graph.intersectsSlowZone(currentN, endN)){
				float weight = Vector2f.sub(endN.getPoint(), currentN.getPoint(), null).length()
						+ graph.calcAdditionalWeight(currentN, endN, graph.getSlowZoneIntersections(endN, currentN)) 
						- graph.calcLessWeight(currentN, endN, graph.getFastZoneIntersections(endN, currentN));
				currentN.addNode(new Edge2(endN, weight));
			}
		}
	}
	
	public ArrayList<Vector2f> getPath(){
		return path;
	}
	
	public ArrayList<Node> getNodePath(){
		return nodePath;
	}
}
