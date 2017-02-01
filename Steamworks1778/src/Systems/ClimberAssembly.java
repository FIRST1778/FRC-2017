package Systems;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;

public class ClimberAssembly {
	private static boolean initialized = false;
	
	private static final int CLIMBER_TALON_ID = 12;
	private static final int CLIMBER_RELAY_CHANNEL = 2;
	
	private static final int GAMEPAD_ID = 1;
	private static final int CLIMBER_CONTROL_BUTTON = 8;
	private static final int CLIMBER_MOTOR_AXIS = 5;

	private static final double CLIMBER_MOTOR_DEAD_ZONE = 0.05;
	
	private static Relay climberRelay;
	private static CANTalon climberMotor;
	private static Joystick gamepad;
	
	public static void initialize() {
		if (initialized)
			return;
		
		climberMotor = new CANTalon(CLIMBER_TALON_ID);
		
		climberRelay = new Relay(CLIMBER_RELAY_CHANNEL,Relay.Direction.kForward);
		climberRelay.set(Relay.Value.kOff);
		
		gamepad = new Joystick(GAMEPAD_ID);
	}
			
	public static void teleopInit() {
		
	}
	
	public static void teleopPeriodic() {
		if (gamepad.getRawButton(CLIMBER_CONTROL_BUTTON))
			climberRelay.set(Relay.Value.kOn);
			
		double climbValue = gamepad.getRawAxis(CLIMBER_MOTOR_AXIS);
		if (Math.abs(climbValue) < CLIMBER_MOTOR_DEAD_ZONE)
			climbValue= 0.0;
		
		if (climbValue != 0.0)
			climberMotor.set(climbValue);
			
			
	}
}
