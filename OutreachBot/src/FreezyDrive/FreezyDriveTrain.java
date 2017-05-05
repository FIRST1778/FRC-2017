package FreezyDrive;

import edu.wpi.first.wpilibj.Spark;

public class FreezyDriveTrain {
	
	// TalonSRX IDs
	private static final int LEFT_SPARK_ID = 0;
	private static final int RIGHT_SPARK_ID = 1;

	// Initalizing TalonSRXs
	private static Spark motorL,motorR;
	private static boolean initialized = false;

	private static DriveControl driveControl;
	
	public static void initialize() {
		
		if (initialized)
			return;
		
		motorL = new Spark(LEFT_SPARK_ID);
		motorR = new Spark(RIGHT_SPARK_ID);
		
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
   	 		Controller.Driver_isQuickTurn());
		
	}
}