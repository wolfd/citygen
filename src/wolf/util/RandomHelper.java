package wolf.util;

public class RandomHelper {
	public static float random(float rnd, float range0, float range1){
		return (rnd*(range1-range0))+range0;
	}
}
