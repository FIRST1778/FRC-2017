package Systems;

//import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.Timer;

public class UltrasonicSensor {
	
	//private static double vcc = 5.0;			
	//private static final int ANALOG_CHANNEL_ID = 0;
	//private static AnalogInput ultrasonicDevice;
	
	private static final int TRIGGER_CHANNEL_ID = 0;
	private static final int ECHO_CHANNEL_ID = 1;
	private static Ultrasonic ultrasonicDevice;
	private static boolean initialized = false;

	public static void initialize()
	{
		if (!initialized) {
			//ultrasonicDevice = new AnalogInput(ANALOG_CHANNEL_ID);
			ultrasonicDevice = new Ultrasonic(TRIGGER_CHANNEL_ID,ECHO_CHANNEL_ID);
			ultrasonicDevice.setAutomaticMode(true);
			
			initialized = true;
		}
	}
	
	public static double getRange() {
		if (!initialized)
			initialize();
		
		//Timer.delay(0.05);
		
		//double vm = vcc/512.0;
		//double vi = ultrasonicDevice.getVoltage();
		//double ri = (vi/vm) - BIAS_VALUE;
		
		return ultrasonicDevice.getRangeInches();
	}
}
