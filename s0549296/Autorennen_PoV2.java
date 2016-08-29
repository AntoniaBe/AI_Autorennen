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
	
	final static float PATH_POINT_RADIUS = 20f;
	final static float FZ_PATH_POINT_RADIUS = 40f;

	private SteeringBehaviours steering;
	
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
		steering = new SteeringBehaviours(info);
	}


	@Override
	public String getName() {
		return "BURNING DESIRE6";
	}
	
	@Override
	public boolean isEnabledForRacing(){
		return true;
	}
	
	@Override
	public String getTextureResourceName() {
		return  "/s0549296/flames2.png";
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
		if(!graph.intersectsObstacle(new Node(pos), new Node(checkpoint)) ){
			action = steering.seek(pos, checkpoint, true);
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
				
					
				if(currentPath==null){
					action=steering.seek(pos, checkpoint, true);
				}else{
					betterPath = path.betterPath(currentPath);
				}
				currentCP = new Point(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
				pathIndex=0;
				
			}else{
				if(pathIndex<betterPath.size()){
					if((graph.getFastMap().contains(betterPath.get(pathIndex).x, betterPath.get(pathIndex).y) && Vector2f.sub(betterPath.get(pathIndex), pos, null).length()<(FZ_PATH_POINT_RADIUS)) || Vector2f.sub(betterPath.get(pathIndex), pos, null).length()<(PATH_POINT_RADIUS)){
						pathIndex++;
					}
				}
				if(pathIndex<betterPath.size()){
					action = steering.seek(pos, betterPath.get(pathIndex), currentPath.contains(betterPath.get(pathIndex)));
					rv = Vector2f.sub(betterPath.get(pathIndex), pos, null);
					
				}else{
					action = steering.seek(pos, checkpoint, true);
				}
			}
		}
		
		float degree = (float)Math.atan2(rv.y, rv.x);
		float diff = degree - o;
		diff = steering.checkDegree(diff);
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
}
