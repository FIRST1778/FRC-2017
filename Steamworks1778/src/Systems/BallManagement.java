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
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Utility;

public class BallManagement {
	
	private static boolean initialized = false;
	private static boolean feeding = false;
	
	private static final double TRANSPORT_IN_LEVEL = 0.5;
	private static final double TRANSPORT_OUT_LEVEL = -0.5;
	
	private static final double COLLECTOR_IN_LEVEL = 0.5;
	private static final double COLLECTOR_OUT_LEVEL = -0.5;
	
	private static final double AGITATOR_LEVEL = 0.8;
	private static final double FEEDER_LEVEL = 0.3;
	
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
	private static final double motorSettings[] = { 0, 0, 200, 230, 260, 300, 300 };		    // Speed (Native) control settings
	
	
	//private static final double motorSettings[] = { 0.0, 0.1, 0.375, 0.43, 0.5, 1.0, 1.0 };   // Vbus (%) control settings
	private static final int NUM_MOTOR_SETTINGS = 7;
	
	// relays to release collector and gear tray
	private static Relay collectorRelay;
	private static Relay gearTrayRelay;
	
	// shooter and support motors
	private static CANTalon shooterMotor, feederMotor;
	private static Spark agitatorMotor;
	
	// collector & transport motors
	private static CANTalon transportMotor;
	private static Spark collectorMotor;
	
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
                
        // create and reset gear tray relay
        gearTrayRelay = new Relay(HardwareIDs.GEAR_TRAY_RELAY_CHANNEL,Relay.Direction.kForward);
        gearTrayRelay.set(Relay.Value.kOff);

		// create motors
		transportMotor = new CANTalon(HardwareIDs.TRANSPORT_TALON_ID);
		collectorMotor = new Spark(HardwareIDs.COLLECTOR_PWM_ID);
		
		feederMotor = new CANTalon(HardwareIDs.FEEDER_TALON_ID);
		agitatorMotor = new Spark(HardwareIDs.AGITATOR_PWM_ID);
		shooterMotor = new CANTalon(HardwareIDs.SHOOTER_TALON_ID);
		
		// set up shooter motor sensor
		shooterMotor.reverseSensor(false);
		shooterMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		//shooterMotor.configEncoderCodesPerRev(12);   // use this ONLY if you are NOT reading Native units
		
		// USE FOR DEBUG ONLY:  configure shooter motor for open loop speed control
		//shooterMotor.changeControlMode(TalonControlMode.PercentVbus);
		
		// configure shooter motor for closed loop speed control
		shooterMotor.changeControlMode(TalonControlMode.Speed);
		shooterMotor.configNominalOutputVoltage(+0.0f, -0.0f);
		shooterMotor.configPeakOutputVoltage(+12.0f, -12.0f);
		shooterMotor.setProfile(0);
		shooterMotor.setP(3);
		shooterMotor.setI(0);
		shooterMotor.setD(0);
		shooterMotor.setF(2.91);

		// make sure all motors are off
		resetMotors();
		
		gamepad = new Joystick(HardwareIDs.GAMEPAD_ID);
		
		initialized = true;
	}
	
	public static void resetMotors()
	{		
		shooterMotor.set(0);
		feederMotor.set(0);
		agitatorMotor.set(0);
		
		transportMotor.set(0);
		collectorMotor.set(0);
		
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
						Thread.sleep(1000);  // wait one sec before starting to feed
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
		//System.out.println("starting feeders...");
		
        double agitatorLevel = AGITATOR_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorMotor.set(agitatorLevel);
		
        double feederLevel = FEEDER_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);	
                
        feeding = true;
	}
	
	public static void stopFeeding() {
        double agitatorLevel = 0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorMotor.set(agitatorLevel);		
		
        double feederLevel = 0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);	
        
        feeding = false;
	}		
	
	private static void checkCollectorControls() {
		
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
			
	public static void teleopInit() {
		// release collector and gear tray
    	collectorRelay.set(Relay.Value.kOn);
    	gearTrayRelay.set(Relay.Value.kOn);
				
		resetMotors();
		
        initTriggerTime = Utility.getFPGATime();
        
	}
	
	public static void teleopPeriodic() {
		
		checkCollectorControls();
		checkShooterControls();
		
		// DEBUG - report on shooter motor values		
		double speed_rpm = shooterMotor.getSpeed() * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Actual", speed_rpm);
		
		//double encVelocity = shooterMotor.getEncVelocity();
		//InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/encVelocity", encVelocity);
				
		double motorOutput = shooterMotor.getOutputVoltage()/shooterMotor.getBusVoltage();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/motorOutput", motorOutput);

		double closedLoopError = shooterMotor.getClosedLoopError();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/closedLoopError", closedLoopError);
		
	}
	
}
