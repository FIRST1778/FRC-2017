package StateMachine;

import java.util.prefs.Preferences;

import NetworkComm.InputOutputComm;
import Systems.AutoDriveAssembly;

public class DriveForwardMagicAction extends Action {
	
	private String name;
	private double targetPosRevs = 0.0;
	private double speedRpm = 0.0;
	
	private final double REVS_PER_INCH = 1/(6 * 3.14159);
	
	public DriveForwardMagicAction(double targetPosInches, double speedRpm)
	{
		this.name = "<Drive Forward Magic Action>";		
		this.targetPosRevs = targetPosInches * REVS_PER_INCH;
		this.speedRpm = speedRpm;

		AutoDriveAssembly.initialize();
	}
	
	public DriveForwardMagicAction(String name, double targetPosInches, double speedRpm)
	{
		this.name =  name;
		this.targetPosRevs = targetPosInches * REVS_PER_INCH;
		this.speedRpm = speedRpm;
				
		AutoDriveAssembly.initialize();
	}
		
	// action entry
	public void initialize() {
		// do some drivey initialization
		
		AutoDriveAssembly.autoInit(true, true);
		AutoDriveAssembly.autoMagicStraight(targetPosRevs, speedRpm);
		
		super.initialize();
	}
	
	// called periodically
	public void process()  {
		
		// do some drivey stuff
						
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
		actionPrefs.putDouble("speedRpm",this.speedRpm);
	}

}
