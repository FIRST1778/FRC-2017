package Systems;

import com.ctre.CANTalon;

import NetworkComm.InputOutputComm;
import Utility.HardwareIDs;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;

public class ClimberAssembly {
	private static boolean initialized = false;
	
	private static final double CLIMBER_MOTOR_DEAD_ZONE = 0.2;

	// apply speed conversion factor from joystick to motor
	// UP joystick = NEG motor throttle = correct!
	private static final double CLIMB_MOTOR_FACTOR = 1.0;
	
	private static CANTalon climberMotor;
	private static Joystick gamepad;
	private static double currentClimbValue = 0.0;
	
	public static void initialize() {
		if (initialized)
			return;
		
		climberMotor = new CANTalon(HardwareIDs.CLIMBER_TALON_ID);
				
		gamepad = new Joystick(HardwareIDs.GAMEPAD_ID);
		
	}
			
	public static void teleopInit() {
		currentClimbValue = 0.0;
	}
	
	public static void teleopPeriodic() {

		double newClimbValue = gamepad.getRawAxis(HardwareIDs.CLIMBER_MOTOR_AXIS);
		
		// convert joystick value into motor speed value
		if ((Math.abs(newClimbValue) < CLIMBER_MOTOR_DEAD_ZONE) || (newClimbValue > 0.0))
			newClimbValue= 0.0;
		else if ((Math.abs(newClimbValue) >= CLIMBER_MOTOR_DEAD_ZONE) && (newClimbValue < 0.0))
			newClimbValue *= CLIMB_MOTOR_FACTOR;      
		
		// if current climber motor is set to this value already, just return
		if (newClimbValue == currentClimbValue)
			return;
		
		// set motor and persisted climb value
		currentClimbValue = newClimbValue;
		climberMotor.set(newClimbValue);
		
		String climbValueStr = String.format("%.2f", newClimbValue);
		InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"Climber/speed", climbValueStr);
	}
}
