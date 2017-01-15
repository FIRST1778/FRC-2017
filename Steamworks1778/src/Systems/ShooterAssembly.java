package Systems;

import com.ctre.CANTalon;

import NetworkComm.InputOutputComm;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Utility;

public class ShooterAssembly {
	
	private static boolean initialized = false;

	private static final int TALON_ID = 6;
	
	public static final int MOTOR_OFF = 0;
	public static final int MOTOR_VERY_LOW = 1;
	public static final int MOTOR_LOW = 2;
	public static final int MOTOR_MEDIUM = 3;
	public static final int MOTOR_HIGH = 4;
	public static final int MOTOR_VERY_HIGH = 5;
	public static final int MOTOR_MAX = 6;

	private static final double motorSettings[] = { 0.0, -0.1, -0.4, -0.6, -0.8, -0.9, -1.0 };
	private static final int NUM_MOTOR_SETTINGS = 7;
	
	private static double currentMotorIndex = 0;
	
	private static CANTalon shooterMotor;
	
	private static final int GAMEPAD_ID = 0;
	private static Joystick gamepad;
	
	private static final int FIRE_HIGH_BUTTON = 2;
	private static final int FIRE_MEDIUM_BUTTON = 1;
	private static final int FIRE_LOW_BUTTON = 3;
	private static final int HOLD_BUTTON = 4;
	
    private static final int TRIGGER_CYCLE_WAIT_US = 1000000;
    private static double initTriggerTime;
    
	public static void initialize() {
		if (initialized)
			return;
		
		shooterMotor = new CANTalon(TALON_ID);
		currentMotorIndex = MOTOR_OFF;
		shooterMotor.set(motorSettings[MOTOR_OFF]);
		
		gamepad = new Joystick(GAMEPAD_ID);
		
		initialized = true;
	}
	
	public static void setMotorStrength(int newIndex) {
		
		if (!initialized)
			initialize();
		
		// if out of range, just return
		if ((newIndex > MOTOR_MAX) || (newIndex < MOTOR_OFF))
			return;
		
		//System.out.println("Motor Strength = " + motorSettings[newIndex]);
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Teleop/ShooterStrength", motorSettings[newIndex]);		
		
		shooterMotor.set(motorSettings[newIndex]);
		
	}
	
	public static void fireLevel(int motorLevel) {
		
		setMotorStrength(motorLevel);
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();
	}
		
	public static void teleopInit() {
		setMotorStrength(MOTOR_OFF);
        initTriggerTime = Utility.getFPGATime();
	}
	
	public static void teleopPeriodic() {
		
		double currentTime = Utility.getFPGATime();

		// if not enough time has passed, no polling allowed!
		if ((currentTime - initTriggerTime) < TRIGGER_CYCLE_WAIT_US)
			return;

		if (gamepad.getRawButton(FIRE_HIGH_BUTTON))
			fireLevel(MOTOR_HIGH);			
		
		if (gamepad.getRawButton(FIRE_MEDIUM_BUTTON))
			fireLevel(MOTOR_MEDIUM);			

		if (gamepad.getRawButton(FIRE_LOW_BUTTON)) 
			fireLevel(MOTOR_LOW);			
		
		if (gamepad.getRawButton(HOLD_BUTTON)) {
			fireLevel(MOTOR_OFF);	
		}
		
	}
	
}
