package FreezyDrive;

/** 
    Use this class to map the buttons and tumbsticks of the Logitech controllers to named uses.
	For reference, all the button IDs are listed in the LogitechF310.java file.
*/
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import Utility.LogitechF310;
import Utility.LogitechDualAction;
import Utility.InterLink;
import Utility.InterLinkElite;


public class Controller {

	// Types: 0 = Logitech F310, 1 = InterLink Flight Controller, 2 = Logitech Dual Action
	public static final int DRIVER_CONTROLLER_TYPE = 1;
    //public static final int COPILOT_CONTROLLER_TYPE = 1;
    public static final int PORT_DRIVER_CONTROLLER = 0;
    //public static final int PORT_COPILOT_CONTROLLER = 1;
	public static Joystick Driver;
	public static Joystick CoPilot;
	
	public static boolean[] controllerType = new boolean[3];
	
	public static boolean initialized = false;
	    
    public static void initialize() {
    	
    	if (initialized)
    		return;
    	
    	Driver = new Joystick(PORT_DRIVER_CONTROLLER);
    	//CoPilot = new Joystick(PORT_COPILOT_CONTROLLER);
   	
    	initialized = true;
    }
    
    public static double Driver_Throttle (){
    	
		return Driver.getRawAxis(InterLinkElite.Axis.LEFT_Y);
    	//return Driver.getRawAxis(1);
    	// swtich statements for all controller types
    	/*
    	switch(DRIVER_CONTROLLER_TYPE){
    	case 0:
    		return Driver.getRawAxis(LogitechF310.Axis.LEFT_Y);
    	case 1:
    		return Driver.getRawAxis(InterLink.Axis.LEFT_Y);
    	case 2:
    		return Driver.getRawAxis(LogitechDualAction.Axis.LEFT_Y);
    	}
    	return 0;
    	*/
    }

    public static double Driver_Steering (){
    	
    	return -Driver.getRawAxis(InterLinkElite.Axis.RIGHT_X);
    //	return -Driver.getRawAxis(0);
    	/*
    	switch(DRIVER_CONTROLLER_TYPE){
    	case 0:
    		return -Driver.getRawAxis(LogitechF310.Axis.RIGHT_X);
    	case 1:
    		return -Driver.getRawAxis(InterLink.Axis.RIGHT_X);
    	case 2:
    		return -Driver.getRawAxis(LogitechDualAction.Axis.RIGHT_X);
    	}
    	return 0;
    	*/
    }
    
    public static boolean Driver_isQuickTurn(){
    	
    	return !Driver.getRawButton(InterLinkElite.RIGHT_SWITCH);
    	//return false;
    	/*
    	switch(DRIVER_CONTROLLER_TYPE){
    	case 0:
    		return Driver.getRawButton(LogitechF310.LB);
    	case 1: 
    		return Driver.getRawButton(InterLink.RIGHT_SWITCH);
    	case 2: 
    		return Driver.getRawButton(LogitechDualAction.RIGHT_BUMPER);
    	}	
    	return false;
    	*/
    }
    
}