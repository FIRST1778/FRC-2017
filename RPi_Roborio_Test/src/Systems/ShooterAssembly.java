package Systems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Utility;

public class ShooterAssembly {
	
	private static boolean initialized = false;

	private static final int TALON_ID = 0;
	
	public static final int MOTOR_OFF = 0;
	public static final int MOTOR_VERY_LOW = 1;
	public static final int MOTOR_LOW = 2;
	public static final int MOTOR_MEDIUM = 3;
	public static final int MOTOR_HIGH = 4;
	public static final int MOTOR_VERY_HIGH = 5;
	public static final int MOTOR_MAX = 6;

	private static final double motorSettings[] = { 0.0, 0.1, 0.25, 0.5, 0.75, 0.9, 1.0 };
	private static final int NUM_MOTOR_SETTINGS = 7;
	
	private static double currentMotorIndex = 0;
	
	private static CANTalon shooterMotor;
	private static boolean enabled = false;
	
	private static final int GAMEPAD_ID = 0;
	private static Joystick gamepad;
	
	private static final int FIRE_BUTTON = 0;
	private static final int HOLD_BUTTON = 1;
	
    private static final int TRIGGER_CYCLE_WAIT_US = 1000000;
    private static double initTriggerTime;
    
	public static void initialize() {
		if (initialized)
			return;
		
		shooterMotor = new CANTalon(TALON_ID);
		currentMotorIndex = MOTOR_MEDIUM;
		
		gamepad = new Joystick(GAMEPAD_ID);
		
		initialized = true;
	}
	
	public static void setMotorStrength(int newIndex) {
		
		if (!initialized)
			initialize();
		
		// if out of range, just return
		if ((newIndex > MOTOR_VERY_HIGH) || (newIndex < MOTOR_VERY_LOW))
			return;
		
		shooterMotor.set(motorSettings[newIndex]);
		
	}
	
	public static void fire() {
		// toggle fire control on
		enabled = true;
	}
	
	public static void hold() {
		// toggle fire control off
		enabled = false;
	}
	
	public static void teleopInit() {
		setMotorStrength(MOTOR_MEDIUM);
        initTriggerTime = Utility.getFPGATime();
	}
	
	public static void teleopPeriodic() {
		double currentTime = Utility.getFPGATime();

		// if not enough time has passed, no polling allowed!
		if ((currentTime - initTriggerTime) < TRIGGER_CYCLE_WAIT_US)
			return;

		if (gamepad.getRawButton(FIRE_BUTTON))  {
			fire();
			
			// reset trigger init time
			initTriggerTime = Utility.getFPGATime();
		}
		
		if (gamepad.getRawButton(HOLD_BUTTON)) {
			hold();	
			
			// reset trigger init time
			initTriggerTime = Utility.getFPGATime();
		}
		
	}
	
}
