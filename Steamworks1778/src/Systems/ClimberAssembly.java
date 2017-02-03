package Systems;

import com.ctre.CANTalon;
import Utility.HardwareIDs;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;

public class ClimberAssembly {
	private static boolean initialized = false;
	
	private static final double CLIMBER_MOTOR_DEAD_ZONE = 0.05;
	
	private static Relay climberRelay;
	private static CANTalon climberMotor;
	private static Joystick gamepad;
	
	public static void initialize() {
		if (initialized)
			return;
		
		climberMotor = new CANTalon(HardwareIDs.CLIMBER_TALON_ID);
		
		climberRelay = new Relay(HardwareIDs.CLIMBER_RELAY_CHANNEL,Relay.Direction.kForward);
		climberRelay.set(Relay.Value.kOff);
		
		gamepad = new Joystick(HardwareIDs.GAMEPAD_ID);
	}
			
	public static void teleopInit() {
		
	}
	
	public static void teleopPeriodic() {
		if (gamepad.getRawButton(HardwareIDs.CLIMBER_CONTROL_BUTTON))
			climberRelay.set(Relay.Value.kOn);
			
		double climbValue = gamepad.getRawAxis(HardwareIDs.CLIMBER_MOTOR_AXIS);
		if (Math.abs(climbValue) < CLIMBER_MOTOR_DEAD_ZONE)
			climbValue= 0.0;
		
		if (climbValue != 0.0)
			climberMotor.set(climbValue);
			
			
	}
}
