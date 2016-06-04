package s0549296;

import java.awt.Polygon;
import java.awt.geom.PathIterator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.awt.geom.Area;
import java.awt.Point;


//import org.lwjgl.input.Keyboard;
//import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class Autorennen2 extends AI {

	
	final static float MAX_SEE_AHEAD = 20f;
	final static float MAX_AVOIDANCE_FORCE = 10f;
	final static float TOLERANCE = 0.01f;
	final static float DECELERATING_DEGREE = 1.5f;
	final static float WISH_TIME_R = 0.5f;
	final static float WISH_TIME_D = 5f;
	final static float DESTINATION_RADIUS = 0.05f;
	final static float DECELERATING_RADIUS = 10f;
	
	final static int TILE_NUMBER = 40;
	//Graph
	private Node[] nodes; 
	float deltaX;
	float deltaY;
	private ArrayDeque<Node> q ;
	private int pathIndex=0;
	private Point currentCP = null;
	private ArrayList<Vector2f> path ;
	private ArrayList<Vector2f> betterPath;
	
	
	public Autorennen2(Info info) {
		super(info);
		
		//Make grid
		float width = info.getTrack().getWidth();
		float height = info.getTrack().getHeight();
		deltaX = width/TILE_NUMBER;
		deltaY = height/TILE_NUMBER;
		
		Area map = new Area();
		Polygon[] polys = info.getTrack().getObstacles();
		//Bestimmt Obstacles in map
		for(int i = 0 ; i < polys.length; i++){
			map.add(new Area(polys[i]));
		}
		
		createGraph(map);
	}
	
	private void createGraph(Area map){
		//Hinzufügen der Knoten in Array nodes
		addNodes(map);
		addEdges();
		
	}
	
	/**
	 * Fügt alle Knoten zur PriorityQueue hinzu
	 * TODO: Später ggf. Hinternisfelder entfernen
	 */
	private void addNodes(Area map){
		nodes = new Node[TILE_NUMBER * TILE_NUMBER];
			for(int j=0; j<TILE_NUMBER; j++){
				for(int i =0; i< TILE_NUMBER; i++){
					int pos = j * TILE_NUMBER + i;
					Node n = new Node(new Vector2f((float)(i*deltaX +0.5*deltaX), (float)(j*deltaY +0.5*deltaY)));
					float x = deltaX*j;
					float y = deltaY*i;
					   if(map.intersects(x, y, deltaX, deltaY)){
						   n.setDriveable(false);
					   }else{
						   n.setDriveable(true);
					   }
					nodes[pos] = n;
			}
		}
	}
	
	/**
	 * Den erstellten Knoten werden hier Kanten zu anderen Knoten (max. 4 Stck.) hinzugefügt,
	 * Kanten werden nur zu befahrbaren Knoten erstellt, oder mit einem sehr großen Kantengewicht
	 * z.B. Integer.MAX_VALUE
	 */
	private void addEdges(){
		
		for(int y = 0 ; y <TILE_NUMBER; y++){
			for(int x= 0; x < TILE_NUMBER ; x++){
				int pos = y*TILE_NUMBER +x;
				if(x!=0){
					if(nodes[pos-1].isDriveable()){
						nodes[pos].addNode(new Edge(nodes[pos-1],1));
					}
				}
				if((x!=TILE_NUMBER-1)){
					if(nodes[pos+1].isDriveable()){
						nodes[pos].addNode(new Edge(nodes[pos+1],1));
					}
				}
				if(y!=0){
					if(nodes[pos-TILE_NUMBER].isDriveable()){
						nodes[pos].addNode(new Edge(nodes[pos-TILE_NUMBER],1));
					}
				}
				if(y!=TILE_NUMBER-1){
					if(nodes[pos+TILE_NUMBER].isDriveable()){
						nodes[pos].addNode(new Edge(nodes[pos+TILE_NUMBER],1));
					}
				}
			}
		}
	}
	
	private ArrayList<Vector2f> findShortestPath(Vector2f pos, Vector2f cp){
		//Vectoren in Tile einordnen
		int pos_X = (int) (pos.x/info.getTrack().getWidth()*TILE_NUMBER);
		int pos_Y = (int) (pos.y/info.getTrack().getWidth()*TILE_NUMBER);
		Node start = nodes[pos_Y*TILE_NUMBER + pos_X];
		int cp_X = (int) (cp.x/info.getTrack().getWidth()*TILE_NUMBER);
		int cp_Y = (int) (cp.y/info.getTrack().getWidth()*TILE_NUMBER);
		Node end = nodes[cp_Y*TILE_NUMBER + cp_X];
		
		//Knoten in ArrayDeque einfügen, TODO: evtl. andere Struktur verwenden
//		putNodesInAD();
		q = new ArrayDeque<Node>();
		Node current = end;
		end.setCurrentCost(0);
		q.add(end);
		if(end.getAdjList().size()==0){
			//Sitzt fest
			return null;
		}
		boolean done = false;
		while(!q.isEmpty()&current!=start){
			current = q.pop();
			int c=0;
			while(c<current.getAdjList().size()){
				Edge e = current.getAdjList().get(c);
				Node to = e.getTo();
				if(to.getCurrentCost() > (e.getWeight()+current.getCurrentCost())){
					to.setCurrentCost(e.getWeight()+current.getCurrentCost());
					to.setBefore(current);
					if(to ==start){
						done =true;
						break;
					}
					q.add(to);
				}
				c++;
			}
			if(done){
				break;
			}
			
		}
		if(start.getCurrentCost()<Integer.MAX_VALUE){
			path = new ArrayList<Vector2f>();

			//get Path... oder start und end vertauschen
			current = start;
			if(current.getBefore()==null){
				return null;
			}
			current = start.getBefore();
			path.add(current.getMp());
			while(current.getBefore()!=end){
				if(current.getBefore() != null){
					current = current.getBefore();
				}
				
				path.add(current.getMp());
			}
			path.add(cp);
			return path;
		}else{
			return null;
		}
	}
	
	private ArrayList<Vector2f> pathSmoothing(ArrayList<Vector2f> path){
		if(path == null){
			return null;
		}
		betterPath = new ArrayList<Vector2f>();
		boolean sameX = false;
		boolean sameY = false;
		float storedX;
		float storedY;
		for(int i=0; i<path.size() & i+1 < path.size(); i++){
			Vector2f current = path.get(i);
			Vector2f next = path.get(i+1);
			if(i==path.size()-2){
				betterPath.add(next);
				break;
			}
			
			if(current.x == next.x){
				sameX=true;
			}else{
				if(sameX){
					betterPath.add(current);
					sameX=false;
				}
			}
			if(current.y == next.y){
				sameY=true;
			}else{
				if(sameY){
					betterPath.add(current);
					sameY=false;
				}
			}
			
		}
		return betterPath;
	}

	@Override
	public String getName() {
		return "BURNING DESIRE2";
	}
	
	@Override
	public void doDebugStuff(){
		float width = info.getTrack().getWidth();
		float height = info.getTrack().getHeight();
		
		
		float deltaX = width /TILE_NUMBER;
		float deltaY = height/TILE_NUMBER;
		
		boolean[][] befahrbar = new boolean[TILE_NUMBER][TILE_NUMBER];
		Area map = new Area();
		Polygon[] polys = info.getTrack().getObstacles();
		//Obstacles in map
		for(int i = 0 ; i < polys.length; i++){
			map.add(new Area(polys[i]));
		}
		
		//j = Spalte, i= Zeile
		for(int j = 0; j<TILE_NUMBER; j++){
			for(int i=0; i<TILE_NUMBER; i++){
				float x;
				float y;
				   x = deltaX*j;
				   y = deltaY*i;
				   if(map.intersects(x, y, deltaX, deltaY)){
					   befahrbar[j][i]=false;
					   glColor4f(1f,0,0,0.5f);
					  
				   }else{
					   befahrbar[j][i]=true;
					   glColor4f(0,1f,0,0.5f);
					   
				   }
				   glBegin(GL_QUADS);
					glVertex2f(x, y+deltaY);
					glVertex2f(x,y);
					
					glVertex2f(x+deltaX, y);
					glVertex2f(x+deltaX, y+deltaY);
					glEnd();
				   }
			
		}
		if(betterPath!= null){
		for(int i =0; i<betterPath.size() & i+1<betterPath.size(); i++){
			glBegin(GL_LINES);
			glColor3f(0,0,1);
			glVertex2f(betterPath.get(i).x, betterPath.get(i).y);
			glVertex2f(betterPath.get(i+1).x, betterPath.get(i+1).y);
		}
		}
		
		for(int i=0; i<TILE_NUMBER; i++){
			
			glBegin(GL_LINES);
				glColor3f(0,0,0);
				glVertex2f(i*deltaX, 0);
				glVertex2f(i*deltaX, height);
			
			
			glEnd();
		}
			
		for(int i=0; i<TILE_NUMBER; i++){
				
				glBegin(GL_LINES);
					glColor3f(0,0,0);
					glVertex2f(0,i*deltaY);
					glVertex2f(width, i*deltaY);
				glEnd();
		}
	}
	
	
	private void pathFindAStar(Vector2f start, Vector2f end, Object heuristic){
		Node startNode = new Node(start);
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
//		
		float rvX = (float)cx - x;
		float rvY = (float) cy - y;
//		float degree = (float)Math.atan2(rvY, rvX);
//		//für Winkelberechnungen
//		float diff = degree - o;
//		//für Abstandsberechnungen
		float abs = (float) Math.sqrt((rvX*rvX)+(rvY*rvY));
		
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
		if(ichWurdeZurückgesetzt){
			path = null;
		}
		if(path==null){
			path = findShortestPath(pos, checkpoint);
			betterPath = pathSmoothing(path);
			if(betterPath==null){
				action=seek(pos, checkpoint);
			}
			currentCP = info.getCurrentCheckpoint();
			pathIndex=0;
		}else if (info.getCurrentCheckpoint()!= currentCP){
			path = null;
			path = findShortestPath(new Vector2f(currentCP.x, currentCP.y), checkpoint);
			betterPath = pathSmoothing(path);
			if(betterPath==null){
				action=seek(pos, checkpoint);
			}
			currentCP = info.getCurrentCheckpoint();
			pathIndex=0;
		}else{
			if(pathIndex<betterPath.size()){
				if(Vector2f.sub(betterPath.get(pathIndex), pos, null).length()<(deltaX*0.5f)){
					pathIndex++;
				}
			}
			if(pathIndex<betterPath.size()){
				System.out.println("Pos dist to MP:" + Vector2f.sub(path.get(pathIndex), pos, null).length());
				System.out.println("deltaX:" + deltaX);
				System.out.println(path.get(pathIndex));
				System.out.println(pathIndex);
				action = seek(pos, betterPath.get(pathIndex));
			}else{
				action = seek(pos, checkpoint);
			}
		}
		
	
		if(Math.abs(action[1])<0.1& abs>30){
			action[0] = info.getMaxAcceleration();
		}else if(Math.abs(action[1])>0.5){
			action[0] = 0.01f;
		}
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
		return false;
	}
	
	@Override
	public String getTextureResourceName() {
		return  "/s0549296/flames2.png";
	}
}
