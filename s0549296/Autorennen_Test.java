//package s0549296;
//
//import java.awt.Polygon;
//import java.util.ArrayList;
//import java.awt.geom.Area;
//import java.awt.Point;
//
//
////import org.lwjgl.input.Keyboard;
////import org.lwjgl.util.Point;
//import org.lwjgl.util.vector.Vector2f;
//import static org.lwjgl.opengl.GL11.*;
//
//import lenz.htw.ai4g.ai.AI;
//import lenz.htw.ai4g.ai.DriverAction;
//import lenz.htw.ai4g.ai.Info;
//
//public class Autorennen_Test extends AI {
//	final static float MAX_SEE_AHEAD = 20f;
//	final static float MAX_AVOIDANCE_FORCE = 10f;
//	final static float TOLERANCE = 0.01f;
//	final static float DECELERATING_DEGREE = 1.5f;
//	final static float WISH_TIME_R = 0.5f;
//	final static float WISH_TIME_D = 0.1f;
//	final static float DESTINATION_RADIUS = 3f;
//	final static float DECELERATING_RADIUS = 50f; //zuvor:10
//	
//	final static int TILE_NUMBER = 40;
//	//Graph
//	float deltaX;
//	float deltaY;
//	
//	private Graph graph;
//	private Path path;
//	private int pathIndex=0;
//	private ArrayList<Vector2f> currentPath;
//	private ArrayList<Vector2f> betterPath;
//	private Point currentCP = new Point(-1, -1);
//	
//	public Autorennen_Test(Info info) {
//		super(info);
//		Area map = new Area();
//		Polygon[] polys = info.getTrack().getObstacles();
//		
//		//Bestimmt Obstacles in map
//				for(int i = 0 ; i < polys.length; i++){
//					map.add(new Area(polys[i]));
//				}
//		graph = new Graph(polys, map);
//		path = new Path();
//	}
//
//
//	@Override
//	public String getName() {
//		return "BURNING DESIRE5";
//	}
//	
//	@Override
//	public void doDebugStuff(){
//		if(currentPath!=null){
//			for(int i = 0; i+1<currentPath.size(); i++){
//				glBegin(GL_LINES);
//				glColor3f(0,0,1);
//				glVertex2f(currentPath.get(i).x, currentPath.get(i).y);
//				glVertex2f(currentPath.get(i+1).x, currentPath.get(i+1).y);
//				glEnd();
//			}
//		}
//	}
//	
//
//	@Override
//	public DriverAction update(boolean ichWurdeZurückgesetzt) {
//		
//		float[] action = new float[2];
//		
//		float cx = (float) info.getCurrentCheckpoint().getX();
//		float cy = (float) info.getCurrentCheckpoint().getY();
//		Vector2f checkpoint = new Vector2f(cx,cy);
//		
//		float x = info.getX();
//		float y = info.getY();
//		Vector2f pos = new Vector2f(x, y);
//		
//		Vector2f rv = Vector2f.sub(checkpoint, pos, null);
//		
//		float o = info.getOrientation();
//		
//		//Falls direkter Weg frei
//		if(graph.isFreespace(new Node(pos), new Node(checkpoint))){
//			action = seek(pos, checkpoint, true);
//		//Falls direkter Weg versperrt
//		}else{
//			if(ichWurdeZurückgesetzt){
//				currentPath = null;
//				betterPath = null;
//			}
//			if(currentCP.x != info.getCurrentCheckpoint().x | currentCP.y != info.getCurrentCheckpoint().y){
//				currentPath = null;
//				betterPath = null;
//			}
//			if(currentPath==null){
//				path.findShortestPath(graph, pos, checkpoint);
//				currentPath = path.getPath();
//				betterPath = path.betterPath(currentPath);
//				if(currentPath==null){
//					action=seek(pos, checkpoint, true);
//				}
//				currentCP = new Point(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
//				pathIndex=0;
//			}else{
//				if(pathIndex<betterPath.size()){
//					if(Vector2f.sub(betterPath.get(pathIndex), pos, null).length()<(20)){
//						pathIndex++;
//					}
//				}
//				if(pathIndex<betterPath.size()){
//					action = seek(pos, betterPath.get(pathIndex), currentPath.contains(betterPath.get(pathIndex)));
//					rv = Vector2f.sub(betterPath.get(pathIndex), pos, null);
//					
//				}else{
//					action = seek(pos, checkpoint, true);
//				}
//			}
//		}
//		
//		float degree = (float)Math.atan2(rv.y, rv.x);
//		float diff = degree - o;
//		diff = checkDegree(diff);
//		float abs = (float) rv.length();
//		
//		
//		//Kreisen vermeiden
//		if(Math.abs(diff)<0.05f){
//			if(abs>50){
//				action[0] = info.getMaxAcceleration();
//			}
//		}else{
//			action[0] = 0.03f;
//		}
//
////		//berechnetes throttle und steering anwenden
//		return new DriverAction(-20, 0);
//	}
//	
//	
//	private float checkDegree(float diff){
//		if(diff > Math.PI){
//			diff = (float) ((diff - 2 * Math.PI));
//		}else if ( diff < (-1)*( Math.PI)){
//			diff = (float) ((diff + 2 * Math.PI));
//		}
//		return diff;
//	}
//	
//	private float[] seek(Vector2f start, Vector2f dest, boolean arrive){
//		float[] action = new float[2];
//		
//		float rvX = (float)dest.x - start.x;
//		float rvY = (float)dest.y - start.y;
//		float degree = (float)Math.atan2(rvY, rvX);
//		//für Winkelberechnungen
//		float diff = degree - info.getOrientation();
//		
//		diff = checkDegree(diff);
//		if(arrive){
//			action[0] = arrive(start, dest);
//		}else{
//			action[0]=info.getMaxAcceleration();
//		}
//		action[1] = align(diff);
//		
//		return action;
//	}
//	
//	private float arrive(Vector2f start, Vector2f ziel){
//		Vector2f rv = Vector2f.sub(ziel, start, null);
//		float abs = rv.length();
//		if(abs<DESTINATION_RADIUS){
//			return 0;
//		}else{
//			//Abstand(Start, Ziel) < Abbremsradius
//			float wunschgeschw;
//			if(abs<DECELERATING_RADIUS){
//				wunschgeschw = abs * info.getMaxVelocity()/DECELERATING_RADIUS;
//			}else{
//				wunschgeschw = info.getMaxVelocity();
//			}
//			//Beschleunigung
//			return (float) ((wunschgeschw - info.getVelocity().length())/WISH_TIME_D);
//		}
//	}
//	
//	private float align(float degree){
//		//Winkel zwischen Orientierungen < Toleranz
//		if(Math.abs(degree) < TOLERANCE){
//			return 0;
//		
//		//Winkel zw. Orientierungen < Abbremswinkel
//		}else{
//			float wunschdrehgeschw ;
//			if(Math.abs(degree) < DECELERATING_DEGREE){
//				wunschdrehgeschw = degree * info.getMaxAngularVelocity()/DECELERATING_DEGREE;
//			}else{
//				wunschdrehgeschw = info.getMaxAngularVelocity();
//			}
//			//Beschleunigung
//			return (wunschdrehgeschw - info.getAngularVelocity())/WISH_TIME_R;
//		}
//	}
//	
//	@Override
//	public boolean isEnabledForRacing(){
//		return false;
//	}
//	
//	@Override
//	public String getTextureResourceName() {
//		return  "/s0549296/flames2.png";
//	}
//}
