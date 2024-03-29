package sim.airport;

import java.util.ArrayList;
import java.util.List;

import sim.aircraft.Aeroplane;
import sim.globalvalue.GlobalValue;
import sim.utils.Constants;
import sim.utils.Constraint;
import sim.utils.GlideSlopeUtils;
import sim.utils.Waypoint;

public abstract class Airport {
	public String runway;
	private double landHeight;
	private double runwayDirection;
	private Waypoint runwayStartingPoint;
	private Waypoint runwayEndingPoint;
	private double landingPointAltitude;
	private static double GLIDESLOPEINDEX[] = { 4300, 3500, 3000, 2500, 2100,
			1700, 1500, 1300, 1100, 900, 700, 500, 400, 300, 200, 100, 50, 0, 0 };
	public static double GLIDESLOPETANGENT = 0.09;
	private static double GLIDESLOPEPOINTRAIUSCONST = 1.0 / 15;
	public List<Waypoint> glideSlope;
	
	public static double RETIFIED_GS_AMP_COEFFICIENT = 10;

	public void setRunwayDirection(double direction) {
		this.runwayDirection = direction;
	}

	public double getRunwayDirection() {
		return runwayDirection;
	}

	public Waypoint getRunwayStartingPoint() {
		return runwayStartingPoint;
	}

	public void setRunwayStartingPoint(Waypoint landingPoint) {
		this.runwayStartingPoint = landingPoint;
	}

	public Waypoint getRunwayEndingPoint() {
		return runwayEndingPoint;
	}

	public void setRunwayEndingPoint(Waypoint takingoffPoint) {
		this.runwayEndingPoint = takingoffPoint;
	}

	public double getLandingPointAltitude() {
		return landingPointAltitude;
	}

	public void setLandingPointAltitude(double landingPointAltitude) {
		this.landingPointAltitude = landingPointAltitude;
	}

	public List<Waypoint> getGlideSlope() {
		glideSlope = new ArrayList<Waypoint>();

		double bearing = runwayDirection - 180;

		if (bearing < 0) {
			bearing += 360;
		}

		double latitude = runwayStartingPoint.getLatitude();
		double longitude = runwayStartingPoint.getLongitude();
		double prevDistance = 4000;
		double dDistance = 0;

		for (double distance : GLIDESLOPEINDEX) {
			if (distance == 0) {
				break;
			}
			dDistance = prevDistance - distance;
			Waypoint waypoint = GlideSlopeUtils
					.generateWaypointByDistanceAndBearing(latitude, longitude,
							distance, bearing);
			double localHeight = distance * GLIDESLOPETANGENT
					/ Constants.FOOTTOMETER + landHeight
					+ GlobalValue.SAFETYHEIGHT;
			localHeight = Constraint.constraint(localHeight,
					Aeroplane.DEFAULT_NAVIGATION_HEIGHT, 0);
			waypoint.setHeight(localHeight);
			waypoint.setApproachRadius(dDistance * GLIDESLOPEPOINTRAIUSCONST
					+ GlobalValue.TOLERANCERANGE);
			waypoint
					.setSpeedLimit(Aeroplane.ROLLOUT_VELOCITY
							+ (Aeroplane.DEFAULT_NAVIGATION_SPEED - Aeroplane.ROLLOUT_VELOCITY)
							/ GLIDESLOPEINDEX[2] * distance);
			prevDistance = distance;

			glideSlope.add(waypoint);
		}

		glideSlope.add(new Waypoint("Roll_Out", latitude, longitude, 7,
				landHeight + 15, 65));
		glideSlope.add(new Waypoint("Runway_End", runwayEndingPoint
				.getLatitude(), runwayEndingPoint.getLongitude(), 5,
				landHeight, 0));

		return glideSlope;
	}

	public List<Waypoint> getRevisedGlideSlope(int nextWaypointIndex,
			boolean isClockWise) {
		List<Waypoint> newGlideSlope = new ArrayList<Waypoint>();

		double bearing = runwayDirection;
		if (isClockWise) {
			bearing += 90;
		} else {
			bearing -= 90;
		}
		if (bearing >= 360) {
			bearing -= 360;
		} else if (bearing <= 0) {
			bearing += 360;
		}

		for (int i = 0; i < glideSlope.size(); i++) {
			if (i < nextWaypointIndex) {
				newGlideSlope.add(glideSlope.get(i));
			} else {
				double distance = 0;
				distance = GLIDESLOPEINDEX[i] / Constants.KILO;
				distance *= distance;
				distance *= RETIFIED_GS_AMP_COEFFICIENT; /* NEED TO BE TUNED */
				if (distance > 1) {
					Waypoint newWaypoint = GlideSlopeUtils
							.generateWaypointByDistanceAndBearing(glideSlope
									.get(i).getLatitude(), glideSlope.get(i)
									.getLongitude(), distance, bearing);
					newWaypoint.setApproachRadius(glideSlope.get(i)
							.getApproachRadius());
					newWaypoint.setHeight(glideSlope.get(i).getHeight());
					newWaypoint
							.setSpeedLimit(glideSlope.get(i).getSpeedLimit());
					newGlideSlope.add(newWaypoint);
				} else {
					newGlideSlope.add(glideSlope.get(i));
				}
			}
		}

		return newGlideSlope;
	}

	public double getLandHeight() {
		return landHeight;
	}

	public void setLandHeight(double landHeight) {
		this.landHeight = landHeight;
	}
}
