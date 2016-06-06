package s0549296;

import java.awt.Polygon;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.Point;


//import org.lwjgl.input.Keyboard;
//import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class Autorennen_PoV extends AI {
	final static float MAX_SEE_AHEAD = 20f;
	final static float MAX_AVOIDANCE_FORCE = 10f;
	final static float TOLERANCE = 0.01f;
	final static float DECELERATING_DEGREE = 1.5f;
	final static float WISH_TIME_R = 0.5f;
	final static float WISH_TIME_D = 20f;
	final static float DESTINATION_RADIUS = 0.05f;
	final static float DECELERATING_RADIUS = 30f; //zuvor:10
	
	final static int TILE_NUMBER = 40;
	//Graph
	private Node[] nodes; 
	float deltaX;
	float deltaY;
	private ArrayDeque<Node> q ;
	private int pathIndex=0;
	private Point currentCP = new Point(-1, -1);
//	private ArrayList<Vector2f> path ;
//	private ArrayList<Vector2f> betterPath;
	
	private Graph graph;
	private Path path;
	private ArrayList<Vector2f> currentPath;
	
//	private ArrayList<Node2> graph;
	private ArrayList<Line2D> obstacles;

	
	
	public Autorennen_PoV(Info info) {
		super(info);
		Area map = new Area();
		Polygon[] polys = info.getTrack().getObstacles();
		
		//Bestimmt Obstacles in map
				for(int i = 0 ; i < polys.length; i++){
					map.add(new Area(polys[i]));
				}
		graph = new Graph(polys, map);
		path = new Path();
	}
	
//	private void createGraph(Polygon[] polys, Area map){
////		Area map = new Area();
////		Polygon[] polys = info.getTrack().getObstacles();
////		
////		//Bestimmt Obstacles in map
////				for(int i = 0 ; i < polys.length; i++){
////					map.add(new Area(polys[i]));
////				}
////		
////		graph = new ArrayList<Node2>();
////		obstacles = new ArrayList<Line2D>();
//		
//		//Iteriere über alle Hindernisse
//		for(int i=0; i< polys.length; i++){
//			System.out.println("Polygon:" + i);
//			Polygon poly = polys[i];
//			int[] xCoords = poly.xpoints;
//			int[] yCoords = poly.ypoints;
//			
//			//Fülle alle Eckpunkte des Hindernisses in ein Vektor Array
//			Vector2f[] points = new Vector2f[poly.npoints];
//			for(int p=0; p<poly.npoints; p++){
//				points[p] = new Vector2f(xCoords[p], yCoords[p]);
//			}
//			addNodes(points, map);
//		}
//		
//		//Hinzufügen der Knoten in Array nodes
//		addEdges();
//	}
	
	/**
	 * Den erstellten Knoten werden hier Kanten zu anderen Knoten (max. 4 Stck.) hinzugefügt,
	 * Kanten werden nur zu befahrbaren Knoten erstellt, oder mit einem sehr großen Kantengewicht
	 * z.B. Integer.MAX_VALUE
	 */
//	private void addEdges(){
//		for(int i = 0 ; i<graph.size(); i++){
//			Node2 currentNode = graph.get(i);
//			for(int j=0; j<graph.size(); j++){
//				if(j==i)
//					continue;
//				Node2 toNode = graph.get(j);
//				if(isFreespace(currentNode, toNode)){
//					float weight = Vector2f.sub(toNode.getPoint(), currentNode.getPoint(), null).length();
//					currentNode.addNode(new Edge2(toNode, weight));
//				}
//			}
//		}
//	}
//	
//	public boolean isFreespace(Node2 from, Node2 to){
//		Line2D[] lines = createParallelLines(from, to);
//		for(int i=0; i<obstacles.size(); i++){
//			if(obstacles.get(i).intersectsLine(lines[0])||obstacles.get(i).intersectsLine(lines[1])|obstacles.get(i).intersectsLine(lines[2]))
//				return false;
//		}
//		return true;
//	}
	
	/**
	 * Creates the needed lines to check if the drive space is free
	 */
//	private Line2D[] createParallelLines(Line2D line){
//		Vector2f rv = Vector2f.sub(new Vector2f((float)line.getX1(), (float)line.getY1()), new Vector2f((float)line.getX2(), (float)line.getY2()), null);
//		Vector2f orthoRV = new Vector2f(-rv.y, rv.x).normalise(null);
//		
//		//Skaliere orthoRV
//		orthoRV = (Vector2f) orthoRV.scale(5);
//		
//		Line2D para1 = new Line2D.Float((float)line.getX1()+orthoRV.x, (float)line.getY1()+orthoRV.y, (float)line.getX2()+orthoRV.x, (float)line.getY2()+orthoRV.y);
//		Line2D para2 = new Line2D.Float((float)line.getX1()-orthoRV.x, (float)line.getY1()-orthoRV.y, (float)line.getX2()-orthoRV.x, (float)line.getY2()-orthoRV.y);
//		
//		Line2D[] result = {line, para1, para2};
//		return result;
//	}
	/**
	 * Creates the needed lines to check if the drive space is free
	 */
//	private Line2D[] createParallelLines(Node2 from, Node2 to){
//		Vector2f sv = from.getPoint();
//		Vector2f rvTo = to.getPoint();
//		Vector2f rv = Vector2f.sub(rvTo, sv, null);
//		Vector2f orthoRV = new Vector2f(-rv.y, rv.x).normalise(null);
//		
//		
//		//Skaliere orthoRV
//		orthoRV = (Vector2f) orthoRV.scale(5);
//		
//		Line2D line = new Line2D.Float(sv.x, sv.y, rvTo.x, rvTo.y);
//		Line2D para1 = new Line2D.Float((float)sv.x+orthoRV.x, (float)sv.y+orthoRV.y, (float)rvTo.x+orthoRV.x, (float)rvTo.y+orthoRV.y);
//		Line2D para2 = new Line2D.Float((float)sv.x-orthoRV.x, (float)sv.y-orthoRV.y, (float)rvTo.x-orthoRV.x, (float)rvTo.y-orthoRV.y);
//		
//		Line2D[] result = {line, para1, para2};
//		return result;
//	}
	
	/**
	 * Creates the needed lines to check if the drive space is free
	 */
//	private Line2D[] createParallelLines(Vector2f sv, Vector2f rv){
//		Vector2f orthoRV = new Vector2f(-rv.y, rv.x).normalise(null);
//		Vector2f rvTo = Vector2f.add(sv, rv, null);
//		
//		//Skaliere orthoRV
//		orthoRV = (Vector2f) orthoRV.scale(5);
//		
//		Line2D line = new Line2D.Float(sv.x, sv.y, rvTo.x, rvTo.y);
//		Line2D para1 = new Line2D.Float((float)sv.x+orthoRV.x, (float)sv.y+orthoRV.y, (float)rvTo.x+orthoRV.x, (float)rvTo.y+orthoRV.y);
//		Line2D para2 = new Line2D.Float((float)sv.x-orthoRV.x, (float)sv.y-orthoRV.y, (float)rvTo.x-orthoRV.x, (float)rvTo.y-orthoRV.y);
//		
//		Line2D[] result = {line, para1, para2};
//		return result;
//	}
	
//	private boolean pointIsLeft(Vector2f rv, Vector2f sVector, Vector2f testPoint){
//		Vector2f sVtoTP = Vector2f.sub(testPoint, sVector, null);
//		Vector2f orthoRV = new Vector2f(-rv.y, rv.x);
//		float scalar = Vector2f.dot(sVtoTP, orthoRV);
//		if(scalar>=0){
//			return true;
//		}else{
//			return false;
//		}
//	}
	
	/**
	 * Fügt alle Knoten zur PriorityQueue hinzu
	 * TODO: Später ggf. Hinternisfelder entfernen
	 */
//	private void addNodes(Vector2f[] points, Area map){
//		//Prüfe auf Konvexität
//		for(int p = 0;  p< points.length; p++){
//			Vector2f currentP = points[p];
//			Vector2f previousP;
//			if(p!=0){
//				previousP = points[p-1];
//			}else{
//				previousP = points[points.length-1];
//			}
//			Vector2f nextP;
//			if(p!=points.length-1){
//				nextP = points[p+1];
//			}else{
//				nextP = points[0];
//			}
//			Vector2f toC = Vector2f.sub(currentP, previousP, null);
//			Vector2f fromC = Vector2f.sub(nextP, currentP, null);
//			//Fülle ArrayList Hindernisse mit Linien der Hindernisränder
//			obstacles.add(new Line2D.Float(currentP.x, currentP.y, nextP.x, nextP.y));
//			
//			
//			//->CurrentP ist konvex und kann dem Graphen hinzugefügt werden
//			if(pointIsLeft(fromC, currentP, previousP)){
//				//RV von current zu Ecken davor und dahinter, normalisiert
//				Vector2f toNext = fromC.normalise(null);
//				Vector2f toPrevious = toC.negate(null).normalise(null);
//				
//				Vector2f mp = Vector2f.add(Vector2f.add(currentP, toNext, null),(Vector2f) Vector2f.sub(toPrevious, toNext, null).scale(0.5f), null);
//				Vector2f toMp = Vector2f.sub(mp, currentP, null).normalise(null).negate(null);
//				
//				//Lege den Abstand zum Hindernis durch Skalierung fest
//				float scaleFactor = 10;
//				Vector2f newPoint = Vector2f.add(currentP, (Vector2f) toMp.scale(scaleFactor), null);
//				if(!map.contains(new Point2D.Float(newPoint.x, newPoint.y))){
//				//Lege den Knoten im Graphen an
//				graph.add(new Node2(newPoint));
//				}
//				
//			}
//			
//		}
//	}
	
	
	
//	private ArrayList<Vector2f> findShortestPath(Vector2f pos, Vector2f cp){
//		//Vectoren in Tile einordnen
//		int pos_X = (int) (pos.x/info.getTrack().getWidth()*TILE_NUMBER);
//		int pos_Y = (int) (pos.y/info.getTrack().getWidth()*TILE_NUMBER);
//		Node start = nodes[pos_Y*TILE_NUMBER + pos_X];
//		int cp_X = (int) (cp.x/info.getTrack().getWidth()*TILE_NUMBER);
//		int cp_Y = (int) (cp.y/info.getTrack().getWidth()*TILE_NUMBER);
//		Node end = nodes[cp_Y*TILE_NUMBER + cp_X];
//		
//		//Knoten in ArrayDeque einfügen, TODO: evtl. andere Struktur verwenden
////		putNodesInAD();
//		q = new ArrayDeque<Node>();
//		Node current = end;
//		end.setCurrentCost(0);
//		q.add(end);
//		if(end.getAdjList().size()==0){
//			//Sitzt fest
//			return null;
//		}
//		boolean done = false;
//		while(!q.isEmpty()&current!=start){
//			current = q.pop();
//			int c=0;
//			while(c<current.getAdjList().size()){
//				Edge e = current.getAdjList().get(c);
//				Node to = e.getTo();
//				if(to.getCurrentCost() > (e.getWeight()+current.getCurrentCost())){
//					to.setCurrentCost(e.getWeight()+current.getCurrentCost());
//					to.setBefore(current);
//					if(to ==start){
//						done =true;
//						break;
//					}
//					q.add(to);
//				}
//				c++;
//			}
//			if(done){
//				break;
//			}
//			
//		}
//		if(start.getCurrentCost()<Integer.MAX_VALUE){
//			path = new ArrayList<Vector2f>();
//
//			//get Path... oder start und end vertauschen
//			current = start;
//			if(current.getBefore()==null){
//				return null;
//			}
//			current = start.getBefore();
//			path.add(current.getMp());
//			while(current.getBefore()!=end){
//				if(current.getBefore() != null){
//					current = current.getBefore();
//				}
//				
//				path.add(current.getMp());
//			}
//			path.add(cp);
//			return path;
//		}else{
//			return null;
//		}
//	}
	
//	private ArrayList<Vector2f> pathSmoothing(ArrayList<Vector2f> path){
//		if(path == null){
//			return null;
//		}
//		betterPath = new ArrayList<Vector2f>();
//		boolean sameX = false;
//		boolean sameY = false;
//		float storedX;
//		float storedY;
//		for(int i=0; i<path.size() & i+1 < path.size(); i++){
//			Vector2f current = path.get(i);
//			Vector2f next = path.get(i+1);
//			if(i==path.size()-2){
//				betterPath.add(next);
//				break;
//			}
//			
//			if(current.x == next.x){
//				sameX=true;
//			}else{
//				if(sameX){
//					betterPath.add(current);
//					sameX=false;
//				}
//			}
//			if(current.y == next.y){
//				sameY=true;
//			}else{
//				if(sameY){
//					betterPath.add(current);
//					sameY=false;
//				}
//			}
//			
//		}
//		return betterPath;
//	}

	@Override
	public String getName() {
		return "BURNING DESIRE5";
	}
	
	@Override
	public void doDebugStuff(){
		
		
//		float width = info.getTrack().getWidth();
//		float height = info.getTrack().getHeight();
//		
//		
//		float deltaX = width /TILE_NUMBER;
//		float deltaY = height/TILE_NUMBER;
//		
//		boolean[][] befahrbar = new boolean[TILE_NUMBER][TILE_NUMBER];
//		Area map = new Area();
//		Polygon[] polys = info.getTrack().getObstacles();
//		//Obstacles in map
//		for(int i = 0 ; i < polys.length; i++){
//			map.add(new Area(polys[i]));
//		}
//		
//		//j = Spalte, i= Zeile
//		for(int j = 0; j<TILE_NUMBER; j++){
//			for(int i=0; i<TILE_NUMBER; i++){
//				float x;
//				float y;
//				   x = deltaX*j;
//				   y = deltaY*i;
//				   if(map.intersects(x, y, deltaX, deltaY)){
//					   befahrbar[j][i]=false;
//					   glColor4f(1f,0,0,0.5f);
//					  
//				   }else{
//					   befahrbar[j][i]=true;
//					   glColor4f(0,1f,0,0.5f);
//					   
//				   }
//				   glBegin(GL_QUADS);
//					glVertex2f(x, y+deltaY);
//					glVertex2f(x,y);
//					
//					glVertex2f(x+deltaX, y);
//					glVertex2f(x+deltaX, y+deltaY);
//					glEnd();
//				   }
//			
//		}
//		if(currentPath!= null){
//		for(int i =0; i<currentPath.size() & i+1<currentPath.size(); i++){
//			glBegin(GL_LINES);
//			glColor3f(0,0,1);
//			glVertex2f(currentPath.get(i).x, currentPath.get(i).y);
//			glVertex2f(currentPath.get(i+1).x, currentPath.get(i+1).y);
//		}
//		}
//		
//		for(int i=0; i<TILE_NUMBER; i++){
//			
//			glBegin(GL_LINES);
//				glColor3f(0,0,0);
//				glVertex2f(i*deltaX, 0);
//				glVertex2f(i*deltaX, height);
//			
//			
//			glEnd();
//		}
//			
//		for(int i=0; i<TILE_NUMBER; i++){
//				
//				glBegin(GL_LINES);
//					glColor3f(0,0,0);
//					glVertex2f(0,i*deltaY);
//					glVertex2f(width, i*deltaY);
//				glEnd();
//		}
		
		if(currentPath!=null){
			for(int i = 0; i+1<currentPath.size(); i++){
				glBegin(GL_LINES);
				glColor3f(0,0,1);
				glVertex2f(currentPath.get(i).x, currentPath.get(i).y);
				glVertex2f(currentPath.get(i+1).x, currentPath.get(i+1).y);
				glEnd();
			}
		}
	}
	

	
	
	@Override
	public DriverAction update(boolean ichWurdeZurückgesetzt) {
		
		float[] action = new float[2];
		float o = info.getOrientation();
		float cx = (float) info.getCurrentCheckpoint().getX();
		float cy = (float) info.getCurrentCheckpoint().getY();
		Vector2f checkpoint = new Vector2f(cx,cy);
		float x = info.getX();
		float y = info.getY();
		Vector2f pos = new Vector2f(x, y);
		Vector2f velocity = info.getVelocity();
//		
//		float rvX = (float)cx - x;
//		float rvY = (float) cy - y;
		Vector2f rv = Vector2f.sub(checkpoint, pos, null);
		
//		//für Abstandsberechnungen
		
		
		//TODO: Prüfe ob Weg zum Ziel überhaupt versperrt!!
		
		
		
		//TODO: auf NullPointer prüfen, bzw umgehen
//		if(info.getCurrentCheckpoint()==currentCP){
//			
//			if(path == null){
//				//Alternative überlegen!!!!
//			}else{
//				Vector2f currentAim = path.get(pathIndex);
//				action = seek(pos, currentAim);
//			}
//		}else{
//			path = findShortestPath(pos, checkpoint);
//			currentCP = info.getCurrentCheckpoint();
//	}
//		if(currentPath!=null){
//		if(ichWurdeZurückgesetzt| pathIndex == currentPath.size()){
//			currentPath = null;
//		}
//		}
		
		

		
		
		//Falls direkter Weg frei
		if(graph.isFreespace(new Node2(pos), new Node2(checkpoint))){
			action = seek(pos, checkpoint);
		//Falls direkter Weg versperrt
		}else{
			if(ichWurdeZurückgesetzt){
				currentPath = null;
			}
			if(currentCP.x != info.getCurrentCheckpoint().x | currentCP.y != info.getCurrentCheckpoint().y){
				currentPath = null;
			}
			if(currentPath==null){
				path.findShortestPath(graph, pos, checkpoint);
				currentPath = path.getPath();
				if(currentPath==null){
					action=seek(pos, checkpoint);
				}
				currentCP = new Point(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
				pathIndex=0;
			}else{
				if(pathIndex<currentPath.size()){
					if(Vector2f.sub(currentPath.get(pathIndex), pos, null).length()<(10)){
						pathIndex++;
					}
				}
				if(pathIndex<currentPath.size()){
//					System.out.println("Pos dist to MP:" + Vector2f.sub(currentPath.get(pathIndex), pos, null).length());
//					System.out.println("deltaX:" + deltaX);
//					System.out.println(currentPath.get(pathIndex));
//					System.out.println(pathIndex);
					action = seek(pos, currentPath.get(pathIndex));
					rv = Vector2f.sub(currentPath.get(pathIndex), pos, null);
					
				}else{
					action = seek(pos, checkpoint);
				}
			}
		}
		float degree = (float)Math.atan2(rv.y, rv.x);
//		//für Winkelberechnungen
		float diff = degree - o;
		diff = checkDegree(diff);
		float abs = (float) rv.length();
		
		System.out.println("Degree pos, cp:" + degree);
		System.out.println("Angular diff between degree(pos,cp) and orientation:" + diff);
		
		
		//Kreisen vermeiden
		if(Math.abs(diff)<0.05f){
			if(abs>50){
				action[0] = info.getMaxAcceleration();
			}
		}else{
			action[0] = 0.02f;//0.01f;
		}
		
//		if(Math.abs(action[1])<0.1& abs>30){
//		if(Math.abs(action[1])<0.1){
//			action[0] = info.getMaxAcceleration();
////		}else if(abs<=30 & abs>2 & Math.abs(action[1])<0.1){
////			action[0] = info.getMaxAcceleration();
//		}else if(Math.abs(action[1])>0.1&Math.abs(action[1])<=0.5){
//			action[0] = 0.01f;
//		}else if(Math.abs(action[1])>0.5){
//			action[0] = 0.01f;
//		}
		
		System.out.println("Throttle:" + action[0]);
		System.out.println("Steering:" + action[1]);

//
//		//berechnetes throttle und steering anwenden
		return new DriverAction(action[0], action[1]);
//		return new DriverAction(info.getMaxVelocity(), 0);
	}
	
	
	private float[] obstacleInfo(Polygon o){
		//Kreis ziehen
				float minX = info.getTrack().getWidth();
				float maxX = 0;
				float minY = info.getTrack().getHeight();
				float maxY = 0;
				
				//Berechne min und max von x und y des Hindernisses
				for(int i =0; i<o.npoints; i++){
					float ox = o.xpoints[i];
					float oy = o.ypoints[i];
					if(ox<minX)
						minX = ox;
					else if(ox > maxX)
						maxX = ox;
					
					if(oy<minY)
						minY = oy;
					else if(oy>maxY)
						maxY = oy;
				}
				
				float oWidth = maxX -minX;
				float oHeight = maxY - minY;
				float radius = 0;
				if(oWidth == oHeight)
					radius=0.5f * oWidth;
				//Berechne MP des Hindernisses
				float mpX = minX + radius;
				float mpY = minY + radius;
				
				float[] oInfo = {mpX, mpY, radius};
				return oInfo;
	}
	
	private Vector2f isLeft(Polygon obstacle, Vector2f[] vs, Vector2f[] svEndpoints){
		int result = 0;
		int n = 0;
//		float[] scalar = new float[obstacle.npoints];
		for(int i = 0 ; i<svEndpoints.length; i++){
			for(int j  = 0 ; j<obstacle.npoints; j++){
			Vector2f vTs= new Vector2f(-vs[j].y, vs[j].x);
				float scalar= Vector2f.dot((Vector2f.sub(svEndpoints[i], new Vector2f(obstacle.xpoints[j], obstacle.ypoints[j]), null) ), vTs);
				if(scalar>0)
					n++;
			}
			if(n==obstacle.npoints){
				if(i<2)
					result++;
				else
					result --;
			}
		}
		if(result<0)
			return svEndpoints[3];
		else if( result> 0 )
			return svEndpoints[0];
		else 
			return null;
	}
	
	private Vector2f[] obstacleBounds(Polygon o){
		Vector2f[] result = new Vector2f[o.npoints];
		for (int i=0; i<o.npoints; i++){
			float rvX = o.xpoints[(i+1)%8] - o.xpoints[i];
			float rvY = o.ypoints[(i+1)%8] - o.ypoints[i];
			result[i] = new Vector2f(rvX, rvY);
		}
		return result;
	}
	
	private float checkDegree(float diff){
		if(diff > Math.PI){
//		diff = (float) ((float) - Math.PI + (diff-Math.PI));
			diff = (float) ((diff - 2 * Math.PI));
		}else if ( diff < (-1)*( Math.PI)){
//		diff = (float) (Math.PI + (diff + Math.PI));
			diff = (float) ((diff + 2 * Math.PI));
		}
		return diff;
	}
	
	private float[] seek(Vector2f start, Vector2f dest){
		float[] action = new float[2];
		
		float rvX = (float)dest.x - start.x;
		float rvY = (float)dest.y - start.y;
		float degree = (float)Math.atan2(rvY, rvX);
		//für Winkelberechnungen
		float diff = degree - info.getOrientation();
		
		diff = checkDegree(diff);
		action[0] = arrive(start, dest);
		action[1] = align(diff);
		
		return action;
	}
	
	private float arrive(Vector2f start, Vector2f ziel){
		Vector2f rv = Vector2f.sub(ziel, start, null);
		float abs = rv.length();
		if(abs<DESTINATION_RADIUS){
			return 0;
		}else{
			//Abstand(Start, Ziel) < Abbremsradius
			float wunschgeschw;
			if(abs<DECELERATING_RADIUS){
				wunschgeschw = abs * info.getMaxVelocity()/DECELERATING_RADIUS;
			}else{
				wunschgeschw = info.getMaxVelocity();
			}
			//Beschleunigung
			float gx = info.getVelocity().x;
			float gy = info.getVelocity().y;
			return (float) ((wunschgeschw - Math.sqrt((gx*gx) + (gy*gy)))/WISH_TIME_D);
		}
	}
	
	private float align(float degree){
		//Winkel zwischen Orientierungen < Toleranz
		if(Math.abs(degree) < TOLERANCE){
			return 0;
		
		//Winkel zw. Orientierungen < Abbremswinkel
		}else{
			float wunschdrehgeschw ;
			if(Math.abs(degree) < DECELERATING_DEGREE){
				wunschdrehgeschw = degree * info.getMaxAngularVelocity()/DECELERATING_DEGREE;
			}else{
				wunschdrehgeschw = info.getMaxAngularVelocity();
			}
			//Beschleunigung
			return (wunschdrehgeschw - info.getAngularVelocity())/WISH_TIME_R;
		}
	}
	
	@Override
	public boolean isEnabledForRacing(){
		return true;
	}
	
	@Override
	public String getTextureResourceName() {
		return  "/s0549296/flames2.png";
	}
}
