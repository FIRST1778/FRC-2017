package StateMachine;

import java.util.prefs.Preferences;

import Systems.AutoDriveAssembly;
import NetworkComm.RPIComm;

public class CalibrateTargetAction extends Action {
	
	private double desiredX, desiredY;
	private double threshX, threshY;
	private double speedX, speedY;
	
	public CalibrateTargetAction() {
		this.name = "<Calibrate Target Action>";	
		this.desiredX = 0.0;
		this.desiredY = 0.0;
		this.threshX = 0.0;
		this.threshY = 0.0;
		this.speedX = 0.0;
		this.speedY = 0.0;
		
		// do some calibrate initialization
		AutoDriveAssembly.initialize();
		RPIComm.initialize();
	}
	
	public CalibrateTargetAction(String name, double desiredX, double desiredY, double threshX, double threshY, double speedX, double speedY)
	{
		this.name = name;
		this.desiredX = desiredX;
		this.desiredY = desiredY;
		this.threshX = threshX;
		this.threshY = threshY;
		this.speedX = speedX;
		this.speedY = speedY;
		
		// do some calibrate initialization
		AutoDriveAssembly.initialize();
		RPIComm.initialize();
	}
	
	// action entry
	public void initialize() {
		
		// reset the RPi Vision Table
		RPIComm.autoInit();
						
		// set the desired target X and Y
		RPIComm.setDesired(desiredX, desiredY, threshX, threshY, speedX, speedY);
		
		RPIComm.setMovementModes(true, true);  // forward and lateral movement
				
		super.initialize();
	}
	
	// called periodically
	public void process()  {
		
		RPIComm.updateValues();
		
		if (RPIComm.hasTarget()) {
			
			RPIComm.targetProcessing();
			
			double leftVal = RPIComm.getLeftDriveValue();
			double rightVal = RPIComm.getRightDriveValue();
			
			AutoDriveAssembly.drive(leftVal, rightVal, 0);
		}
		else {
			// no target - stop motors
			AutoDriveAssembly.drive(0, 0, 0);
		}
		
		super.process();
	}
	
	// state cleanup and exit
	public void cleanup() {
		// do some calibrate cleanup
		AutoDriveAssembly.drive(0, 0, 0);
					
		// cleanup base class
		super.cleanup();
	}
	
	public void persistWrite(int counter, Preferences prefs) {

		// create node for action
		Preferences actionPrefs = prefs.node(counter + "_" + this.name);
	
		// store action name
		actionPrefs.put("class",this.getClass().toString());
	}

}
