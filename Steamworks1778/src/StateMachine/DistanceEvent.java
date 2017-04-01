package StateMachine;

import NetworkComm.InputOutputComm;
import Systems.AutoDriveAssembly;

public class DistanceEvent extends Event {
	private String name;
	private double desiredDistanceInches;
	
	public DistanceEvent()
	{	
		this.name = "<Distance Event>";
		this.desiredDistanceInches = 0.0;
	}
	
	public DistanceEvent(double distanceInches)
	{
		this.name = "<Distance Event>";
		this.desiredDistanceInches = distanceInches;
	}
	
	// overloaded initialize method
	public void initialize()
	{
		//System.out.println("DistanceEvent initialized!");
		AutoDriveAssembly.initialize();
		
		super.initialize();
	}
	
	public void setDistance(double distanceInches) 
	{
		this.desiredDistanceInches = distanceInches;		
	}
	
	public double getDistance() {
		
		double currentDistanceInches = AutoDriveAssembly.getDistanceInches();
		
		String distStr = String.format("%.2f", currentDistanceInches);
	    String myString = new String("currentDistanceInches = " + distStr);
		//System.out.println(myString);
		InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"Auto/CurrentDistance", myString);		
		
		return currentDistanceInches;
	}
	
	// overloaded trigger method
	public boolean isTriggered()
	{	
		//System.out.println("currentRangeInInches = " + currentRangeInches);
		
		if (getDistance() >= desiredDistanceInches)
		{
			System.out.println("DistanceEvent triggered!");
			return true;
		}
		
		return false;
	}

}
