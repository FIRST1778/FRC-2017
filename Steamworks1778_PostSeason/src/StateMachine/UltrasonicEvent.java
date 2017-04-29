package StateMachine;

import NetworkComm.InputOutputComm;
import Systems.UltrasonicSensor;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Utility;

public class UltrasonicEvent extends Event {
		
	private String name;
	private double desiredRangeInches;
	
	
	public UltrasonicEvent()
	{	
		this.name = "<Ultrasonic Event>";
		this.desiredRangeInches = 0.0;
	}
	
	public UltrasonicEvent(double rangeInches)
	{
		this.name = "<Ultrasonic Event>";
		this.desiredRangeInches = rangeInches;
	}
	
	// overloaded initialize method
	public void initialize()
	{
		//System.out.println("UltrasonicEvent initialized!");
		UltrasonicSensor.initialize();
		
		super.initialize();
	}
	
	public void setRange(double rangeInches) 
	{
		this.desiredRangeInches = rangeInches;		
	}
	
	public double getRange() {
		
		double currentRangeInches = UltrasonicSensor.getRange();
		
		String rangeStr = String.format("%.2f", currentRangeInches);
	    String myString = new String("currentRangeInches = " + rangeStr);
		//System.out.println(myString);
		InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"Auto/CurrentRange", myString);		
		
		return currentRangeInches;
	}
	
	// overloaded trigger method
	public boolean isTriggered()
	{	
		//System.out.println("currentRangeInInches = " + currentRangeInches);
		
		if (getRange() <= desiredRangeInches)
		{
			System.out.println("UltrasonicEvent triggered!");
			return true;
		}
		
		return false;
	}
}
