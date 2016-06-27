package s0549296;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.lwjgl.util.vector.Vector2f;

import lenz.htw.ai4g.track.Track;

public class Graph {

	private ArrayList<Node> nodes;

	private Polygon[] fastzones;
	private Polygon[] slowzones;
	private Polygon[] obstacles;

	private Area obstacleMap;
	private Area slowMap;
	private Area fastMap;
	private ArrayList<ArrayList<Line2D>> edges;

	private static final float O_SCALE = 20;
	private static final float SZ_SCALE = 20;
	private static final float FZ_SCALE = 10;
	
	private static final float SZ_WEIGHT = 100;
	private static final float FZ_WEIGHT = 0.5f;

	private static final float MIDPOINT_SCALE = 15;

	public Graph(Track t) {
		// Entnehme Track alle wichtigen Elemente für den Graphen
		this.fastzones = t.getFastZones();
		this.slowzones = t.getSlowZones();
		this.obstacles = t.getObstacles();

		// Erstelle Areas zur Schnittprüfung für alle drei Track Elemente
		createAreas();

		// Erstelle Knoten für die einzelnen Zonentypen (Hindernis, langsame und
		// schnelle Zone)
		Polygon[][] trackElements = { obstacles, slowzones, fastzones };
		createNodes(trackElements);

		// Erstelle Kanten zwischen den Graphknoten
		createEdges(nodes);
	}
	
	public ArrayList<Node> getNodes(){
		return nodes;
	}

	public Polygon[] getFastzones() {
		return fastzones;
	}

	public Polygon[] getSlowzones() {
		return slowzones;
	}

	/**
	 * Creates Areas to check for intersections. 
	 * TODO: Maybe remove unused parts later. Hint: obstacle area in use!!
	 */
	private void createAreas() {
		this.obstacleMap = new Area();
		for (int i = 0; i < obstacles.length; i++)
			obstacleMap.add(new Area(obstacles[i]));

		this.slowMap = new Area();
		for (int i = 0; i < slowzones.length; i++)
			slowMap.add(new Area(slowzones[i]));

		this.fastMap = new Area();
		for (int i = 0; i < fastzones.length; i++)
			fastMap.add(new Area(fastzones[i]));
	}

	/**
	 * 
	 * @param trackElements
	 *            includes the obstacles,
	 */
	private void createNodes(Polygon[][] trackElements) {
		this.edges = new ArrayList<ArrayList<Line2D>>(3);
		this.nodes = new ArrayList<Node>();

		
		for (int j = 0; j < trackElements.length; j++) {
			this.edges.add(new ArrayList<Line2D>());
			for (int i = 0; i < trackElements[j].length; i++) {
				Polygon element = trackElements[j][i];
				int[] xCoords = element.xpoints;
				int[] yCoords = element.ypoints;

				Vector2f[] points = new Vector2f[element.npoints];
				for (int p = 0; p < element.npoints; p++) {
					points[p] = new Vector2f(xCoords[p], yCoords[p]);
				}
				if(j == 2)
					addMidpoint(points);
				addNodes(points, j);
			}
		}
	}

	private void addMidpoint(Vector2f[] points) {
		Vector2f rv = Vector2f.sub(points[2], points[0], null);
		Vector2f midpoint = Vector2f.add(points[0], (Vector2f) rv.scale(0.5f), null);
		
		if(!obstacleMap.contains(midpoint.x, midpoint.y)){
			nodes.add(new Node(midpoint, "fastzone"));
		}
		
	}

	private void addNodes(Vector2f[] points, int num) {
		// Iteriere über alle Eckpunkte des Polygons
		for (int p = 0; p < points.length; p++) {
			Vector2f currentP = points[p];

			Vector2f previousP;
			if (p != 0)
				previousP = points[p - 1];
			else
				previousP = points[points.length - 1];

			Vector2f nextP;
			if (p != points.length - 1)
				nextP = points[p + 1];
			else
				nextP = points[0];

			// Fülle ArrayList Hindernisse mit Linien der Hindernisränder
			edges.get(num).add(new Line2D.Float(currentP.x, currentP.y, nextP.x, nextP.y));

			Vector2f toC = Vector2f.sub(currentP, previousP, null);
			Vector2f fromC = Vector2f.sub(nextP, currentP, null);

			// Obstacle
			if (num == 0) {
				// Prüfe auf Konvexität
				if (pointIsLeft(fromC, currentP, previousP))
					createNode(currentP, fromC, toC, 0);

				// Slowzone oder Fastzone
			} else
				createNode(currentP, fromC, toC, num);
		}
	}

	private void createNode(Vector2f currentP, Vector2f fromC, Vector2f toC, int num) {
		Vector2f toNext = fromC.normalise(null);
		Vector2f toPrevious = toC.negate(null).normalise(null);

		Vector2f mp = Vector2f.add(Vector2f.add(currentP, toNext, null),
				(Vector2f) Vector2f.sub(toPrevious, toNext, null).scale(0.5f), null);
		Vector2f toMp = Vector2f.sub(mp, currentP, null).normalise(null);

		Vector2f newPoint;
		Vector2f otherPoint = null;
		String type;
		// Obstacle
		if (num == 0) {
			newPoint = Vector2f.add(currentP, (Vector2f) toMp.negate(null).scale(O_SCALE), null);
			type = "obstacle";
			// Slowzone
		} else if (num == 1) {
			newPoint = Vector2f.add(currentP, (Vector2f) toMp.negate(null).scale(SZ_SCALE), null);
//			otherPoint = Vector2f.add(Vector2f.add(currentP, (Vector2f) fromC.scale(0.5f), null),(Vector2f) new Vector2f(toNext.y, - toNext.x).normalise(null).scale(MIDPOINT_SCALE), null);
			type = "slowzone";
			// Fastzone
		} else {
			newPoint = Vector2f.add(currentP, (Vector2f) toMp.negate(null).scale(FZ_SCALE), null);
			type  = "fastzone";
		}

		// Füge dem Graphen hinzu, falls Knoten nicht in einem Hindernis liegt
		if (!obstacleMap.contains(newPoint.x, newPoint.y)) {
			nodes.add(new Node(newPoint, type));
			
		}
//		if(otherPoint != null){
//			if(!obstacleMap.contains(otherPoint.x, otherPoint.y)){
//				nodes.add(new Node(otherPoint, type));
//			}
//		}

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

	private void createEdges(ArrayList<Node> nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			Node currentNode = nodes.get(i);
			for (int j = 0; j < this.nodes.size(); j++) {
				if (j == i)
					continue;
				Node toNode = this.nodes.get(j);
				if (!intersectsObstacle(currentNode, toNode)) {
					float weight;
					ArrayList<Vector2f> szIntersections = getSlowZoneIntersections(currentNode, toNode);
					ArrayList<Vector2f> fzIntersections = getFastZoneIntersections(currentNode, toNode);

					// Berechne Länge der Zonenschnitte
					if(szIntersections.isEmpty() & fzIntersections.isEmpty())
						weight = Vector2f.sub(toNode.getPoint(), currentNode.getPoint(), null).length();
					else{
					weight = Vector2f.sub(toNode.getPoint(), currentNode.getPoint(), null).length()
							+ calcAdditionalWeight(currentNode, toNode, szIntersections) 
							- calcLessWeight(currentNode, toNode, fzIntersections);
					}
					currentNode.addNode(new Edge2(toNode, weight));
				}
			}
		}
	}

	public float calcLessWeight(Node currentNode, Node toNode, ArrayList<Vector2f> intersections) {
		ArrayList<Vector2f> sorted = new ArrayList<Vector2f>();
		// Sortiere SPs nach Abstand zum aktuellen Knoten, kleister in Liste
		// zuerst
		intersections.sort(new Comparator<Vector2f>() {
			public int compare(Vector2f a, Vector2f b){
				float lengthA = Vector2f.sub(a, currentNode.getPoint(), null).length();
				float lengthB = Vector2f.sub(a, currentNode.getPoint(), null).length();
				int result = Float.compare(lengthA, lengthB);
				return result;
			}});
		sorted = intersections;
		
//		for (int i = 0; i < intersections.size(); i++) {
//			float distance = Vector2f.sub(intersections.get(i), currentNode.getPoint(), null).length();
//			if (sorted.isEmpty())
//				sorted.add(intersections.get(i));
//			else {
//				boolean inserted = false;
//				for (int j = 0; j < sorted.size(); j++) {
//					if (distance < Vector2f.sub(sorted.get(j), currentNode.getPoint(), null).length()) {
//						sorted.add(j, intersections.get(i));
//						inserted = true;
//					}
//				}
//				if (!inserted)
//					sorted.add(intersections.get(i));
//			}
//
//		}
		// hier sollten die SPs sortiert in der sorted ArrayList liegen ->
		// testen
		float lessWeight = 0;
		if (!sorted.isEmpty()) {
			if (fastMap.contains(currentNode.getPoint().x, currentNode.getPoint().y)
					& fastMap.contains(toNode.getPoint().x, toNode.getPoint().y)) {
				Vector2f current = currentNode.getPoint();
				lessWeight += Vector2f.sub(sorted.get(0), current, null).length() / 2;
				for (int i = 1; i < sorted.size(); i = i + 2) {
					if (i + 1 < sorted.size())
						lessWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * FZ_WEIGHT;
					else
						lessWeight += Vector2f.sub(toNode.getPoint(), sorted.get(i), null).length() * FZ_WEIGHT;
				}
			} else {
				if (fastMap.contains(currentNode.getPoint().x, currentNode.getPoint().y)) {
					Vector2f current = currentNode.getPoint();
					lessWeight += Vector2f.sub(sorted.get(0), current, null).length() * FZ_WEIGHT;
					for (int i = 1; i < sorted.size(); i += 2) {
						if (i + 1 < sorted.size())
							lessWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * FZ_WEIGHT;
					}
				} else if (fastMap.contains(toNode.getPoint().x, toNode.getPoint().y)) {
					for (int i = 0; i < sorted.size(); i += 2) {
						if (i + 1 >= sorted.size()) {
							lessWeight += Vector2f.sub(toNode.getPoint(), sorted.get(i), null).length() * FZ_WEIGHT;
							continue;
						}
						lessWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * FZ_WEIGHT;
					}
				} else {
					for (int i = 0; i < sorted.size(); i += 2) {
						if (i + 1 < sorted.size())
							lessWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * FZ_WEIGHT;
					}
				}
			}
		}else if(fastMap.contains(currentNode.getPoint().x, currentNode.getPoint().y) & fastMap.contains(toNode.getPoint().x, toNode.getPoint().y)){
			lessWeight = Vector2f.sub(toNode.getPoint(), currentNode.getPoint(), null).length() * FZ_WEIGHT;
		}
		return lessWeight;
	}

	

	// TODO: Concat with getSZInters. because auf same structure, with
	// additional parameter: int index (0=fz, 1=sz)
	public ArrayList<Vector2f> getFastZoneIntersections(Node from, Node to) {
		ArrayList<Vector2f> intersections = new ArrayList<Vector2f>();
		Line2D line = new Line2D.Float(from.getPoint().x, from.getPoint().y, to.getPoint().x, to.getPoint().y);
		for (int i = 0; i < edges.get(2).size(); i++) {
			if (edges.get(2).get(i).intersectsLine(line)) {
				Vector2f point = lineLineIntersection(edges.get(2).get(i), line);
//				if (line.contains(point.x, point.y) & edges.get(2).get(i).contains(point.x, point.y))
					intersections.add(point);
			}
		}
		return intersections;
	}

	public float calcAdditionalWeight(Node currentNode, Node toNode, ArrayList<Vector2f> intersections) {
		ArrayList<Vector2f> sorted = new ArrayList<Vector2f>(intersections.size());
		// Sortiere SPs nach Abstand zum aktuellen Knoten, kleister in Liste
		// zuerst
		
		
		//NEW
//		Collections.sort(intersections, new Comparator<Vector2f>() {
//			public int compare(Vector2f a, Vector2f b){
//				float lengthA = Vector2f.sub(a, currentNode.getPoint(), null).length();
//				float lengthB = Vector2f.sub(a, currentNode.getPoint(), null).length();
//				int result = Float.compare(lengthA, lengthB);
//				return result;
//			}
//		});
		intersections.sort(new Comparator<Vector2f>() {
			public int compare(Vector2f a, Vector2f b){
				float lengthA = Vector2f.sub(a, currentNode.getPoint(), null).length();
				float lengthB = Vector2f.sub(b, currentNode.getPoint(), null).length();
				int result = Float.compare(lengthA, lengthB);
				return result;
			}});
		sorted = intersections;
//		for (int i = 0; i < intersections.size(); i++) {
//			float distance = Vector2f.sub(intersections.get(i), currentNode.getPoint(), null).length();
//			if (sorted.isEmpty())
//				sorted.add(intersections.get(i));
//			else {
//				boolean inserted = false;
//				for (int j = 0; j < sorted.size(); j++) {
//					if (distance < Vector2f.sub(sorted.get(j), currentNode.getPoint(), null).length()) {
//						sorted.add(j, intersections.get(i));
//						inserted = true;
//					}
//				}
//				if (!inserted)
//					sorted.add(intersections.get(i));
//			}
//
//		}
		// hier sollten die SPs sortiert in der sorted ArrayList liegen ->
		// testen
		float additionalWeight = 0;
		if (!sorted.isEmpty()) {
			if (slowMap.contains(currentNode.getPoint().x, currentNode.getPoint().y)
					&& slowMap.contains(toNode.getPoint().x, toNode.getPoint().y)) {
				Vector2f current = currentNode.getPoint();
				additionalWeight += Vector2f.sub(sorted.get(0), current, null).length() * SZ_WEIGHT;
				for (int i = 1; i < sorted.size(); i = i + 2) {
					if (i + 1 < sorted.size())
						additionalWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * SZ_WEIGHT;
					else
						additionalWeight += Vector2f.sub(toNode.getPoint(), sorted.get(i), null).length() * SZ_WEIGHT;
				}
			} else {
				if (slowMap.contains(currentNode.getPoint().x, currentNode.getPoint().y)) {
					Vector2f current = currentNode.getPoint();
					additionalWeight += Vector2f.sub(sorted.get(0), current, null).length() * SZ_WEIGHT;
					for (int i = 1; i < sorted.size(); i += 2) {
						if (i + 1 < sorted.size())
							additionalWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * SZ_WEIGHT;
					}
				} else if (slowMap.contains(toNode.getPoint().x, toNode.getPoint().y)) {
					for (int i = 0; i < sorted.size(); i += 2) {
						if (i + 1 >= sorted.size()) {
							additionalWeight += Vector2f.sub(toNode.getPoint(), sorted.get(i), null).length() * SZ_WEIGHT;
							continue;
						}
						additionalWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * SZ_WEIGHT;
					}
				} else {
					for (int i = 0; i < sorted.size(); i += 2) {
						if (i + 1 < sorted.size())
							additionalWeight += Vector2f.sub(sorted.get(i + 1), sorted.get(i), null).length() * SZ_WEIGHT;
					}
				}
			}
		}
		return additionalWeight;
	}

	public Polygon[] getObstacles() {
		return obstacles;
	}

	public Area getObstacleMap() {
		return obstacleMap;
	}

	public Area getSlowMap() {
		return slowMap;
	}

	public Area getFastMap() {
		return fastMap;
	}

	public ArrayList<ArrayList<Line2D>> getEdges() {
		return edges;
	}

	public static float getoScale() {
		return O_SCALE;
	}

	public static float getSzScale() {
		return SZ_SCALE;
	}

	public static float getFzScale() {
		return FZ_SCALE;
	}

	public boolean intersectsObstacle(Node from, Node to) {
		Line2D[] lines = createParallelLines(from, to);
		ArrayList<Line2D> bounds = edges.get(0);
		for (int i = 0; i < bounds.size(); i++) {
			if (bounds.get(i).intersectsLine(lines[0])
					|| bounds.get(i).intersectsLine(lines[1]) | bounds.get(i).intersectsLine(lines[2])){
				return true;
			}
		}
		return false;
	}
	
	public boolean intersectsSlowZone(Node from, Node to){
		Line2D line = new Line2D.Float(from.getPoint().x, from.getPoint().y, to.getPoint().x, to.getPoint().y);
		for (int i = 0; i < edges.get(1).size(); i++) {
			if (edges.get(1).get(i).intersectsLine(line)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<Vector2f> getSlowZoneIntersections(Node from, Node to) {
		ArrayList<Vector2f> intersections = new ArrayList<Vector2f>();
		Line2D line = new Line2D.Float(from.getPoint().x, from.getPoint().y, to.getPoint().x, to.getPoint().y);
		for (int i = 0; i < edges.get(1).size(); i++) {
			if (edges.get(1).get(i).intersectsLine(line)) {
				Vector2f point = lineLineIntersection(edges.get(1).get(i), line);
//				if (line.contains(point.x, point.y) & edges.get(1).get(i).contains(point.x, point.y))
					intersections.add(point);
			}
		}
		return intersections;
	}

	public Vector2f lineLineIntersection(Line2D a, Line2D b) {
		float x1 = (float) a.getX1();
		float x2 = (float) a.getX2();
		float y1 = (float) a.getY1();
		float y2 = (float) a.getY2();
		float x3 = (float) b.getX1();
		float x4 = (float) b.getX2();
		float y3 = (float) b.getY1();
		float y4 = (float) b.getY2();

		float x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4))
				/ ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));
		float y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4))
				/ ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

		return new Vector2f(x, y);
	}

	/**
	 * Creates the needed lines to check if the drive space is free
	 */
	private Line2D[] createParallelLines(Node from, Node to) {
		Vector2f sv = from.getPoint();
		Vector2f rvTo = to.getPoint();
		Vector2f rv = Vector2f.sub(rvTo, sv, null);
		Vector2f orthoRV = new Vector2f(-rv.y, rv.x).normalise(null);

		orthoRV = (Vector2f) orthoRV.scale(5);

		Line2D line = new Line2D.Float(sv.x, sv.y, rvTo.x, rvTo.y);
		Line2D para1 = new Line2D.Float((float) sv.x + orthoRV.x, (float) sv.y + orthoRV.y, (float) rvTo.x + orthoRV.x,
				(float) rvTo.y + orthoRV.y);
		Line2D para2 = new Line2D.Float((float) sv.x - orthoRV.x, (float) sv.y - orthoRV.y, (float) rvTo.x - orthoRV.x,
				(float) rvTo.y - orthoRV.y);

		Line2D[] result = { line, para1, para2 };
		return result;
	}

}
