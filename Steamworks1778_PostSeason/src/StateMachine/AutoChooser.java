package StateMachine;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutoChooser {
	
	// drive networks
	public static final int DO_NOTHING = 0;
	public static final int DRIVE_FORWARD = 1;
	
	// gear networks
	public static final int DEPOSIT_GEAR_LEFT = 2;
	public static final int DEPOSIT_GEAR_CENTER = 3;
	public static final int DEPOSIT_GEAR_RIGHT = 4;
	
	// debug networks
	public static final int TURNING_FOREVER = 5;
	public static final int PACING_FOREVER = 6;

	// legacy networks
	public static final int DRIVE_AND_SHOOT_BLUE_LEFT = 7;
	public static final int DRIVE_AND_SHOOT_RED_RIGHT = 8;
	public static final int DEPOSIT_GEAR_AND_SHOOT_RED_CENTER = 9;
	public static final int DEPOSIT_GEAR_AND_SHOOT_BLUE_CENTER = 10;
	public static final int SHOOT_AND_DRIVE_BLUE_LEFT = 11;
	public static final int SHOOT_AND_DRIVE_RED_RIGHT = 12;
	public static final int DRIVE_AND_SHOOT_NEAR = 13;
	public static final int DRIVE_AND_SHOOT_MEDIUM = 14;
	
	// internal selection class used for SendableChooser only
	public class ModeSelection {
		public int mode = DO_NOTHING;
		ModeSelection(int mode) {
			this.mode = mode;
		}
	}
	
	int mode;
	private SendableChooser<ModeSelection> chooser_basic;
	private SendableChooser<ModeSelection> chooser_gears;
	private SendableChooser<ModeSelection> chooser_debug;
	//private SendableChooser<ModeSelection> chooser_shoot;

	public AutoChooser() {

		chooser_basic = new SendableChooser<ModeSelection>();
		chooser_gears = new SendableChooser<ModeSelection>();
		chooser_debug = new SendableChooser<ModeSelection>();
		//chooser_shoot = new SendableChooser<ModeSelection>();
		
		chooser_basic.addDefault("DO_NOTHING", new ModeSelection(DO_NOTHING));
		chooser_basic.addObject("DRIVE_FORWARD", new ModeSelection(DRIVE_FORWARD));
		
		chooser_gears.addDefault("DO_NOTHING", new ModeSelection(DO_NOTHING));
		chooser_gears.addObject("DEPOSIT_GEAR_LEFT", new ModeSelection(DEPOSIT_GEAR_LEFT));
		chooser_gears.addObject("DEPOSIT_GEAR_CENTER", new ModeSelection(DEPOSIT_GEAR_CENTER));
		chooser_gears.addObject("DEPOSIT_GEAR_RIGHT", new ModeSelection(DEPOSIT_GEAR_RIGHT));
		
		chooser_debug.addDefault("DO_NOTHING", new ModeSelection(DO_NOTHING));
		chooser_debug.addObject("TURNING_FOREVER", new ModeSelection(TURNING_FOREVER));
		chooser_debug.addObject("PACING_FOREVER", new ModeSelection(PACING_FOREVER));

		//chooser_shoot.addDefault("DO_NOTHING", new ModeSelection(DO_NOTHING));
		//chooser_shoot.addObject("DRIVE_AND_SHOOT_BLUE_LEFT", new ModeSelection(DRIVE_AND_SHOOT_BLUE_LEFT));
		//chooser_shoot.addObject("DRIVE_AND_SHOOT_RED_RIGHT", new ModeSelection(DRIVE_AND_SHOOT_RED_RIGHT));
		//chooser_shoot.addObject("DEPOSIT_GEAR_AND_SHOOT_RED_CENTER", new ModeSelection(DEPOSIT_GEAR_AND_SHOOT_RED_CENTER));
		//chooser_shoot.addObject("DEPOSIT_GEAR_AND_SHOOT_BLUE_CENTER", new ModeSelection(DEPOSIT_GEAR_AND_SHOOT_BLUE_CENTER));

		// debug networks
		//chooser_debug.addDefault("DO_NOTHING", new ModeSelection(DO_NOTHING));
		//chooser_debug.addObject("PACING_FOREVER", new ModeSelection(PACING_FOREVER));

		//chooser_shoot.addObject("SHOOT_AND_DRIVE_BLUE_LEFT", new ModeSelection(SHOOT_AND_DRIVE_BLUE_LEFT));
		//chooser_shoot.addObject("SHOOT_AND_DRIVE_RED_RIGHT", new ModeSelection(SHOOT_AND_DRIVE_RED_RIGHT));

		//chooser_debug.addObject("DRIVE_AND_SHOOT_NEAR", new ModeSelection(DRIVE_AND_SHOOT_NEAR));
		//chooser_debug.addObject("DRIVE_AND_SHOOT_MEDIUM", new ModeSelection(DRIVE_AND_SHOOT_MEDIUM));
		
		SmartDashboard.putData("AutoChooser_Basic", chooser_basic);
		SmartDashboard.putData("AutoChooser_Gears", chooser_gears);
		SmartDashboard.putData("AutoChooser_Debug", chooser_debug);
		//SmartDashboard.putData("AutoChooser_Shoot", chooser_shoot);
	}
	
	public int getAutoChoice() {
		
		// scans choosers in order, returns the first to have action
		
		// check basic
		ModeSelection selection = chooser_basic.getSelected();
		if (selection.mode != DO_NOTHING)
			return selection.mode;	

		// check gears
		selection = chooser_gears.getSelected();
		if (selection.mode != DO_NOTHING)
			return selection.mode;	
		
		// check shoot
		//selection = chooser_shoot.getSelected();
		//if (selection.mode != DO_NOTHING)
		//	return selection.mode;	

		// check debug
		selection = chooser_debug.getSelected();
		if (selection.mode != DO_NOTHING)
			return selection.mode;	

		// default - do nothing
		return DO_NOTHING;
	}

}
