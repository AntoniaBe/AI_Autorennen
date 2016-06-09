package s0549296;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.lwjgl.util.vector.Vector2f;

public class Path {
	
	private ArrayList<Vector2f> path;
	private Graph graph;
	private static final float MAX_SEGMENT_LENGTH = 35f;
	
	public Path(){
		
	}
	/**
	 * Uses the A* algorithm to calculate the shortest path from A to B
	 */
	public void findShortestPath(Graph graph, Vector2f start, Vector2f end){
		//Initialize start and end node
		path = new ArrayList<Vector2f>();
		this.graph = graph;
		Node2 startN = new Node2(start);
		Node2 endN = new Node2(end);
		startN.setEstimatedTotalCost(calcHeuristic(startN, endN));
		startN.setCurrentCost(0);
		endN.setEstimatedTotalCost(0);
		createEdges(graph.getNodes(), startN, endN);
		
		//Überprüfe sortierung, Head = möglichst klein
		ArrayList<Node2> openList = new ArrayList<Node2>();
		openList.add(startN);
		List<Node2> closedList = new LinkedList<Node2>();
		
		Node2 current = null;
		while(!openList.isEmpty()){
			current = openList.get(searchMinimum(openList));
			if(current==endN){
				break;
			}
			LinkedList<Edge2> edges = current.getAdjList();
			for(Edge2 e : edges){
				Node2 endNode = e.getTo();
				endNode.setCurrentCost(current.getCurrentCost()+e.getWeight());
				//To-Node is already in closed, test if the calculated new cost is smaller than the already set cost
				if(closedList.contains(endNode)){
					int index = closedList.indexOf(endNode);
					Node2 endNodeRecord = closedList.get(index);
					if(endNodeRecord.getCurrentCost() <= endNode.getCurrentCost())
						continue;
					closedList.remove(index);
					endNode.setEstimatedTotalCost(endNodeRecord.getEstimatedTotalCost());
					endNode.setBefore(current);
					openList.add(endNode);
					//To-Node ist already in Open, test if the calculated new cost is smaller than the already set cost
				}else if (openList.contains(endNode)){
					int index = openList.indexOf(endNode);
					Node2 endNodeRecord = openList.get(index);
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
		}else{
			while(current!=startN){
				path.add(new Vector2f(current.getPoint().x, current.getPoint().y));
				current = current.getBefore();
			}
			path = reversePath();
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
	
	private int searchMinimum(ArrayList<Node2> openList){
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
	
	private float calcHeuristic(Node2 currentN, Node2 endN){
		Vector2f current = currentN.getPoint();
		Vector2f end = endN.getPoint();
		Vector2f rv = Vector2f.sub(end, current, null);
		return rv.length();
	}
	
	private void createEdges(ArrayList<Node2> nodes, Node2 startN, Node2 endN){
		for(int i = 0; i<nodes.size(); i++){
			Node2 currentN = nodes.get(i);
			if(graph.isFreespace(startN, currentN)){
				float weight = Vector2f.sub(currentN.getPoint(), startN.getPoint(), null).length();
				startN.addNode(new Edge2(currentN, weight));
			}
			
			if(graph.isFreespace(currentN, endN)){
				float weight = Vector2f.sub(endN.getPoint(), currentN.getPoint(), null).length();
				currentN.addNode(new Edge2(endN, weight));
			}
		}
	}
	
	public ArrayList<Vector2f> getPath(){
		return path;
	}
}
