package Systems;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Servo;

public class CameraControl {
	private static boolean initialized = false;
	
	private static final int DRIVER_CONTROL_ID = 0;
	
	private static final int SERVO_CHANNEL_ID = 0;
	private static final int CAMERA_CONTROL_BUTTON = 1;
	
	private static Joystick gamepad;
	
	// assumes HS-485HB servo, which 1.0 = 180 degrees
	public static final double BOILER_CAM_POS = 0.25;  // 45 deg
	public static final double GEAR_CAM_POS = 0.0;     // 0 deg
	private static final double SERVO_POS_TOLERANCE = 0.005;
	
	
	private static Servo positionServo;
	
	public static void initialize() {
		if (initialized)
			return;
		
		positionServo = new Servo(SERVO_CHANNEL_ID);
		gamepad = new Joystick(DRIVER_CONTROL_ID);
		
		initialized = true;
	}
	
	public static void moveToPos(double position) {
		if ((position < GEAR_CAM_POS) || (position > BOILER_CAM_POS))
			return;
		
		positionServo.set(position);
	}
	
	public static void teleopInit() {
		
	}
	
	public static void teleopPeriodic() {
		double currentPos = positionServo.get();
		
		if (gamepad.getRawButton(CAMERA_CONTROL_BUTTON) == true)
		{
			if (Math.abs(currentPos - GEAR_CAM_POS) > SERVO_POS_TOLERANCE)
				moveToPos(GEAR_CAM_POS);
		}
		else
		{
			if (Math.abs(currentPos - BOILER_CAM_POS) > SERVO_POS_TOLERANCE)
				moveToPos(BOILER_CAM_POS);
		}
			
	}
}
