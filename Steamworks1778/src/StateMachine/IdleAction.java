package StateMachine;

import java.util.prefs.Preferences;

import NetworkComm.InputOutputComm;
import Systems.NavXSensor;
import Systems.UltrasonicSensor;


public class IdleAction extends Action {
	
	public IdleAction() {
		this.name = "<Idle Action>";		
	}
	
	private double getGyroAngle() {
		//double gyroAngle = 0.0;
		//double gyroAngle = NavXSensor.getYaw();  // -180 deg to +180 deg
		double gyroAngle = NavXSensor.getAngle();  // continuous angle (can be larger than 360 deg)
		
		//System.out.println("autoPeriodicStraight:  Gyro angle = " + gyroAngle);
			
		// send output data for test & debug
	    InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/IMU_Connected",NavXSensor.isConnected());
	    InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/IMU_Calibrating",NavXSensor.isCalibrating());

		//System.out.println("gyroAngle = " + gyroAngle);
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Auto/GyroAngle", gyroAngle);		

		return gyroAngle;
	}
	public void initialize() {
		super.initialize();
	}
	
	public void process() {
		getGyroAngle();

		super.process();
	}
	
	public void cleanup() {
		super.cleanup();
	}
	
	public IdleAction(String name)
	{
		
		this.name = name;
	}
		
	// no need for enter, process, exit overloaded methods
	// WE DON'T DO ANYTHING IN IDLE!
	// used for persisting the network in a Java Preferences class object
	
	public void persistWrite(int counter, Preferences prefs) {

		// create node for action
		Preferences actionPrefs = prefs.node(counter + "_" + this.name);
	
		// store action class
		actionPrefs.put("class",this.getClass().toString());
	}
	
}
