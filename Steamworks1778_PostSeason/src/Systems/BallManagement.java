package Systems;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.FeedbackDeviceStatus;
import com.ctre.CANTalon.TalonControlMode;

import NetworkComm.InputOutputComm;
import Utility.HardwareIDs;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Utility;

public class BallManagement {
	
	private static boolean initialized = false;
	private static boolean feeding = false;
	
	private static final double TRANSPORT_IN_LEVEL = 0.5;
	private static final double TRANSPORT_OUT_LEVEL = -0.5;
	
	private static final double COLLECTOR_IN_LEVEL = -0.75;
	private static final double COLLECTOR_OUT_LEVEL = 0.75;
	
	private static final double FEEDER_LEVEL = 0.3;
	private static final double AGITATOR_ON = 0.75;
	private static final double AGITATOR_OFF = 0.5;
	
	//  10 100ms/s * (60 s/min) * (1 rev/12 Native Units)
	private static final double NATIVE_TO_RPM_FACTOR = 10 * 60 / 12;
	
	private static final double DEAD_ZONE_THRESHOLD = 0.05;
	
	// Shooter PIDF values - comp.bot version - not yet tuned
	private static final double P_COEFF = 3.45;
	private static final double I_COEFF = 0.0;
	private static final double D_COEFF = 0.0;
	private static final double F_COEFF = 9.175; 
	
	public static final int MOTOR_OFF = 0;
	public static final int MOTOR_AUTO_BLUE = 1;
	public static final int MOTOR_AUTO_RED = 2;
	public static final int MOTOR_LOW = 3;
	public static final int MOTOR_MEDIUM = 4;
	public static final int MOTOR_HIGH = 5;

	// Competition bot calibrated native speed settings
	private static final double motorSettings[] = { 0, 45, 45, 45, 50, 55};	    // Speed (Native) control settings
	
	// Percent VBus settings (debug reference only)
	//private static final double motorSettings[] = { 0.0, 0.0, 0.0, 0.25, 0.5, 0.75};   // Vbus (%) control settings

	private static final int NUM_MOTOR_SETTINGS = 6;
	
	// relays to release collector and gear tray
	//private static Relay collectorRelay;
	private static Spark collectorSolenoid;
	private static Relay gearTrayRelay;   // first gear tray solenoid controlled by Relay
	private static Relay gearTrayRelay2;   // second gear tray solenoid controlled by Relay
	
	// shooter and support motors
	private static CANTalon shooterMotor, feederMotor; 
	private static Spark agitatorServo;    //  continuous rotation servo control modeled as Spark PWM
	
	// collector & transport motors
	private static Spark transportMotor;		// moves balls from collector to bin
	private static CANTalon collectorMotor;		// collects balls from floor
	private static boolean collectorEnabled = false;
	
	private static Joystick gamepad;
		
	// wait 0.25 s between button pushes on shooter
    private static final int TRIGGER_CYCLE_WAIT_US = 250000;
    private static double initTriggerTime;
    
	public static void initialize() {
		if (initialized)
			return;

		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();		

        // create and reset collector relay
		collectorSolenoid = new Spark(HardwareIDs.COLLECTOR_SOLENOID_PWM_ID);
                
        // create and reset gear tray spark & relay
        gearTrayRelay = new Relay(HardwareIDs.GEAR_TRAY_RELAY_CHANNEL_1,Relay.Direction.kForward);
        gearTrayRelay.set(Relay.Value.kOff);
        gearTrayRelay2 = new Relay(HardwareIDs.GEAR_TRAY_RELAY_CHANNEL_2,Relay.Direction.kForward);
        gearTrayRelay2.set(Relay.Value.kOff);

		// create motors & servos
		transportMotor = new Spark(HardwareIDs.TRANSPORT_PWM_ID);
		collectorMotor = new CANTalon(HardwareIDs.COLLECTOR_TALON_ID);
		agitatorServo = new Spark(HardwareIDs.AGITATOR_PWM_ID);    // continuous servo control modeled as Spark PWM
		
		feederMotor = new CANTalon(HardwareIDs.FEEDER_TALON_ID);
		shooterMotor = new CANTalon(HardwareIDs.SHOOTER_TALON_ID);
		
		// set up shooter motor sensor
		shooterMotor.reverseSensor(false);
		shooterMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);

		// FOR REFERENCE ONLY:
		//shooterMotor.configEncoderCodesPerRev(12);   // use this ONLY if you are NOT reading Native units
		
		// USE FOR DEBUG ONLY:  configure shooter motor for open loop speed control
		//shooterMotor.changeControlMode(TalonControlMode.PercentVbus);
		
		// configure shooter motor for closed loop speed control
		shooterMotor.changeControlMode(TalonControlMode.Speed);
		shooterMotor.configNominalOutputVoltage(+0.0f, -0.0f);
		shooterMotor.configPeakOutputVoltage(+12.0f, -12.0f);
		
		// set PID(F) for shooter motor (one profile only)
		shooterMotor.setProfile(0);
		
		shooterMotor.setP(P_COEFF);
		shooterMotor.setI(I_COEFF);
		shooterMotor.setD(D_COEFF);
		shooterMotor.setF(F_COEFF);
		
		// make sure all motors are off
		resetMotors();
		
		gamepad = new Joystick(HardwareIDs.GAMEPAD_ID);
		
		initialized = true;
	}
	
	public static void resetMotors()
	{		
		shooterMotor.set(0);
		feederMotor.set(0);	
		transportMotor.set(0);
		collectorMotor.set(0);
		agitatorServo.set(AGITATOR_OFF);
		
		feeding = false;
	}
	
	public static void setShooterStrength(int newIndex) {
		
		if (!initialized)
			initialize();
		
		// if out of range, just return...	
		if ((newIndex > MOTOR_HIGH) || (newIndex < MOTOR_OFF))
			return;
		
		// report on what strength we're setting
		//System.out.println("Motor Strength = " + motorSettings[newIndex]);
		double shooter_rpm = motorSettings[newIndex] * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Target", shooter_rpm);		

		// set motor to the specified value
		shooterMotor.set(motorSettings[newIndex]);	

		// if turning off motors...
		if (newIndex == MOTOR_OFF) {
			stopFeeding();  // turn off feeder & agitator motors			
		}
		// if turning on motors...
		else  {	
			// if shooter motor is on and we're not yet feeding (i.e. motor is spinning up from being off)
			if (!feeding)
			{
				// spawn a wait thread to ensure agitator and feeder are turned on only AFTER a certain period
				new Thread() {
					public void run() {
						try {
							Thread.sleep(1000);  // wait one second before starting to feed
							startFeeding();		 // start feeder motors
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		}
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();		
	}
	
	public static void startFeeding() {
		//System.out.println("starting feeder & agitator...");
				
        double feederLevel = FEEDER_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);
        
        
        double agitatorLevel = AGITATOR_ON;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorServo.set(agitatorLevel);
                
        feeding = true;
	}
	
	public static void stopFeeding() {
		//System.out.println("stopping feeder & agitator...");
		
        double feederLevel = 0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);	
        
        double agitatorLevel = AGITATOR_OFF;  // setting half will turn off continuous servo
        //double agitatorLevel = 0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorServo.set(agitatorLevel);
        
        feeding = false;
	}		
	
	public static void gearTrayOn() {
		// release collector and gear tray
    	gearTrayRelay.set(Relay.Value.kOn);		
    	gearTrayRelay2.set(Relay.Value.kOn);		
	}
	
	public static void gearTrayOff() {
		// release collector and gear tray
    	gearTrayRelay.set(Relay.Value.kOff);		
    	gearTrayRelay2.set(Relay.Value.kOff);		
	}
	
	// no longer used
	/*
	public static void collectorOn() {
		collectorSolenoid.set(1.0);
    	collectorEnabled = true;
	}
	
	public static void collectorOff() {
		collectorSolenoid.set(0.0);
    	collectorEnabled = false;
	}
	*/
	
	private static void checkCollectorControls() {
		
		//if (gamepad.getRawButton(HardwareIDs.COLLECTOR_CONTROL_BUTTON))  {
		//	if (!collectorEnabled)
		//		collectorOn();
		//}
		
		//if (!collectorEnabled)
		//	return;
		
		// transport control
		double transportLevel = gamepad.getRawAxis(HardwareIDs.TRANSPORT_IN_AXIS);
		if (Math.abs(transportLevel) > DEAD_ZONE_THRESHOLD)
			transportLevel = TRANSPORT_IN_LEVEL;
		else if (gamepad.getRawButton(HardwareIDs.TRANSPORT_OUT_BUTTON))
			transportLevel = TRANSPORT_OUT_LEVEL;
		else
			transportLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/TransportLevel", transportLevel);		
		transportMotor.set(transportLevel);
		
		// collector control - no longer used
		/*
		double collectorLevel = gamepad.getRawAxis(HardwareIDs.COLLECTOR_IN_AXIS);
		if (Math.abs(collectorLevel) > DEAD_ZONE_THRESHOLD)
			collectorLevel = COLLECTOR_IN_LEVEL;
		else if (gamepad.getRawButton(HardwareIDs.COLLECTOR_OUT_BUTTON))
			collectorLevel = COLLECTOR_OUT_LEVEL;
		else
			collectorLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/CollectorLevel", collectorLevel);
		collectorMotor.set(collectorLevel);
		*/
		
	}
	
	private static void checkShooterControls() {
		// fire controls - using a timer to debounce
		double currentTime = Utility.getFPGATime();

		// if not enough time has passed, no polling allowed!
		if ((currentTime - initTriggerTime) < TRIGGER_CYCLE_WAIT_US)
			return;

		// shooter commands
		if (gamepad.getRawButton(HardwareIDs.FIRE_HIGH_BUTTON))
			setShooterStrength(MOTOR_HIGH);			
		
		if (gamepad.getRawButton(HardwareIDs.FIRE_MEDIUM_BUTTON))
			setShooterStrength(MOTOR_MEDIUM);			

		if (gamepad.getRawButton(HardwareIDs.FIRE_LOW_BUTTON))
			setShooterStrength(MOTOR_LOW);			
		
		if (gamepad.getRawButton(HardwareIDs.HOLD_BUTTON))
			setShooterStrength(MOTOR_OFF);
		
	}

	public static void autoInit() {
				
		gearTrayOff();
		//collectorOff();
		resetMotors();
		
        initTriggerTime = Utility.getFPGATime();
	}

	public static void teleopInit() {
				
		gearTrayOn();
		//collectorOff();    // keep collector off until gamepad control pressed
		resetMotors();
		
		// spawn a wait thread to turn relays back off after a number of seconds
		/*
		new Thread() {
			public void run() {
				try {
					Thread.sleep(3000);  // wait a number of sec before starting to feed
					gearTrayOff();	 	 // turn relays off
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		*/
		
        initTriggerTime = Utility.getFPGATime();
        
	}
	
	public static void teleopPeriodic() {
		
		checkCollectorControls();
		checkShooterControls();
		
		// DEBUG - report on shooter motor native values		
		double speed_rpm = shooterMotor.getSpeed() * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Actual", speed_rpm);
						
		double motorOutput = shooterMotor.getOutputVoltage()/shooterMotor.getBusVoltage();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/motorOutput", motorOutput);

		double closedLoopError = shooterMotor.getClosedLoopError();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/closedLoopError", closedLoopError);
		
		double agitatorFb = agitatorServo.getPosition();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorPos", agitatorFb);		

	}
	
}
