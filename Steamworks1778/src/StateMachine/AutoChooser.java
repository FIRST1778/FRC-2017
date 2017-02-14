package StateMachine;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutoChooser {
	public static final int DO_NOTHING = 0;
	public static final int DRIVE_FORWARD = 1;
	
	public static final int DEPOSIT_GEAR_LEFT = 2;
	public static final int DEPOSIT_GEAR_CENTER = 3;
	public static final int DEPOSIT_GEAR_RIGHT = 4;
	
	public static final int DEPOSIT_GEAR_AND_SHOOT_RED_CENTER = 5;
	public static final int DEPOSIT_GEAR_AND_SHOOT_RED_RIGHT = 6;
	public static final int DEPOSIT_GEAR_AND_SHOOT_BLUE_LEFT = 7;
	public static final int DEPOSIT_GEAR_AND_SHOOT_BLUE_CENTER = 8;

	public static final int DRIVE_AND_SHOOT_BLUE_LEFT = 9;
	public static final int DRIVE_AND_SHOOT_BLUE_CENTER = 10;
	public static final int DRIVE_AND_SHOOT_BLUE_RIGHT = 11;
	public static final int DRIVE_AND_SHOOT_RED_LEFT = 12;
	public static final int DRIVE_AND_SHOOT_RED_CENTER = 13;
	public static final int DRIVE_AND_SHOOT_RED_RIGHT = 14;

	// debug networks
	public static final int DRIVE_AND_SHOOT_NEAR = 15;
	public static final int DRIVE_AND_SHOOT_MEDIUM = 16;
	public static final int DRIVE_AND_SHOOT_FAR = 17;
	
	int mode;
	private SendableChooser chooser;
	
	public AutoChooser() {
		chooser = new SendableChooser();
		
		chooser.addDefault("DO_NOTHING", DO_NOTHING);
		chooser.addObject("DRIVE_FORWARD", DRIVE_FORWARD);
		
		chooser.addObject("DEPOSIT_GEAR_LEFT", DEPOSIT_GEAR_LEFT);
		chooser.addObject("DEPOSIT_GEAR_CENTER", DEPOSIT_GEAR_CENTER);
		chooser.addObject("DEPOSIT_GEAR_RIGHT", DEPOSIT_GEAR_RIGHT);

		chooser.addObject("DEPOSIT_GEAR_AND_SHOOT_RED_CENTER", DEPOSIT_GEAR_AND_SHOOT_RED_CENTER);
		chooser.addObject("DEPOSIT_GEAR_AND_SHOOT_RED_RIGHT", DEPOSIT_GEAR_AND_SHOOT_RED_RIGHT);
		chooser.addObject("DEPOSIT_GEAR_AND_SHOOT_BLUE_LEFT", DEPOSIT_GEAR_AND_SHOOT_BLUE_LEFT);
		chooser.addObject("DEPOSIT_GEAR_AND_SHOOT_BLUE_CENTER", DEPOSIT_GEAR_AND_SHOOT_BLUE_CENTER);
		
		chooser.addObject("DRIVE_AND_SHOOT_BLUE_LEFT", DRIVE_AND_SHOOT_BLUE_LEFT);
		chooser.addObject("DRIVE_AND_SHOOT_BLUE_CENTER", DRIVE_AND_SHOOT_BLUE_CENTER);
		chooser.addObject("DRIVE_AND_SHOOT_BLUE_RIGHT", DRIVE_AND_SHOOT_BLUE_RIGHT);	
		chooser.addObject("DRIVE_AND_SHOOT_RED_LEFT", DRIVE_AND_SHOOT_RED_LEFT);
		chooser.addObject("DRIVE_AND_SHOOT_RED_CENTER", DRIVE_AND_SHOOT_RED_CENTER);
		chooser.addObject("DRIVE_AND_SHOOT_RED_RIGHT", DRIVE_AND_SHOOT_RED_RIGHT);

		// debug networks
		chooser.addObject("DRIVE_AND_SHOOT_NEAR", DRIVE_AND_SHOOT_NEAR);
		chooser.addObject("DRIVE_AND_SHOOT_MEDIUM", DRIVE_AND_SHOOT_MEDIUM);
		chooser.addObject("DRIVE_AND_SHOOT_FAR", DRIVE_AND_SHOOT_FAR);
		
		SmartDashboard.putData("Auto_Mode_Chooser", chooser);
	}
	
	public int getAutoChoice() {
		return (int) chooser.getSelected();
	}

}
