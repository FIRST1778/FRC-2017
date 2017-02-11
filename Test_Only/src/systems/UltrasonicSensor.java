package systems;

//import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Ultrasonic;
import utility.HardwareIDs;

public class UltrasonicSensor {
		
	private static Ultrasonic ultrasonicDevice;
	private static boolean initialized = false;

	public static void initialize()
	{
		if (!initialized) {
			ultrasonicDevice = new Ultrasonic(HardwareIDs.TRIGGER_CHANNEL_ID,HardwareIDs.ECHO_CHANNEL_ID);
			ultrasonicDevice.setAutomaticMode(true);
			
			initialized = true;
		}
	}
	
	public static double getRange() {
		if (!initialized)
			initialize();
				
		return ultrasonicDevice.getRangeInches();
	}
}
