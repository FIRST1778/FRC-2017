package StateMachine;

import java.util.prefs.Preferences;

import NetworkComm.InputOutputComm;
import NetworkComm.RPIComm;
import Systems.AutoDriveAssembly;

public class DriveTowardTargetAction extends Action {
	private final double AUTO_DRIVE_TARGET_CORRECT_COEFF = 0.75;
	
	private String name;
	private double speed = 0.0;
	
	private double desiredX = 0.0;
	private double desiredY = 0.0;
	
	public DriveTowardTargetAction(double speed, double desiredX, double desiredY)
	{
		this.name = "<Drive Toward Target Action>";		
		this.speed = speed;
		this.desiredX = desiredX;
		this.desiredY = desiredY;

		AutoDriveAssembly.initialize();
		RPIComm.initialize();
		
		// set the desired target X and Y
		RPIComm.setDesired(desiredX, desiredY);
	}
	
	public DriveTowardTargetAction(String name, double speed, double desiredX, double desiredY)
	{
		this.name =  name;
		this.speed = speed;
		this.desiredX = desiredX;
		this.desiredY = desiredY;
				
		AutoDriveAssembly.initialize();
		RPIComm.initialize();	
	}
	
	// action entry
	public void initialize() {
						
		// reset the RPI vision object
		RPIComm.autoInit();
		
		// set the desired target X and Y
		RPIComm.setDesired(desiredX, desiredY);
		
		super.initialize();
	}
	
	// called periodically
	public void process()  {
		
		// do some drivey stuff
		RPIComm.updateValues();
		
		if (RPIComm.hasTarget()) {
			
			// target found!  process and retrieve deltaX from desired location		
			double frameWidth = RPIComm.getFrameWidth();
			double deltaX = RPIComm.getDeltaX();
			double driveIncrement = (deltaX/frameWidth) * AUTO_DRIVE_TARGET_CORRECT_COEFF;

			InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/hasTarget", true);		
			InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Auto/desiredX", desiredX);		
			InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Auto/deltaX", deltaX);		
			
			// calculate adjustment for drive toward target
			double leftSpeed = speed+driveIncrement;		
			double rightSpeed = speed-driveIncrement;	

			AutoDriveAssembly.drive(leftSpeed, rightSpeed, 0);
		}
		else {
			InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/hasTarget", false);		

			// no target - drive straight
			AutoDriveAssembly.drive(speed, speed, 0);
		}
		
						
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
