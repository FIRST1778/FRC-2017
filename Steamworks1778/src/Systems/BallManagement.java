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
	private static final double AGITATOR_LEVEL = 0.25;
	
	//  10 100ms/s * (60 s/min) * (1 rev/12 Native Units)
	private static final double NATIVE_TO_RPM_FACTOR = 10 * 60 / 12;
	
	private static final double DEAD_ZONE_THRESHOLD = 0.05;
	
	public static final int MOTOR_OFF = 0;
	public static final int MOTOR_VERY_LOW = 1;
	public static final int MOTOR_LOW = 2;
	public static final int MOTOR_MEDIUM = 3;
	public static final int MOTOR_HIGH = 4;
	public static final int MOTOR_VERY_HIGH = 5;
	public static final int MOTOR_MAX = 6;

	// 1:1 native speed settings
	//private static final double motorSettings[] = { 0, 0, 100, 115, 130, 300, 300 };		    // Speed (Native) control settings

	// 2:1 native speed settings
	//private static final double motorSettings[] = { 0, 0, 200, 230, 260, 300, 300 };		    // Speed (Native) control settings
	
	// (2.5):1 native speed settings
	private static final double motorSettings[] = { 0, 0, 250, 287, 325, 350, 350 };		    // Speed (Native) control settings
	
	// Percent VBus settings
	//private static final double motorSettings[] = { 0.0, 0.0, 0.25, 0.5, 0.75, 1.0, 1.0 };   // Vbus (%) control settings

	private static final int NUM_MOTOR_SETTINGS = 7;
	
	// relays to release collector and gear tray
	private static Relay collectorRelay;
	private static Spark gearTraySpark;
	private static Relay gearTrayRelay2;
	
	// shooter and support motors
	private static CANTalon shooterMotor, feederMotor;
	private static Servo agitatorServo;
	
	// collector & transport motors
	private static Spark transportMotor;
	private static CANTalon collectorMotor;
	private static boolean collectorEnabled = false;
	
	private static Joystick gamepad;
		
	
	// wait 0.25 s between button pushes on shooter
    private static final int TRIGGER_CYCLE_WAIT_US = 250000;
    private static double initTriggerTime;
    
	public static void initialize() {
		if (initialized)
			return;
		
        // create and reset collector relay
        collectorRelay = new Relay(HardwareIDs.COLLECTOR_RELAY_CHANNEL,Relay.Direction.kForward);
        collectorRelay.set(Relay.Value.kOff);
                
        // create and reset gear tray spark & relay
        gearTraySpark = new Spark(HardwareIDs.GEAR_TRAY_PWM_ID);
        gearTrayRelay2 = new Relay(HardwareIDs.GEAR_TRAY_RELAY_CHANNEL_2,Relay.Direction.kForward);
        gearTrayRelay2.set(Relay.Value.kOff);

		// create motors & servos
		transportMotor = new Spark(HardwareIDs.TRANSPORT_PWM_ID);
		collectorMotor = new CANTalon(HardwareIDs.COLLECTOR_TALON_ID);
		
		feederMotor = new CANTalon(HardwareIDs.FEEDER_TALON_ID);
		shooterMotor = new CANTalon(HardwareIDs.SHOOTER_TALON_ID);
		agitatorServo = new Servo(HardwareIDs.AGITATOR_PWM_ID);
		
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
		shooterMotor.setProfile(0);
		shooterMotor.setP(0);
		shooterMotor.setI(0);
		shooterMotor.setD(0);
		shooterMotor.setF(1);

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
		agitatorServo.set(0.5);
		
		feeding = false;
	}
	
	public static void setShooterStrength(int newIndex) {
		
		if (!initialized)
			initialize();
		
		// if out of range, just return
		if ((newIndex > MOTOR_MAX) || (newIndex < MOTOR_OFF))
			return;
		
		if (newIndex == MOTOR_OFF) {
			stopFeeding();  // turn off feeder motors
		}
		
		//System.out.println("Motor Strength = " + motorSettings[newIndex]);
		double shooter_rpm = motorSettings[newIndex] * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Target", shooter_rpm);		
		
		//System.out.println("shooter motor speed..");
		shooterMotor.set(motorSettings[newIndex]);	
		
		// if shooter is not off and we're not feeding (i.e. motor is spinning up from being off)
		if ((newIndex != MOTOR_OFF) && (!feeding))
		{
			// spawn a wait thread to ensure agitator and feeder are turned on only AFTER a certain period
			new Thread() {
				public void run() {
					try {
						Thread.sleep(3000);  // wait a number of sec before starting to feed
						startFeeding();		 // start feeder motors
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();		
	}
	
	public static void startFeeding() {
		//System.out.println("starting feeder & agitator...");
				
        double feederLevel = FEEDER_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);
        
        
        double agitatorLevel = AGITATOR_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorServo.set(agitatorLevel);
                
        feeding = true;
	}
	
	public static void stopFeeding() {
		
        double feederLevel = 0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);	
        
        double agitatorLevel = 0.5;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorServo.set(agitatorLevel);
        
        feeding = false;
	}		
	
	public static void gearTrayOn() {
		// release collector and gear tray
    	gearTraySpark.set(1.0);
    	gearTrayRelay2.set(Relay.Value.kOn);		
	}
	
	public static void gearTrayOff() {
		// release collector and gear tray
    	gearTraySpark.set(0.0);
    	gearTrayRelay2.set(Relay.Value.kOff);		
	}
	
	public static void collectorOn() {
    	collectorRelay.set(Relay.Value.kOn);
    	collectorEnabled = true;
	}
	
	public static void collectorOff() {
    	collectorRelay.set(Relay.Value.kOff);
    	collectorEnabled = false;
	}
	
	private static void checkCollectorControls() {
		
		if (gamepad.getRawButton(HardwareIDs.COLLECTOR_CONTROL_BUTTON))  {
			if (!collectorEnabled)
				collectorOn();
			// just turn on, don't turn off until disabled
			//else
			//	collectorOff();
		}
		
		if (!collectorEnabled)
			return;
		
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
		
		// collector control
		double collectorLevel = gamepad.getRawAxis(HardwareIDs.COLLECTOR_IN_AXIS);
		if (Math.abs(collectorLevel) > DEAD_ZONE_THRESHOLD)
			collectorLevel = COLLECTOR_IN_LEVEL;
		else if (gamepad.getRawButton(HardwareIDs.COLLECTOR_OUT_BUTTON))
			collectorLevel = COLLECTOR_OUT_LEVEL;
		else
			collectorLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/CollectorLevel", collectorLevel);
		collectorMotor.set(collectorLevel);
		
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
		collectorOff();
		resetMotors();
		
        initTriggerTime = Utility.getFPGATime();
	}

	public static void teleopInit() {
				
		gearTrayOn();
		collectorOff();
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
