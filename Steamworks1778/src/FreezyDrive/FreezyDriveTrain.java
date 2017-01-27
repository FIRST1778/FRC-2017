package FreezyDrive;
import Utility.SimpleUtil;

import com.ctre.CANTalon;

public class FreezyDriveTrain {
	
	// Speed controller IDs
	//private static final int LEFT_FRONT_TALON_ID = 8;
	//private static final int LEFT_REAR_TALON_ID = 4;
	//private static final int RIGHT_FRONT_TALON_ID = 3;
	//private static final int RIGHT_REAR_TALON_ID = 7;
	private static final int LEFT_FRONT_TALON_ID = 3;
	private static final int LEFT_REAR_TALON_ID = 7;
	private static final int RIGHT_FRONT_TALON_ID = 8;
	private static final int RIGHT_REAR_TALON_ID = 4;

	private static CANTalon motorFrontL,motorFrontR;
	private static CANTalon motorRearL,motorRearR;
	private static boolean initialized = false;

	private static DriveControl driveControl;
	
	private static double oldWheel, quickStopAccumulator;
    private static double throttleDeadband = 0.02;
    private static double wheelDeadband = 0.02;
	
	public static void initialize() {
		
		if (initialized)
			return;
		
		motorFrontL = new CANTalon(LEFT_FRONT_TALON_ID);
		motorFrontR = new CANTalon(RIGHT_FRONT_TALON_ID);
		motorRearL = new CANTalon(LEFT_REAR_TALON_ID);
		motorRearR = new CANTalon(RIGHT_REAR_TALON_ID);
		
		driveControl = new DriveControl();
		
		Controller.initialize();
		
		initialized = true;
	}
	
	// call to change the power given to the motor
	public static void ChangeSpeed(double powerL,double powerR){
		motorFrontL.set(powerL);
		motorRearL.set(powerL);
		motorFrontR.set(powerR);
		motorRearR.set(powerR);
	}
	
	public static void teleopInit()
	{
		
	}
	
	public static void teleopPeriodic()
	{
    	// drive command for all controllers
   	 	driveControl.calculateDrive(Controller.Driver_Throttle(), Controller.Driver_Steering(),
   	 		Controller.Driver_isQuickTurn(), false);
		
	}
}