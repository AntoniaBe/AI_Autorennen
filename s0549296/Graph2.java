package s0549296;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;

public class Graph2 {

	private ArrayList<Node> nodes;
	private ArrayList<Line2D> obstacles;
	private ArrayList<Line2D> sZones;
	private ArrayList<Line2D> fZones;

	public Graph2(Polygon[] obst, Area map, Polygon[][] zones, Area szMap) {

		nodes = new ArrayList<Node>();
		obstacles = new ArrayList<Line2D>();
		sZones = new ArrayList<Line2D>();
		fZones = new ArrayList<Line2D>();

		// Iteriere über alle Hindernisse
		for (int i = 0; i < obst.length; i++) {
			// System.out.println("Polygon:" + i);
			Polygon o = obst[i];
			int[] xCoords = o.xpoints;
			int[] yCoords = o.ypoints;

			// Fülle alle Eckpunkte des Hindernisses in ein Vektor Array
			Vector2f[] points = new Vector2f[o.npoints];
			for (int p = 0; p < o.npoints; p++) {
				points[p] = new Vector2f(xCoords[p], yCoords[p]);
			}
			addNodes(points, map);
		}

		// Zonenbearbeitung
		// Slowzone!!!!
		for(int j=0; j<zones.length; j++){
			for (int i = 0; i < zones[j].length; i++) {
				Polygon sZone = zones[j][i];
				int[] xCoords = sZone.xpoints;
				int[] yCoords = sZone.xpoints;

				Vector2f[] points = new Vector2f[sZone.npoints];
				for (int p = 0; p < sZone.npoints; p++) {
					points[p] = new Vector2f(xCoords[p], yCoords[p]);
				}
				//für SlowZone
				if(j==0)
					addNodesSpecial(points, map, false);
				//Für fastzone
				else
					addNodesSpecial(points, map, true);
				
			}
		}
		

		// Hinzufügen der Knoten in Array nodes
		addEdges();

	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public ArrayList<Line2D> getObstacles() {
		return obstacles;
	}

	private void addNodesSpecial(Vector2f[] points, Area map, boolean slow) {
		for (int p = 0; p < points.length; p++) {
			Vector2f currentP = points[p];
			Vector2f previousP;
			if (p != 0) {
				previousP = points[p - 1];
			} else {
				previousP = points[points.length - 1];
			}
			Vector2f nextP;
			if (p != points.length - 1) {
				nextP = points[p + 1];
			} else {
				nextP = points[0];
			}
			
			//Füge Ränder von Slow bzw Fastzones hinzu
			if(!slow)
				fZones.add(new Line2D.Float(currentP.x, currentP.y, nextP.x, nextP.y));
			else
				sZones.add(new Line2D.Float(currentP.x, currentP.y, nextP.x, nextP.y));

			// RV von current zu Ecken davor und dahinter, normalisiert
			Vector2f toNext = Vector2f.sub(nextP, currentP, null).normalise(null);
			Vector2f toPrevious = Vector2f.sub(currentP, previousP, null).negate(null).normalise(null);

			Vector2f mp = Vector2f.add(Vector2f.add(currentP, toNext, null),
					(Vector2f) Vector2f.sub(toPrevious, toNext, null).scale(0.5f), null);
			// Negieren wenn Punkt außerhalb, für innerhalb ohne negieren
			Vector2f toMp = Vector2f.sub(mp, currentP, null).normalise(null);

			float scaleFactor = 20;
			Vector2f newPoint = Vector2f.add(currentP, (Vector2f) toMp.negate(null).scale(scaleFactor), null);
			if (!map.contains(newPoint.x, newPoint.y)) {
				// Lege den Knoten im Graphen an
				nodes.add(new Node(newPoint));
			} else {
				Vector2f altPoint = Vector2f.add(currentP, (Vector2f) toMp.scale(scaleFactor), null);
				if (!map.contains(altPoint.x, altPoint.y)) {
					// Lege den Knoten im Graphen an
					nodes.add(new Node(altPoint));
				}
			}
		}
	}

	/**
	 * Fügt alle Knoten zur PriorityQueue hinzu TODO: Später ggf.
	 * Hinternisfelder entfernen
	 */
	private void addNodes(Vector2f[] points, Area map) {
		// Prüfe auf Konvexität
		for (int p = 0; p < points.length; p++) {
			Vector2f currentP = points[p];
			Vector2f previousP;
			if (p != 0) {
				previousP = points[p - 1];
			} else {
				previousP = points[points.length - 1];
			}
			Vector2f nextP;
			if (p != points.length - 1) {
				nextP = points[p + 1];
			} else {
				nextP = points[0];
			}
			Vector2f toC = Vector2f.sub(currentP, previousP, null);
			Vector2f fromC = Vector2f.sub(nextP, currentP, null);
			// Fülle ArrayList Hindernisse mit Linien der Hindernisränder
			obstacles.add(new Line2D.Float(currentP.x, currentP.y, nextP.x, nextP.y));

			// ->CurrentP ist konvex und kann dem Graphen hinzugefügt werden
			if (pointIsLeft(fromC, currentP, previousP)) {
				// RV von current zu Ecken davor und dahinter, normalisiert
				Vector2f toNext = fromC.normalise(null);
				Vector2f toPrevious = toC.negate(null).normalise(null);

				Vector2f mp = Vector2f.add(Vector2f.add(currentP, toNext, null),
						(Vector2f) Vector2f.sub(toPrevious, toNext, null).scale(0.5f), null);
				Vector2f toMp = Vector2f.sub(mp, currentP, null).normalise(null).negate(null);

				// Lege den Abstand zum Hindernis durch Skalierung fest
				float scaleFactor = 20;
				Vector2f newPoint = Vector2f.add(currentP, (Vector2f) toMp.scale(scaleFactor), null);
				if (!map.contains(newPoint.x, newPoint.y)) {
					// Lege den Knoten im Graphen an
					nodes.add(new Node(newPoint));
				}

			}

		}
	}

	/**
	 * Den erstellten Knoten werden hier Kanten zu anderen Knoten (max. 4 Stck.)
	 * hinzugefügt, Kanten werden nur zu befahrbaren Knoten erstellt, oder mit
	 * einem sehr großen Kantengewicht z.B. Integer.MAX_VALUE Edited: Checks for
	 * intersecting slowzone
	 */
	private void addEdges() {
		for (int i = 0; i < nodes.size(); i++) {
			Node currentNode = nodes.get(i);
			for (int j = 0; j < nodes.size(); j++) {
				if (j == i)
					continue;
				Node toNode = nodes.get(j);
				if (isFreespace(currentNode, toNode)) {
					float weight;
					ArrayList<Vector2f> intersections = getSlowZoneIntersections(currentNode, toNode);
					if (intersections.isEmpty())
						weight = Vector2f.sub(toNode.getPoint(), currentNode.getPoint(), null).length();
					else
						weight = Vector2f.sub(toNode.getPoint(), currentNode.getPoint(), null).length()*4;

					currentNode.addNode(new Edge2(toNode, weight));
				}
			}
		}
	}

	public ArrayList<Vector2f> getSlowZoneIntersections(Node from, Node to) {
		ArrayList<Vector2f> intersections = new ArrayList<Vector2f>();
		Line2D line = new Line2D.Float(from.getPoint().x, from.getPoint().y, to.getPoint().x, to.getPoint().y);
		for (int i = 0; i < sZones.size(); i++) {
			if (sZones.get(i).intersectsLine(line)){
				Vector2f point = lineLineIntersection(sZones.get(i), line);
				if(line.contains(point.x, point.y) & sZones.get(i).contains(point.x, point.y)) intersections.add(point);
			}
		}
		return intersections;
	}
	
	public Vector2f lineLineIntersection(Line2D a, Line2D b){
		float x1 = (float) a.getX1();
		float x2 = (float) a.getX2();
		float y1 = (float) a.getY1();
		float y2 = (float) a.getY2();
		float x3 = (float) b.getX1();
		float x4 = (float) b.getX2();
		float y3 = (float) b.getY1();
		float y4 = (float) b.getY2();
		
		float x = ((x1*y2 - y1*x2)*(x3 - x4) - (x1 - x2)*(x3*y4 - y3*x4))/((x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4));
		float y = ((x1*y2 - y1*x2)*(y3 - y4) - (y1 - y2)*(x3*y4 - y3*x4))/((x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4));
		
		return new Vector2f(x,y);
	}

	public boolean isFreespace(Node from, Node to) {
		Line2D[] lines = createParallelLines(from, to);
		for (int i = 0; i < obstacles.size(); i++) {
			if (obstacles.get(i).intersectsLine(lines[0])
					|| obstacles.get(i).intersectsLine(lines[1]) | obstacles.get(i).intersectsLine(lines[2]))
				return false;
		}
		return true;
	}

	/**
	 * Creates the needed lines to check if the drive space is free
	 */
	private Line2D[] createParallelLines(Node from, Node to) {
		Vector2f sv = from.getPoint();
		Vector2f rvTo = to.getPoint();
		Vector2f rv = Vector2f.sub(rvTo, sv, null);
		Vector2f orthoRV = new Vector2f(-rv.y, rv.x).normalise(null);

		// Skaliere orthoRV
		orthoRV = (Vector2f) orthoRV.scale(5);

		Line2D line = new Line2D.Float(sv.x, sv.y, rvTo.x, rvTo.y);
		Line2D para1 = new Line2D.Float((float) sv.x + orthoRV.x, (float) sv.y + orthoRV.y, (float) rvTo.x + orthoRV.x,
				(float) rvTo.y + orthoRV.y);
		Line2D para2 = new Line2D.Float((float) sv.x - orthoRV.x, (float) sv.y - orthoRV.y, (float) rvTo.x - orthoRV.x,
				(float) rvTo.y - orthoRV.y);

		Line2D[] result = { line, para1, para2 };
		return result;
	}

	private boolean pointIsLeft(Vector2f rv, Vector2f sVector, Vector2f testPoint) {
		Vector2f sVtoTP = Vector2f.sub(testPoint, sVector, null);
		Vector2f orthoRV = new Vector2f(-rv.y, rv.x);
		float scalar = Vector2f.dot(sVtoTP, orthoRV);
		if (scalar >= 0) {
			return true;
		} else {
			return false;
		}
	}
}
