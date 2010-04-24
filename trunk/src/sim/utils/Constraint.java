package sim.utils;

public class Constraint {
	public static double constraint(double value, double upperLimit, double lowerLimit) {
		if (value > upperLimit) {
			return upperLimit;
		}
		else if (value < lowerLimit) {
			return lowerLimit;
		}
		else {
			return value;
		}
	}
}
