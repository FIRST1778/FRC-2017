package Systems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Joystick;

public class ClimberAssembly {
	
	private static final double SPEED_MULTIPLIER = 1.0;
	
	private static boolean initialized = false;
	private static final int TALON_ID = 1;
	private static CANTalon climberMotor;
	
	private static final int GAMEPAD_ID = 0;
	private static Joystick gamepad;
	
	public static void initialize() {
		if (initialized) 
			return;
		
		climberMotor = new CANTalon(TALON_ID);
		gamepad = new Joystick(GAMEPAD_ID);
	}
	
	public static void teleopInit() {
		if (!initialized) 
			initialize();
			
	}
	
	public static void teleopPeriodic() {
		
		double climberSpeed = gamepad.getRawAxis(1);
		
		climberMotor.set(climberSpeed*SPEED_MULTIPLIER);
	}
	
	
}
