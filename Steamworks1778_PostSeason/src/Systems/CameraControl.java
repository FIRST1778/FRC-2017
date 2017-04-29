package Systems;

import NetworkComm.InputOutputComm;
import Utility.HardwareIDs;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Utility;

public class CameraControl {
	private static boolean initialized = false;
		
	private static Joystick gamepad;
	
	//********* DEBUG (PROTOBOT) ONLY *************
	// assumes HS-475HB servo, which 1.0 = 90 degrees (PROTOBOT)
	//public static final double GEAR_CAM_POS = 0.0;     // 0 deg
	//public static final double BOILER_CAM_POS = (GEAR_CAM_POS + 0.125);  // 11.25 deg = 90 * 0.125
	//*********************************************
	
	// assumes HS-485HB servo, which 1.0 = 180 degrees (COMPETITION BOT)
	public static final double GEAR_CAM_POS = 0.04;     // 0 deg (with minor position adj)
	public static final double BOILER_CAM_POS = (GEAR_CAM_POS + 0.15);  // 27 deg = 180 * 0.15

	//public static final double BOILER_CAM_POS = (GEAR_CAM_POS + 0.5);  // 90 deg
	
	private static final double SERVO_POS_TOLERANCE = 0.005;
	
	// Relay for extra LEDs
	private static Relay cameraLedRelay;
	private static boolean ledState = false;
	
	// camera position servo
	private static Servo positionServo;

	// wait 0.25 s between button pushes on shooter
    private static final int TRIGGER_CYCLE_WAIT_US = 250000;
    private static double initTriggerTime;

	public static void initialize() {
		if (initialized)
			return;
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();		

		cameraLedRelay = new Relay(HardwareIDs.CAMERA_LED_RELAY_CHANNEL,Relay.Direction.kForward);
		cameraLedRelay.set(Relay.Value.kOff);
		
		positionServo = new Servo(HardwareIDs.CAMERA_SERVO_PWM_ID);
		
		gamepad = new Joystick(HardwareIDs.DRIVER_CONTROL_ID);
		
		ledState = false;
		
		initialized = true;
	}
	
	public static void moveToPos(double position) {
		if ((position < GEAR_CAM_POS) || (position > BOILER_CAM_POS))
			return;
		
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"CameraControl/Angle", position*180.0);
		
		positionServo.set(position);
	}
	
	public static void setCameraLed(boolean state) {
		
		if (state == true) {
			cameraLedRelay.set(Relay.Value.kOn);
			ledState = true;
		}
		else {
			cameraLedRelay.set(Relay.Value.kOff);
			ledState = false;
		}
		
	}
	
	public static void autoInit() {
		
		setCameraLed(true);
	}
	
	public static void teleopInit() {
		
		setCameraLed(false);
	}
	
	public static void teleopPeriodic() {

		// fire controls - using a timer to debounce
		double currentTime = Utility.getFPGATime();

		// if not enough time has passed, no polling allowed!
		if ((currentTime - initTriggerTime) < TRIGGER_CYCLE_WAIT_US)
			return;
		
		double currentPos = positionServo.get();
		
		// switch to control camera servo
		if (gamepad.getRawButton(HardwareIDs.CAMERA_CONTROL_BUTTON) == true)
		{
			if (Math.abs(currentPos - BOILER_CAM_POS) > SERVO_POS_TOLERANCE)
				moveToPos(BOILER_CAM_POS);
		}
		else
		{
			if (Math.abs(currentPos - GEAR_CAM_POS) > SERVO_POS_TOLERANCE)
				moveToPos(GEAR_CAM_POS);
		}

		// button to strobe camera LED rings
		if ((gamepad.getRawButton(HardwareIDs.CAMERA_LED_STROBE_BUTTON) == true) && (!ledState))
		{
			setCameraLed(true);
		}
		else if ((gamepad.getRawButton(HardwareIDs.CAMERA_LED_STROBE_BUTTON) == false) && (ledState))
		{
			setCameraLed(false);
		}
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();		

	}
}
