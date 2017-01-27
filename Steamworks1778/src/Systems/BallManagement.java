package Systems;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.FeedbackDeviceStatus;
import com.ctre.CANTalon.TalonControlMode;

import NetworkComm.InputOutputComm;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Utility;

public class BallManagement {
	
	private static boolean initialized = false;

	private static final int COLLECTOR_RELAY_CHANNEL = 0;
	
	private static final int SHOOTER_TALON_ID = 6;
	private static final int CONVEYER_TALON_ID = 10;
	private static final int COLLECTOR_TALON_ID = 9;
	private static final int AGITATOR_TALON_ID = 5;
	
	private static final double CONVEYER_IN_LEVEL = 0.5;
	private static final double CONVEYER_OUT_LEVEL = -0.5;
	
	private static final double COLLECTOR_IN_LEVEL = 0.5;
	private static final double COLLECTOR_OUT_LEVEL = -0.5;
	
	private static final double AGITATOR_LEVEL = 0.3;
	
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

	private static final double motorSettings[] = { 0, 0, 100, 115, 130, 300, 300 };		    // Speed (Native) control settings
	//private static final double motorSettings[] = { 0.0, 0.1, 0.375, 0.43, 0.5, 1.0, 1.0 };   // Vbus (%) control settings
	private static final int NUM_MOTOR_SETTINGS = 7;
	
	private static Relay collectorRelay;
	
	private static CANTalon shooterMotor, conveyerMotor, collectorMotor, agitatorMotor;
	
	private static final int GAMEPAD_ID = 1;
	private static Joystick gamepad;
		
	// gamepad controls
	private static final int COLLECTOR_IN_AXIS = 2;
	private static final int COLLECTOR_OUT_BUTTON = 5;

	private static final int CONVEYER_IN_AXIS = 3;
	private static final int CONVEYER_OUT_BUTTON = 6;
	
	private static final int FIRE_HIGH_BUTTON = 2;
	private static final int FIRE_MEDIUM_BUTTON = 1;
	private static final int FIRE_LOW_BUTTON = 3;
	private static final int HOLD_BUTTON = 4;
	
	// wait 0.25 s between button pushes on shooter
    private static final int TRIGGER_CYCLE_WAIT_US = 250000;
    private static double initTriggerTime;
    
	public static void initialize() {
		if (initialized)
			return;
		
        // create and reset collector relay
        collectorRelay = new Relay(COLLECTOR_RELAY_CHANNEL,Relay.Direction.kForward);
        collectorRelay.set(Relay.Value.kOff);

		// create motors
		conveyerMotor = new CANTalon(CONVEYER_TALON_ID);
		collectorMotor = new CANTalon(COLLECTOR_TALON_ID);
		agitatorMotor = new CANTalon(AGITATOR_TALON_ID);
		shooterMotor = new CANTalon(SHOOTER_TALON_ID);
		shooterMotor.reverseSensor(true);
		shooterMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		//shooterMotor.configEncoderCodesPerRev(12);   // do not use this unless you want RPM-ish values!
		
		// configure shooter motor for open loop speed control
		//shooterMotor.changeControlMode(TalonControlMode.PercentVbus);
		
		// configure shooter motor for closed loop speed control
		shooterMotor.changeControlMode(TalonControlMode.Speed);
		shooterMotor.configNominalOutputVoltage(+0.0f, -0.0f);
		shooterMotor.configPeakOutputVoltage(+12.0f, -12.0f);
		shooterMotor.setProfile(0);
		shooterMotor.setP(9.0);
		shooterMotor.setI(0.001);
		shooterMotor.setD(0.8);
		shooterMotor.setF(4.35);

		// make sure all motors are off
		resetMotors();
		
		gamepad = new Joystick(GAMEPAD_ID);
		
		initialized = true;
	}
	
	public static void resetMotors()
	{		
		shooterMotor.set(0);
		conveyerMotor.set(0);
		collectorMotor.set(0);
		agitatorMotor.set(0);	
		
	}
	
	public static void setShooterStrength(int newIndex) {
		
		if (!initialized)
			initialize();
		
		// if out of range, just return
		if ((newIndex > MOTOR_MAX) || (newIndex < MOTOR_OFF))
			return;
		
		//System.out.println("Motor Strength = " + motorSettings[newIndex]);
		double shooter_rpm = motorSettings[newIndex] * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Target", shooter_rpm);		
		
		shooterMotor.set(motorSettings[newIndex]);	
		
	}
	
	public static void startAgitator() {
        // initialize the agitator (always on)
        double agitatorLevel = AGITATOR_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorMotor.set(agitatorLevel);		
	}
	
	public static void startConveyer() {
		double conveyerLevel = CONVEYER_IN_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ConveyerLevel", conveyerLevel);		
		conveyerMotor.set(conveyerLevel);
		
	}
	
	public static void fireLevel(int motorLevel) {
				
		setShooterStrength(motorLevel);
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();
	}
		
	
	private static void checkCollectorConveyerControls() {
		
		// conveyer control
		double conveyerLevel = gamepad.getRawAxis(CONVEYER_IN_AXIS);
		if (Math.abs(conveyerLevel) > DEAD_ZONE_THRESHOLD)
			conveyerLevel = CONVEYER_IN_LEVEL;
		else if (gamepad.getRawButton(CONVEYER_OUT_BUTTON))
			conveyerLevel = CONVEYER_OUT_LEVEL;
		else
			conveyerLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ConveyerLevel", conveyerLevel);		
		conveyerMotor.set(conveyerLevel);
		
		// collector control
		double collectorLevel = gamepad.getRawAxis(COLLECTOR_IN_AXIS);
		if (Math.abs(collectorLevel) > DEAD_ZONE_THRESHOLD)
			collectorLevel = COLLECTOR_IN_LEVEL;
		else if (gamepad.getRawButton(COLLECTOR_OUT_BUTTON))
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
		if (gamepad.getRawButton(FIRE_HIGH_BUTTON))
			fireLevel(MOTOR_HIGH);			
		
		if (gamepad.getRawButton(FIRE_MEDIUM_BUTTON))
			fireLevel(MOTOR_MEDIUM);			

		if (gamepad.getRawButton(FIRE_LOW_BUTTON))
			fireLevel(MOTOR_LOW);			
		
		if (gamepad.getRawButton(HOLD_BUTTON))
			fireLevel(MOTOR_OFF);
		
	}
		
	public static void teleopInit() {
		// turn on relay
    	collectorRelay.set(Relay.Value.kOn);
				
		setShooterStrength(MOTOR_OFF);
        initTriggerTime = Utility.getFPGATime();
        startAgitator();
        
	}
	
	public static void teleopPeriodic() {
		
		checkCollectorConveyerControls();
		checkShooterControls();
		
		// DEBUG - report on shooter motor values		
		double speed_rpm = shooterMotor.getSpeed() * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Actual", speed_rpm);
		
		double encVelocity = shooterMotor.getEncVelocity();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/encVelocity", encVelocity);
				
		double motorOutput = shooterMotor.getOutputVoltage()/shooterMotor.getBusVoltage();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/motorOutput", motorOutput);

		double closedLoopError = shooterMotor.getClosedLoopError();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/closedLoopError", closedLoopError);
		
	}
	
}
