package StateMachine;

import java.util.prefs.Preferences;
import Systems.AutoDriveAssembly;

import edu.wpi.first.wpilibj.Utility;

// event triggered when closed loop position control gets to within an error range of target for a time period
public class ClosedLoopPositionEvent extends Event {
	
	private String name;
	private double targetPosInches;
	private double errorThresholdInches;
	private double durationSec;
	
	private long startTimeUs;
	
	public ClosedLoopPositionEvent()
	{	
		this.name = "<Closed-Loop Position Event>";
		this.durationSec = 0.0;
		this.errorThresholdInches = 0.0;
		this.targetPosInches = 0.0;
	}
	
	public ClosedLoopPositionEvent(double targetPosInches, double errorThreshInches, double durationSec)
	{
		this.name = "<Closed-Loop Position Event>";
		this.durationSec = durationSec;
		this.errorThresholdInches = errorThreshInches;
		this.targetPosInches = targetPosInches;
	}
	
	// overloaded initialize method
	public void initialize()
	{
		//System.out.println("ClosedLoopPositionEvent initialized!");
		startTimeUs = Utility.getFPGATime();
		
		super.initialize();
	}
	
	// overloaded trigger method
	public boolean isTriggered()
	{		
		// measure current position error
		double actualPosInches = AutoDriveAssembly.getDistanceInches();
		double errorPosInches = Math.abs(targetPosInches - actualPosInches);
		if (errorPosInches > errorThresholdInches)
		{
			// outside error range...
			// reset timer
			startTimeUs = Utility.getFPGATime();
			return false;
		}
	
		long currentTimeUs = Utility.getFPGATime();
		double delta = (currentTimeUs - startTimeUs)/1e6;
		//System.out.println("delta = " + delta + " duration = " + durationSec);
		
		if (delta < durationSec)
		{
			// within error range, but not for enough time
			return false;
		}
		
		// within error range for enough time
		System.out.println("ClosedLoopPositionEvent triggered!");
		return true;
	}
	
	public void persistWrite(int counter, Preferences prefs) {

		// create node for event
		Preferences eventPrefs = prefs.node(counter + "_" + this.name);
	
		// store event details
		eventPrefs.put("class",this.getClass().toString());
		eventPrefs.putDouble("durationSec",this.durationSec);		
	}
}
