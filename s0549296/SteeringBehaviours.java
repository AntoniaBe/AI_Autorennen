package s0549296;

import org.lwjgl.util.vector.Vector2f;

import lenz.htw.ai4g.ai.Info;

public class SteeringBehaviours {
	
	private Info info;
	
	public SteeringBehaviours(Info i){
		this.info = i;
	}
	
	public float[] seek(Vector2f start, Vector2f dest, boolean arrive){
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
	
	final static float DESTINATION_RADIUS = 5f;
	final static float DECELERATING_RADIUS = 50f; //zuvor:10
	final static float WISH_TIME_D = 0.1f;
	
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
	
	final static float TOLERANCE = 0.01f;
	final static float DECELERATING_DEGREE = 1.5f;
	final static float WISH_TIME_R = 0.5f;
	
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
	
	public float checkDegree(float diff){
		if(diff > Math.PI){
			diff = (float) ((diff - 2 * Math.PI));
		}else if ( diff < -Math.PI){
			diff = (float) ((diff + 2 * Math.PI));
		}
		return diff;
	}
}
