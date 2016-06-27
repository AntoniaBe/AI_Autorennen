package s0549296;


import java.util.ArrayList;
import java.awt.Point;

import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class Autorennen_PoV2 extends AI {
	final static float MAX_SEE_AHEAD = 20f;
	final static float MAX_AVOIDANCE_FORCE = 10f;
	final static float TOLERANCE = 0.01f;
	final static float DECELERATING_DEGREE = 1.5f;
	final static float WISH_TIME_R = 0.5f;
	final static float WISH_TIME_D = 0.1f;
	final static float DESTINATION_RADIUS = 5f;
	final static float DECELERATING_RADIUS = 50f; //zuvor:10
	final static float PATH_POINT_RADIUS = 20f;
	final static float FZ_PATH_POINT_RADIUS = 40f;

	
	private Graph graph;

	private Path path;
	private int pathIndex=0;
	private ArrayList<Vector2f> currentPath;
	private ArrayList<Vector2f> betterPath;
	private Point currentCP = new Point(-1, -1);
	
	
	public Autorennen_PoV2(Info info) {
		super(info);

		graph = new Graph(info.getTrack());
		path = new Path(graph);
	}


	@Override
	public String getName() {
		return "BURNING DESIRE6";
	}
	
	@Override
	public void doDebugStuff(){
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
		
		float cx = (float) info.getCurrentCheckpoint().getX();
		float cy = (float) info.getCurrentCheckpoint().getY();
		Vector2f checkpoint = new Vector2f(cx,cy);
		
		float x = info.getX();
		float y = info.getY();
		Vector2f pos = new Vector2f(x, y);
		
		Vector2f rv = Vector2f.sub(checkpoint, pos, null);
		
		float o = info.getOrientation();
		
		//Falls direkter Weg frei
		if(!graph.intersectsObstacle(new Node(pos), new Node(checkpoint)) & !graph.intersectsSlowZone(new Node(pos), new Node(checkpoint))){
			action = seek(pos, checkpoint, true);
		//Falls direkter Weg versperrt
		}else{
			if(ichWurdeZurückgesetzt){
				currentPath = null;
				betterPath = null;
			}
			if(currentCP.x != info.getCurrentCheckpoint().x | currentCP.y != info.getCurrentCheckpoint().y){
				currentPath = null;
				betterPath = null;
			}
			if(currentPath==null){
				path.findShortestPath(pos, checkpoint);
				currentPath = path.getPath();
				betterPath = path.betterPath(currentPath);
				if(currentPath==null){
					action=seek(pos, checkpoint, true);
				}
				currentCP = new Point(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
				pathIndex=0;
			}else{
				if(pathIndex<betterPath.size()){
					if((graph.getFastMap().contains(betterPath.get(pathIndex).x, betterPath.get(pathIndex).y) & Vector2f.sub(betterPath.get(pathIndex), pos, null).length()<(FZ_PATH_POINT_RADIUS)) | Vector2f.sub(betterPath.get(pathIndex), pos, null).length()<(PATH_POINT_RADIUS)){
						pathIndex++;
					}
				}
				if(pathIndex<betterPath.size()){
					action = seek(pos, betterPath.get(pathIndex), currentPath.contains(betterPath.get(pathIndex)));
					rv = Vector2f.sub(betterPath.get(pathIndex), pos, null);
					
				}else{
					action = seek(pos, checkpoint, true);
				}
			}
		}
		
		float degree = (float)Math.atan2(rv.y, rv.x);
		float diff = degree - o;
		diff = checkDegree(diff);
		float abs = (float) rv.length();
		
		
		//Kreisen vermeiden
		if(Math.abs(diff)<0.1f){
			if(abs>50){
				action[0] = info.getMaxAcceleration();
			}
		}
		else{
			action[0] = 0.03f;
		}
		
		//In Fastzone maximaleGeschwindigkeit
		if(graph.getFastMap().contains(pos.x, pos.y)) {
			action[0] = info.getMaxAcceleration();
		}

		return new DriverAction(action[0], action[1]);
	}
	
	
	private float checkDegree(float diff){
		if(diff > Math.PI){
			diff = (float) ((diff - 2 * Math.PI));
		}else if ( diff < -Math.PI){
			diff = (float) ((diff + 2 * Math.PI));
		}
		return diff;
	}
	
	private float[] seek(Vector2f start, Vector2f dest, boolean arrive){
		float[] action = new float[2];
		
		float rvX = (float)dest.x - start.x;
		float rvY = (float)dest.y - start.y;
		float degree = (float)Math.atan2(rvY, rvX);
		//für Winkelberechnungen
		float diff = degree - info.getOrientation();
		
		diff = checkDegree(diff);
		if(arrive){
			action[0] = arrive(start, dest);
		}else{
			action[0]=info.getMaxAcceleration();
		}
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
			return (float) ((wunschgeschw - info.getVelocity().length())/WISH_TIME_D);
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
