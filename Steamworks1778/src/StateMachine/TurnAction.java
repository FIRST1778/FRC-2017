package StateMachine;

import java.util.prefs.Preferences;

import Systems.AutoDriveAssembly;
import Systems.NavXSensor;

public class TurnAction extends Action {
	
	private double angleToTurn = 0.0;
	private double speedToTurn = 0.3;
	private boolean resetGyro = true;
	
	private double initialAngle = 0.0;
		
	public TurnAction(double angleToTurn, boolean resetGyro, double speed)
	{
		this.name = "<Turn Action>";
		this.angleToTurn = angleToTurn;
		this.resetGyro = resetGyro;
		this.speedToTurn = speed;
				
		AutoDriveAssembly.initialize();
	}
	
	public TurnAction(String name, double angleToTurn, boolean resetGyro, double speed)
	{
		this.name =  name;
		this.angleToTurn = angleToTurn;
		this.resetGyro = resetGyro;
		this.speedToTurn = speed;
		
		AutoDriveAssembly.initialize();
	}
	
	// action entry
	public void initialize() {
		
		// if we're not resetting the gyro, we'll want to see what angle it is to start
		if (!resetGyro)
			initialAngle = NavXSensor.getAngle();
		else
			initialAngle = 0.0;
		
		// initialize motor assembly for auto
		AutoDriveAssembly.autoInit(resetGyro);
		
		super.initialize();
	}
	
	// called periodically
	public void process()  {
		
		// check the difference from our initial angle
		double angleDiff = angleToTurn - initialAngle;
			
		// rotate to close the gap
		if (angleDiff > 0.0)
			AutoDriveAssembly.rotateRight(speedToTurn);
		else
			AutoDriveAssembly.rotateLeft(speedToTurn);
		
		super.process();
	}
	
	// action cleanup and exit
	public void cleanup() {
		// do some drivey cleanup
			
		// PWMDriveAssembly not supported
		
		AutoDriveAssembly.autoStop();
		
		// cleanup base class
		super.cleanup();
	}
	
	public void persistWrite(int counter, Preferences prefs) {

		// create node for action
		Preferences actionPrefs = prefs.node(counter + "_" + this.name);
	
		// store action details
		actionPrefs.put("class",this.getClass().toString());
		actionPrefs.putDouble("angleToTurn",this.angleToTurn);
		actionPrefs.putDouble("speedToTurn",this.speedToTurn);
	}

}
