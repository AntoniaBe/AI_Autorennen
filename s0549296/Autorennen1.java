package s0549296;

import java.awt.Polygon;
import java.awt.geom.PathIterator;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class Autorennen1 extends AI {

	
	final static float MAX_SEE_AHEAD = 20f;
	final static float MAX_AVOIDANCE_FORCE = 10f;
	final static float TOLERANCE = 0.01f;
	final static float DECELERATING_DEGREE = 1.5f;
	final static float WISH_TIME_R = 1f;
	final static float WISH_TIME_D = 5f;
	final static float DESTINATION_RADIUS = 0.05f;
	final static float DECELERATING_RADIUS = 10f;
	
	
	public Autorennen1(Info info) {
		super(info);
	
	}

	@Override
	public String getName() {
		return "BURNING DESIRE";
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
		
		float rvX = (float)cx - x;
		float rvY = (float) cy - y;
		float degree = (float)Math.atan2(rvY, rvX);
		//für Winkelberechnungen
		float diff = degree - o;
		//für Abstandsberechnungen
		float abs = (float) Math.sqrt((rvX*rvX)+(rvY*rvY));
		
		float svX = (float) (MAX_SEE_AHEAD * Math.cos(o));
		float svY = (float) (MAX_SEE_AHEAD * Math.sin(o));
		Vector2f sv = new Vector2f(svX, svY);
		
		Vector2f svMinus20G = new Vector2f((float)(Math.cos(o) - 0.3491f) , (float)(Math.sin(o) - 0.3491f)  );
	    Vector2f svMinus45G = new Vector2f((float)(Math.cos(o) - 0.785f)  , (float)(Math.sin(o) - 0.785f)   );
	    
	    
	    Vector2f svPlus20G = new Vector2f((float)(Math.cos(o) + 0.3491f) ,  (float)(Math.sin(o) + 0.3491f) );
	    Vector2f svPlus45G = new Vector2f((float)(Math.cos(o) + 0.785)   ,  (float)(Math.sin(o) + 0.785f)  );
	    
	    
	    Vector2f svMinus20GEndpunkt = Vector2f.add((Vector2f)svMinus20G.scale(MAX_SEE_AHEAD), pos, null);
	    Vector2f svMinus45GEndpunkt = Vector2f.add((Vector2f)svMinus45G.scale(MAX_SEE_AHEAD), pos, null);
	    
	    
	    Vector2f svPlus20GEndpunkt = Vector2f.add((Vector2f)svPlus20G.scale(MAX_SEE_AHEAD), pos, null);
	    Vector2f svPlus45GGEndpunkt = Vector2f.add((Vector2f)svPlus45G.scale(MAX_SEE_AHEAD), pos, null);
	    
	    Vector2f[] svEndPoints = {svPlus20GEndpunkt, svPlus45GGEndpunkt, svMinus45GEndpunkt, svMinus20GEndpunkt};
	    
		action = seek(pos, checkpoint);
		
		//Implement flee behaviour
		Polygon[] obstacles = info.getTrack().getObstacles();
		
		if(obstacles.length>2){
			Polygon[] mainO = new Polygon[obstacles.length-2];
			
			
			Vector2f[] obstacleBound ;
			for(int i = 0; i<mainO.length; i++){
				mainO[i] = obstacles[i+2];
				float[] obstacleInfo = obstacleInfo(mainO[i]);
				obstacleInfo[2] = obstacleInfo[2]+20; //Radius Plus !!
				Vector2f p = Vector2f.add(pos, sv, null);
				Vector2f mp = new Vector2f(obstacleInfo[0], obstacleInfo[1]);
				Vector2f pMPRV = Vector2f.sub(mp, p, null);
				float abstMPO = pMPRV.length();
				Vector2f avoidance_force = new Vector2f();
				
				if(abstMPO <obstacleInfo[2])
					avoidance_force = (Vector2f.sub(info.getVelocity(), mp, null).normalise(null));
				
				float avoid_steer = (float)Math.atan2(avoidance_force.getX(), avoidance_force.getY());
				action[1] = action[1] +avoid_steer;
				
				
				
//				obstacleBound = obstacleBounds(mainO[i]);
//				Vector2f start = isLeft(mainO[i], obstacleBound, svEndPoints);
//				if(start!=null){
//					
//					float fleeX = (float)start.x - pos.x;
//					float fleeY = (float)start.y - pos.y;
//					float deg = (float)Math.atan2(fleeY, fleeX);
//					//für Winkelberechnungen
//					float dif = info.getOrientation()- degree;
//					
//					diff = checkDegree(diff);
//					action[0] = arrive(start, pos);
//					action[1] = align(diff);
//					
					
//				}
			}
		}
//		
//		
//		//Kreis ziehen
//		float minX = info.getTrack().getWidth();
//		float maxX = 0;
//		float minY = info.getTrack().getHeight();
//		float maxY = 0;
//		
//		//Berechne min und max von x und y des Hindernisses
//		for(int i =0; i<mainO.npoints; i++){
//			float ox = mainO.xpoints[i];
//			float oy = mainO.ypoints[i];
//			if(ox<minX)
//				minX = ox;
//			else if(ox > maxX)
//				maxX = ox;
//			
//			if(oy<minY)
//				minY = oy;
//			else if(oy>maxY)
//				maxY = oy;
//		}
//		
//		float oWidth = maxX -minX;
//		float oHeight = maxY - minY;
//		float radius = 0;
//		if(oWidth == oHeight)
//			radius=0.5f * oWidth;
//		
//		//Berechne MP des Hindernisses
//		float mpX = minX + radius;
//		float mpY = minY + radius;
//		
//		float radiusPlus = radius + 20;
//		
//		//Berechne Punkt am Ende des Sichtvektors
//		float px = x + svX;
//		float py = y + svY;
//		
//		//Berechne Abstand zum Mittelpunkt des Hindernisses
//		float pMPRVX = mpX -px;
//		float pMPRVY = mpY -py;
//		Vector2f mp = new Vector2f(mpX, mpY);
//		float abstMPO = (float) Math.sqrt((pMPRVX*pMPRVX) + (pMPRVY*pMPRVY)); 
//		Vector2f avoidance_force = new Vector2f();
//		if(abstMPO <= radiusPlus)
//			avoidance_force = (Vector2f.sub(info.getVelocity(), mp, null).normalise(null));
//		
//		float avoid_steer = (float)Math.atan2(avoidance_force.getX(), avoidance_force.getY());
//		action[1]  = action[1] + avoid_steer;
//		}
		
		
		
		
		if(Math.abs(action[1])<0.2){
			action[0] = info.getMaxAcceleration();
		}

		
		//berechnetes throttle und steering anwenden
		return new DriverAction(action[0], action[1]);
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
	
	
	@Override
	public void doDebugStuff(){
		//Testkomponenten -> später in doDebugStuff auslagern!!
		
		float o = info.getOrientation();
		double cx = info.getCurrentCheckpoint().getX();
		double cy = info.getCurrentCheckpoint().getY();
		float x = info.getX();
		float y = info.getY();
		float rvX = (float)cx - x;
		float rvY = (float) cy - y;
		float degree = (float)Math.atan2(rvY, rvX);
		float diff = degree - o;
//		System.out.println("Checkpoint: " + cx + " " + cy);
//		System.out.println("My pos: " + x + " " + y);
//		System.out.println("Throttle: " + throttle);
//		System.out.println("Steering: " + steering);
//		System.out.println("Degree: " + degree);
//		System.out.println("Orient. : " + o);
//		System.out.println("Diff: " + diff);
		
//		Polygon[] allO = info.getTrack().getObstacles();
//		System.out.println("All Obstac: " +allO.length);
//		
//		Polygon o1 = allO[0];
//		System.out.println("O1: " +o1.npoints);
//		Polygon o2 = allO[1];
//		System.out.println("O2: " +o2.npoints);
		
	}
}
