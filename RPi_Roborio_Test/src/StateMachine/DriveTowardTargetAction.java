package StateMachine;

import java.util.prefs.Preferences;

import Systems.CANDriveAssembly;

public class DriveTowardTargetAction extends Action {
	private String name;
	private double speed = 0.0;
	
	public DriveTowardTargetAction(double speed)
	{
		this.name = "<Drive Toward Target Action>";		
		this.speed = speed;

		CANDriveAssembly.initialize();
	}
	
	public DriveTowardTargetAction(String name, double speed)
	{
		this.name =  name;
		this.speed = speed;
				
		CANDriveAssembly.initialize();
	}
	
	// action entry
	public void initialize() {
		// do some drivey initialization
		
		CANDriveAssembly.autoInit(false);
		
		super.initialize();
	}
	
	// called periodically
	public void process()  {
		
		// do some drivey stuff
				
		CANDriveAssembly.autoPeriodicTowardTarget(speed);
		
		super.process();
	}
	
	// state cleanup and exit
	public void cleanup() {
		// do some drivey cleanup
					
		CANDriveAssembly.autoStop();
		
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
