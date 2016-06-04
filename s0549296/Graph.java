package s0549296;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;

public class Graph {
	private ArrayList<Node2> nodes;
public ArrayList<Node2> getNodes() {
		return nodes;
	}

	public void addNode(Node2 node) {
		nodes.add(node);
	}

	public ArrayList<Line2D> getObstacles() {
		return obstacles;
	}
	//	private Area map;
//	private Polygon[] polys;
	private ArrayList<Line2D> obstacles;
	
	
	public Graph(Polygon[] polys, Area map){
//		this.map = map;
//		this.polys = polys;
		
		nodes = new ArrayList<Node2>();
		obstacles = new ArrayList<Line2D>();
		
		//Iteriere über alle Hindernisse
		for(int i=0; i< polys.length; i++){
			System.out.println("Polygon:" + i);
			Polygon poly = polys[i];
			int[] xCoords = poly.xpoints;
			int[] yCoords = poly.ypoints;
			
			//Fülle alle Eckpunkte des Hindernisses in ein Vektor Array
			Vector2f[] points = new Vector2f[poly.npoints];
			for(int p=0; p<poly.npoints; p++){
				points[p] = new Vector2f(xCoords[p], yCoords[p]);
			}
			addNodes(points, map);
		}
		
		//Hinzufügen der Knoten in Array nodes
		addEdges();
	}
	
	/**
	 * Den erstellten Knoten werden hier Kanten zu anderen Knoten (max. 4 Stck.) hinzugefügt,
	 * Kanten werden nur zu befahrbaren Knoten erstellt, oder mit einem sehr großen Kantengewicht
	 * z.B. Integer.MAX_VALUE
	 */
	private void addEdges(){
		for(int i = 0 ; i<nodes.size(); i++){
			Node2 currentNode = nodes.get(i);
			for(int j=0; j<nodes.size(); j++){
				if(j==i)
					continue;
				Node2 toNode = nodes.get(j);
				if(isFreespace(currentNode, toNode)){
					float weight = Vector2f.sub(toNode.getPoint(), currentNode.getPoint(), null).length();
					currentNode.addNode(new Edge2(toNode, weight));
				}
			}
		}
	}
	
	public boolean isFreespace(Node2 from, Node2 to){
		Line2D[] lines = createParallelLines(from, to);
		for(int i=0; i<obstacles.size(); i++){
			if(obstacles.get(i).intersectsLine(lines[0])||obstacles.get(i).intersectsLine(lines[1])|obstacles.get(i).intersectsLine(lines[2]))
				return false;
		}
		return true;
	}
	/**
	 * Creates the needed lines to check if the drive space is free
	 */
	private Line2D[] createParallelLines(Node2 from, Node2 to){
		Vector2f sv = from.getPoint();
		Vector2f rvTo = to.getPoint();
		Vector2f rv = Vector2f.sub(rvTo, sv, null);
		Vector2f orthoRV = new Vector2f(-rv.y, rv.x).normalise(null);
		
		
		//Skaliere orthoRV
		orthoRV = (Vector2f) orthoRV.scale(5);
		
		Line2D line = new Line2D.Float(sv.x, sv.y, rvTo.x, rvTo.y);
		Line2D para1 = new Line2D.Float((float)sv.x+orthoRV.x, (float)sv.y+orthoRV.y, (float)rvTo.x+orthoRV.x, (float)rvTo.y+orthoRV.y);
		Line2D para2 = new Line2D.Float((float)sv.x-orthoRV.x, (float)sv.y-orthoRV.y, (float)rvTo.x-orthoRV.x, (float)rvTo.y-orthoRV.y);
		
		Line2D[] result = {line, para1, para2};
		return result;
	}
	/**
	 * Fügt alle Knoten zur PriorityQueue hinzu
	 * TODO: Später ggf. Hinternisfelder entfernen
	 */
	private void addNodes(Vector2f[] points, Area map){
		//Prüfe auf Konvexität
		for(int p = 0;  p< points.length; p++){
			Vector2f currentP = points[p];
			Vector2f previousP;
			if(p!=0){
				previousP = points[p-1];
			}else{
				previousP = points[points.length-1];
			}
			Vector2f nextP;
			if(p!=points.length-1){
				nextP = points[p+1];
			}else{
				nextP = points[0];
			}
			Vector2f toC = Vector2f.sub(currentP, previousP, null);
			Vector2f fromC = Vector2f.sub(nextP, currentP, null);
			//Fülle ArrayList Hindernisse mit Linien der Hindernisränder
			obstacles.add(new Line2D.Float(currentP.x, currentP.y, nextP.x, nextP.y));
			
			
			//->CurrentP ist konvex und kann dem Graphen hinzugefügt werden
			if(pointIsLeft(fromC, currentP, previousP)){
				//RV von current zu Ecken davor und dahinter, normalisiert
				Vector2f toNext = fromC.normalise(null);
				Vector2f toPrevious = toC.negate(null).normalise(null);
				
				Vector2f mp = Vector2f.add(Vector2f.add(currentP, toNext, null),(Vector2f) Vector2f.sub(toPrevious, toNext, null).scale(0.5f), null);
				Vector2f toMp = Vector2f.sub(mp, currentP, null).normalise(null).negate(null);
				
				//Lege den Abstand zum Hindernis durch Skalierung fest
				float scaleFactor = 20;
				Vector2f newPoint = Vector2f.add(currentP, (Vector2f) toMp.scale(scaleFactor), null);
				if(!map.contains(new Point2D.Float(newPoint.x, newPoint.y))){
				//Lege den Knoten im Graphen an
				nodes.add(new Node2(newPoint));
				}
				
			}
			
		}
	}
	private boolean pointIsLeft(Vector2f rv, Vector2f sVector, Vector2f testPoint){
		Vector2f sVtoTP = Vector2f.sub(testPoint, sVector, null);
		Vector2f orthoRV = new Vector2f(-rv.y, rv.x);
		float scalar = Vector2f.dot(sVtoTP, orthoRV);
		if(scalar>=0){
			return true;
		}else{
			return false;
		}
	}
}
