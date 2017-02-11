package StateMachine;

import java.util.prefs.Preferences;

import NetworkComm.InputOutputComm;
import NetworkComm.RPIComm;
import Systems.AutoDriveAssembly;

public class DriveTowardTargetAction extends Action {
	private final double AUTO_DRIVE_TARGET_CORRECT_COEFF = 0.75;
	
	private String name;
	private double speedX = 0.0;
	private double speedY = 0.0;
	
	private double desiredX = 0.0;
	private double desiredY = 0.0;
	private double threshX, threshY;
	
	private final double CORRECTION_THRESH_PIX = 5.0;
	private final double TURN_SPEED = 0.2;
	
	public DriveTowardTargetAction(double speed, double desiredX, double desiredY)
	{
		this.name = "<Drive Toward Target Action>";		
		this.desiredX = desiredX;
		this.desiredY = desiredY;
		this.threshX = threshX;
		this.threshY = threshY;
		this.speedX = speedX;
		this.speedY = speedY;

		AutoDriveAssembly.initialize();
		RPIComm.initialize();
		
		// set the desired target X and Y
		RPIComm.setDesired(desiredX, desiredY, threshX, threshY, speedX, speedY);
	}
	
	public DriveTowardTargetAction(String name, double speedX, double speedY, double desiredX, double desiredY)
	{
		this.name =  name;
		this.desiredX = desiredX;
		this.desiredY = desiredY;
		this.threshX = threshX;
		this.threshY = threshY;
		this.speedX = speedX;
		this.speedY = speedY;
				
		AutoDriveAssembly.initialize();
		RPIComm.initialize();	
	}
	
	// action entry
	public void initialize() {
						
		// reset the RPI vision object
		RPIComm.autoInit();
		
		// set the desired target X and Y
		RPIComm.setDesired(desiredX, desiredY, threshX, threshY, speedX, speedY);
		
		super.initialize();
	}
	
	// called periodically
	public void process()  {
		
		// do some drivey stuff
		RPIComm.updateValues();
		
		double leftSpeed = speedY;
		double rightSpeed = speedY;
		
		if (RPIComm.hasTarget()) {
			
			// target found!  process and retrieve deltaX from desired location		
			double deltaX = RPIComm.getDeltaX();
			
			double driveIncrement = 0;
			// if deltaX is more than a threshold, set turn to non-zero
			if (Math.abs(deltaX) > CORRECTION_THRESH_PIX)
				driveIncrement = Math.copySign(TURN_SPEED, deltaX);

			// log data
			InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/hasTarget", true);		
			InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Auto/desiredX", desiredX);		
			InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Auto/deltaX", deltaX);		
			
			// calculate adjustment for drive toward target
			leftSpeed += driveIncrement;		
			rightSpeed -= driveIncrement;	

		}
		else {
			// no target found - log it
			InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/hasTarget", false);		
		}
		
		// send drive speeds to motors
		AutoDriveAssembly.drive(leftSpeed, rightSpeed, 0);
						
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
		actionPrefs.putDouble("speed",this.speedY);
	}

}
