package Systems;

import com.ctre.CANTalon;

import NetworkComm.InputOutputComm;
import Utility.HardwareIDs;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;

public class ClimberAssembly {
	private static boolean initialized = false;
	
	private static final double CLIMBER_MOTOR_DEAD_ZONE = 0.2;

	// apply inversion factor so that UP on the joystick is positive	
	private static final double CLIMB_MOTOR_FACTOR = 1.0;
	
	private static CANTalon climberMotor;
	private static Joystick gamepad;
	
	public static void initialize() {
		if (initialized)
			return;
		
		climberMotor = new CANTalon(HardwareIDs.CLIMBER_TALON_ID);
				
		gamepad = new Joystick(HardwareIDs.GAMEPAD_ID);
	}
			
	public static void teleopInit() {
		
	}
	
	public static void teleopPeriodic() {
		/*	
		double climbValue = gamepad.getRawAxis(HardwareIDs.CLIMBER_MOTOR_AXIS);
		if ((Math.abs(climbValue) < CLIMBER_MOTOR_DEAD_ZONE) || (climbValue > 0.0))
			climbValue= 0.0;
		
		// only send positive motor values (locking ratchet in place - no negative values!)
		if (climbValue < 0.0) {
			climbValue *= CLIMB_MOTOR_FACTOR;      
			climberMotor.set(climbValue);
		}
		
		String climbValueStr = String.format("%.2f", climbValue);
		InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"Climber/speed", climbValueStr);
		*/			
	}
}
