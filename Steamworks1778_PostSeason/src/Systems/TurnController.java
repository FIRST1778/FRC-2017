package Systems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;

public class TurnController {

	private static PIDController pidCtrl;
	
	// comp.bot - tuned 7/20/2017
	private static final double kP = 0.075;
	private static final double kI = 0.0;   // I not needed for PID position control
	private static final double kD = 0.14;
	private static final double kF = 0;     // F not needed for PID position control
		
	// proto.bot - tuned version
	//private static final double kP = 0.04;
	//private static final double kI = 0;     // I not needed for PID position control
	//private static final double kD = 0.175;
	//private static final double kF = 0;     // F not needed for PID position control
	
	private static final double maxSpeed = 0.5;
	
	private static double angleTargetDeg = 0.0;	
	private static TurnOutput pidOut;
	
	private static AHRS ahrs;
	
	private static boolean initialized = false;
	
	public static void initialize() {
		
		if (initialized)
			return;
		
		ahrs = NavXSensor.getAHRS();
		
		pidOut = new TurnOutput();

		pidCtrl = new PIDController(kP,kI,kD,kF,ahrs,pidOut);
		
		pidCtrl.setInputRange(-180.0, 180.0);
		pidCtrl.setOutputRange(-maxSpeed, maxSpeed);
		//pidCtrl.setAbsoluteTolerance(0.5);
		pidCtrl.setContinuous(true);
		
		initialized = true;
	}

	public static void setAngle(double angleDeg) {
		
		angleTargetDeg = angleDeg;
		pidCtrl.setOutputRange(-maxSpeed, maxSpeed);
		pidCtrl.setSetpoint(angleTargetDeg);
	}

	public static void setAngle(double angleDeg, double speed) {
		
		angleTargetDeg = angleDeg;
		pidCtrl.setOutputRange(-speed, speed);
		pidCtrl.setSetpoint(angleTargetDeg);
	}
	
	public static void reset() {
		disable();
		NavXSensor.reset();
		angleTargetDeg = 0.0;
	}
	
	public static void enable() {
		pidCtrl.enable();
	}
	
	public static void disable() {
		pidCtrl.disable();
	}
	
	public static double getLeft() {
		return pidOut.getValue();
	}
	
	public static double getRight() {
		return -pidOut.getValue();
	}
	
	public static class TurnOutput implements PIDOutput {
		private double myOutput = 0.0;
		
		public double getValue() {
			return myOutput;
		}
		
		public void pidWrite(double output) {
			myOutput = output;
		}
	}	
}
