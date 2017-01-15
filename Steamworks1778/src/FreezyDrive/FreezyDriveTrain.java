package FreezyDrive;
import Utility.SimpleUtil;

import com.ctre.CANTalon;

public class FreezyDriveTrain {
	
	// Speed controller IDs
	private static final int LEFT_FRONT_TALON_ID = 8;
	//private static final int LEFT_REAR_TALON_ID = 4;
	private static final int RIGHT_FRONT_TALON_ID = 3;
	//private static final int RIGHT_REAR_TALON_ID = 7;

	private static CANTalon motorL,motorR;
	private static boolean initialized = false;

	private static DriveControl driveControl;
	
	private static double oldWheel, quickStopAccumulator;
    private static double throttleDeadband = 0.02;
    private static double wheelDeadband = 0.02;
	
	public static void initialize() {
		
		if (initialized)
			return;
		
		motorL = new CANTalon(LEFT_FRONT_TALON_ID);
		motorR = new CANTalon(RIGHT_FRONT_TALON_ID);
		
		driveControl = new DriveControl();
		
		Controller.initialize();
		
		initialized = true;
	}
	
	// call to change the power given to the motor
	public static void ChangeSpeed(double powerL,double powerR){
		motorL.set(powerL);
		motorR.set(powerR);
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