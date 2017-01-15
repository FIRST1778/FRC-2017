package StateMachine;

import java.util.prefs.Preferences;

import Systems.AutoDriveAssembly;
import Systems.NavXSensor;

public class DriveForwardAction extends Action {
	
	private String name;
	private double speed = 0.0;
	private boolean resetGyro = true;
	
	public DriveForwardAction(double speed, boolean resetGyro)
	{
		this.name = "<Drive Forward Action>";		
		this.speed = speed;
		this.resetGyro = resetGyro;

		AutoDriveAssembly.initialize();
	}
	
	public DriveForwardAction(String name, double speed, boolean resetGyro)
	{
		this.name =  name;
		this.speed = speed;
		this.resetGyro = resetGyro;
				
		AutoDriveAssembly.initialize();
	}
	
	// action entry
	public void initialize() {
		// do some drivey initialization
		
		AutoDriveAssembly.autoInit(resetGyro);
		
		super.initialize();
	}
	
	// called periodically
	public void process()  {
		
		// do some drivey stuff
				
		AutoDriveAssembly.autoPeriodicStraight(speed);
		
		super.process();
	}
	
	// state cleanup and exit
	public void cleanup() {
		// do some drivey cleanup
					
		AutoDriveAssembly.autoStop();
		
		// cleanup base class
		super.cleanup();
	}
	
	public void persistWrite(int counter, Preferences prefs) {

		// create node for action
		Preferences actionPrefs = prefs.node(counter + "_" + this.name);
	
		// store action details
		actionPrefs.put("class",this.getClass().toString());
		actionPrefs.putDouble("speed",this.speed);
	}

}
