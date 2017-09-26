package StateMachine;

import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import Systems.BallManagement;
import Systems.CameraControl;

public class AutoNetworkBuilder {
		
	// desired target for near shot (assume 160x120 img)
	private final static double NEAR_TARGET_X = 60.0;
	private final static double NEAR_TARGET_Y = 35.0;
	
	// desired target for medium shot (assume 160x120 img)
	private final static double MEDIUM_TARGET_X = 63.0;
	private final static double MEDIUM_TARGET_Y = 57.0;

	// desired target for far shot (assume 160x120 img)
	//private final static double FAR_TARGET_X = 66.0;
	//private final static double FAR_TARGET_Y = 65.0;
	
	// target calibration speeds
	private final static double CAL_SPEED_COARSE_X = 0.4;
	private final static double CAL_SPEED_COARSE_Y = 0.4;
	private final static double CAL_SPEED_FINE_X = 0.3;
	private final static double CAL_SPEED_FINE_Y = 0.35;
	private final static double CAL_SPEED_EXTRAFINE_X = 0.3;
	private final static double CAL_SPEED_EXTRAFINE_Y = 0.0;
	
	private final static String PREF_ROOT = "ChillOutAutonomousNetworks";
	private static Preferences prefRoot, prefs;
	
	private static ArrayList<AutoNetwork> autoNets;
	
	private static boolean initialized = false;
		
	public static void initialize() throws Exception {
		
		if (!initialized) {
			autoNets = null;
			prefRoot = Preferences.userRoot();
			prefs = prefRoot.node(PREF_ROOT);
			
			initialized = true;
		}
	}
	
	public static ArrayList<AutoNetwork> readInNetworks() {
		
		try {
			if (!initialized)
				initialize();
			}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		autoNets = new ArrayList<AutoNetwork>();
			
		/***** use only when storing the preferences first time *****/
		
		// clear current preferences keys from previous runs
		try {
			prefs.clear();
			Preferences node = prefs.node("<Do Nothing Network>");
			node.removeNode();
		}
		catch (BackingStoreException e) {
			e.printStackTrace();
		}
		
		// create networks
		autoNets.add(AutoChooser.DO_NOTHING, createDoNothingNetwork());	
		autoNets.add(AutoChooser.DRIVE_FORWARD, createDriveForward());	

		autoNets.add(AutoChooser.DEPOSIT_GEAR_LEFT, createDepositGearLeft());	
		autoNets.add(AutoChooser.DEPOSIT_GEAR_CENTER, createDepositGearCenter());	
		autoNets.add(AutoChooser.DEPOSIT_GEAR_RIGHT, createDepositGearRight());	

		autoNets.add(AutoChooser.TURNING_FOREVER, createTurningForeverNetwork());
		autoNets.add(AutoChooser.PACING_FOREVER, createPacingForeverNetwork());

		// debug networks
		//autoNets.add(AutoChooser.DRIVE_AND_SHOOT_NEAR, createDriveAndShootNear());	
		//autoNets.add(AutoChooser.DRIVE_AND_SHOOT_MEDIUM, createDriveAndShootMedium());	

		//autoNets.add(AutoChooser.SHOOT_AND_DRIVE_BLUE_LEFT, createShootAndDriveBlueLeft());	
		//autoNets.add(AutoChooser.SHOOT_AND_DRIVE_RED_RIGHT, createShootAndDriveRedRight());	
		//autoNets.add(AutoChooser.DRIVE_AND_SHOOT_BLUE_LEFT, createDriveAndShootBlueLeft());	
		//autoNets.add(AutoChooser.DRIVE_AND_SHOOT_RED_RIGHT, createDriveAndShootRedRight());	

		//autoNets.add(AutoChooser.DEPOSIT_GEAR_AND_SHOOT_RED_CENTER, createDepositGearAndShootRedCenter());	
		//autoNets.add(AutoChooser.DEPOSIT_GEAR_AND_SHOOT_BLUE_CENTER, createDepositGearAndShootBlueCenter());	
	
		// add the networks to the prefs object
		int counter = 0;
		for (AutoNetwork a: autoNets)
			a.persistWrite(counter++,prefs);		
				
		// store networks to file
	    try {
	        FileOutputStream fos = new FileOutputStream("/home/lvuser/chillOutAutoNets.xml");
	        prefs.exportSubtree(fos);
	        fos.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	   
		
		/**** TODO: normal operation - read in preferences file ***/
		
		return autoNets;
	}
	
	private void parseSingleNetwork() {
		
	}
		
	// **** DO NOTHING Network ***** 
	private static AutoNetwork createDoNothingNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Do Nothing Network>");
		
		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);

		AutoState idleState = new AutoState("<Idle State>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState.addAction(deadEnd);

		// connect each event with a state to move to
		camState.associateNextState(idleState);

		autoNet.addState(camState);	
		autoNet.addState(idleState);	
		
		return autoNet;
	}

	// **** MOVE FORWARD Network ***** 
	// 1) Move camera
	// 2) drive forward for a number of sec
	// 3) go back to idle and stay there 
	private static AutoNetwork createDriveForward() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive Forward Network>");
		
		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);
		
		/*
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.3, true, 0.0);
		TimeEvent timer1a = new TimeEvent(5.0);  // timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer1a);
		*/
		
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardMagicAction driveForwardMagic = new DriveForwardMagicAction("<Drive Forward Magic Action>", 60.0, 300, true, 0.0);
		TimeEvent timer2 = new TimeEvent(20.0);  // drive forward timer event
		driveState.addAction(driveForwardMagic);
		driveState.addEvent(timer2);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(idleState2);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}

	// **** DEPOSIT GEAR LEFT SIDE Network ***** 
	// 1) move camera
	// 2) drive forward for a number of sec
	// 3) Turn RIGHT a number of degrees
	// 4) drive forward
	// 5) go back to idle and stay there 
	private static AutoNetwork createDepositGearLeft() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear (left side) Network>");
		
		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);

		/*
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.3, true, 0.0);
		TimeEvent timer2 = new TimeEvent(1.29);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		*/
		
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardMagicAction driveForwardMagic = new DriveForwardMagicAction("<Drive Forward Magic Action>", 61.5, 300, true, 0.0);
		//TimeEvent timer2 = new TimeEvent(2.5);  // drive forward timer event - allow PID time to settle
		ClosedLoopPositionEvent pos1 = new ClosedLoopPositionEvent(61.5, 0.5, 1.0);
		driveState.addAction(driveForwardMagic);
		//driveState.addEvent(timer2);
		driveState.addEvent(pos1);
		
		/*
		AutoState turnRightState = new AutoState("<Turn Right State>");
		TurnAction turnRightAction = new TurnAction("<Turn right action>", 60.0, 0.3, false);   // don't reset gyro - use absolute heading 60 deg
		GyroAngleEvent gyroRight = new GyroAngleEvent(60.0, false, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);
		*/
		
		AutoState turnRightState = new AutoState("<Turn Right State>");
		TurnPIDAction turnPidAction = new TurnPIDAction("<Turn right PID action>", 50.0, 0.35, true);
		//TimeEvent timer3 = new TimeEvent(2.5);  // timer event - allow PID time to settle
		ClosedLoopAngleEvent angle1 = new ClosedLoopAngleEvent(50.0,2.0,1.0);
		turnRightState.addAction(turnPidAction);
		//turnRightState.addEvent(timer3);
		turnRightState.addEvent(angle1);
		
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.25, true, 0.0);  // don't reset gyro - use absolute heading (60 deg) 
		TimeEvent timer4 = new TimeEvent(3.0);
		//TimeEvent timer4 = new TimeEvent(1.0);   // don't drive all the way to the peg - just check alignment
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer4);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4 -reset>", 0.0, true, 0.0);  // reset gyro
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(driveForward4);
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(turnRightState);
		turnRightState.associateNextState(driveState2);
		driveState2.associateNextState(idleState2);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(turnRightState);
		autoNet.addState(driveState2);
		autoNet.addState(idleState2);
				
		return autoNet;
	}
	
	// **** DEPOSIT GEAR CENTER Network ***** 
	// 1) move camera
	// 2) drive forward for a number of sec
	// 3) go back to idle and stay there 
	private static AutoNetwork createDepositGearCenter() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear (Center) Network>");
		
		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);
								
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action 1>", 0.25, true, 0.0);
		TimeEvent timer3 = new TimeEvent(4.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer3);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
						
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(idleState2);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}
	
	// **** DEPOSIT GEAR RIGHT SIDE Network ***** 
	// 1) Move camera
	// 2) drive forward for a number of sec
	// 3) Turn LEFT a number of degrees
	// 4) drive forward
	// 5) go back to idle and stay there 
	private static AutoNetwork createDepositGearRight() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear (Right Side) Network>");
				
		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);

		/*
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.3, true, 0.0);
		TimeEvent timer2 = new TimeEvent(2.1);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		*/
		
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardMagicAction driveForwardMagic = new DriveForwardMagicAction("<Drive Forward Magic Action>", 79.0, 300, true, 0.0);
		//TimeEvent timer2 = new TimeEvent(2.5);  // drive forward timer event - allow PID time to settle
		ClosedLoopPositionEvent pos1 = new ClosedLoopPositionEvent(79.0, 0.5, 1.0);
		driveState.addAction(driveForwardMagic);
		//driveState.addEvent(timer2);
		driveState.addEvent(pos1);
		
		/*
		AutoState turnLeftState = new AutoState("<Turn Left State>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-60.0, 0.3, false);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-60.0, false, GyroAngleEvent.AnglePolarity.kLessThan);  // don't reset gyro - use heading -60 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
		*/
		
		AutoState turnLeftState = new AutoState("<Turn Left State>");
		TurnPIDAction turnPidAction = new TurnPIDAction("<Turn left PID action>", -62.5, 0.35, true);
		//TimeEvent timer3 = new TimeEvent(2.5);  // timer event - allow PID time to settle
		ClosedLoopAngleEvent angle1 = new ClosedLoopAngleEvent(-62.5,2.0,1.0);
		turnLeftState.addAction(turnPidAction);
		//turnLeftState.addEvent(timer3);
		turnLeftState.addEvent(angle1);
		
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.25, true, 0.0);   // don't reset gyro - use heading -60 deg
		TimeEvent timer4 = new TimeEvent(3.0); 
		//TimeEvent timer4 = new TimeEvent(1.0);    // don't drive all the way to the peg - just check alignment
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer4);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4 -reset>", 0.0, true, 0.0);  // reset gyro 
		idleState2.addAction(deadEnd);
		idleState2.addAction(driveForward4);
				
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveState2);
		driveState2.associateNextState(idleState2);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(turnLeftState);
		autoNet.addState(driveState2);
		autoNet.addState(idleState2);
		
		return autoNet;
	}
				
	// **** Turning Network ***** 
	// 1) move camera
	// 2) Turn RIGHT 90 degrees a number of times
	// 3) Turn LEFT 90 degrees a number of times
	// 4) go back to step 2 
	private static AutoNetwork createTurningForeverNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Turning Forever Network>");
		
		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer0 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer0);
		
		AutoState turnState0 = new AutoState("<Turn Left State 0>");
		TurnPIDAction turnPidAction0 = new TurnPIDAction("<Turn Left PID action 0>", -90.0, 0.5, true);
		TimeEvent timer1 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState0.addAction(turnPidAction0);
		turnState0.addEvent(timer1);

		AutoState turnState1 = new AutoState("<Turn Right State 1>");
		TurnPIDAction turnPidAction1 = new TurnPIDAction("<Turn right PID action 1>", 90.0, 0.5, true);
		TimeEvent timer2 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState1.addAction(turnPidAction1);
		turnState1.addEvent(timer2);
						
		AutoState turnState2 = new AutoState("<Turn Left State 2>");
		TurnPIDAction turnPidAction2 = new TurnPIDAction("<Turn Left PID action 2>", -90.0, 0.5, true);
		TimeEvent timer3 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState2.addAction(turnPidAction2);
		turnState2.addEvent(timer3);

		AutoState turnState3 = new AutoState("<Turn Right State 3>");
		TurnPIDAction turnPidAction3 = new TurnPIDAction("<Turn right PID action 3>", 90.0, 0.5, true);
		TimeEvent timer4 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState3.addAction(turnPidAction3);
		turnState3.addEvent(timer4);
		
		AutoState turnState4 = new AutoState("<Turn Left State 4>");
		TurnPIDAction turnPidAction4 = new TurnPIDAction("<Turn left PID action 4>", -90.0, 0.5, true);
		TimeEvent timer5 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState4.addAction(turnPidAction4);
		turnState4.addEvent(timer5);
		
		AutoState turnState5 = new AutoState("<Turn Right State 5>");
		TurnPIDAction turnPidAction5 = new TurnPIDAction("<Turn right PID action 5>", 90.0, 0.5, true);
		TimeEvent timer6 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState5.addAction(turnPidAction5);
		turnState5.addEvent(timer6);
		
		AutoState turnState6 = new AutoState("<Turn Left State 6>");
		TurnPIDAction turnPidAction6 = new TurnPIDAction("<Turn left PID action 6>", -90.0, 0.5, true);
		TimeEvent timer7 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState6.addAction(turnPidAction6);
		turnState6.addEvent(timer7);

		AutoState turnState7 = new AutoState("<Turn Right State 7>");
		TurnPIDAction turnPidAction7 = new TurnPIDAction("<Turn Right PID action 7>", 90.0, 0.5, true);
		TimeEvent timer8 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState7.addAction(turnPidAction7);
		turnState7.addEvent(timer8);

		AutoState turnState8 = new AutoState("<Turn Left State 8>");
		TurnPIDAction turnPidAction8 = new TurnPIDAction("<Turn left PID action 8>", -90.0, 0.5, true);
		TimeEvent timer9 = new TimeEvent(10.0);  // drive forward timer event - allow PID time to settle
		turnState4.addAction(turnPidAction8);
		turnState4.addEvent(timer9);
		
		// connect each event with a state to move to
		camState.associateNextState(turnState0);
		turnState0.associateNextState(turnState1);
		turnState1.associateNextState(turnState2);
		turnState2.associateNextState(turnState3);
		turnState3.associateNextState(turnState4);
		turnState4.associateNextState(turnState5);
		turnState5.associateNextState(turnState6);
		turnState6.associateNextState(turnState7);
		turnState7.associateNextState(turnState8);
		turnState8.associateNextState(turnState0);   // go back to right turning
						
		autoNet.addState(camState);
		autoNet.addState(turnState0);
		autoNet.addState(turnState1);
		autoNet.addState(turnState2);
		autoNet.addState(turnState3);
		autoNet.addState(turnState4);
		autoNet.addState(turnState5);
		autoNet.addState(turnState6);
		autoNet.addState(turnState7);
		autoNet.addState(turnState8);
				
		return autoNet;
	}

	// **** Pacing Forever Network - Pace back and forth forever ***** 
	// This network uses absolute headings, and does NOT reset the gyro!
	//
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn to 180 deg heading
	// 4) drive forward for a number of sec
	// 5) Turn to 0 deg heading
	// 6) Go back to state 2
	private static AutoNetwork createPacingForeverNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Pacing Forever Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		/*
		AutoState driveState1 = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward1 = new DriveForwardAction("<Drive Forward Action 1>", 0.3, false, 0);
		TimeEvent timer2 = new TimeEvent(2.0);  // drive forward timer event
		driveState1.addAction(driveForward1);
		driveState1.addEvent(timer2);
		*/
		
		AutoState driveState1 = new AutoState("<Drive Magic State 1>");
		DriveForwardMagicAction driveForwardMagic = new DriveForwardMagicAction("<Drive Forward Magic Action>", 120.0, 150, true, 0.0);
		TimeEvent timer2 = new TimeEvent(5.0);  // drive forward timer event
		driveState1.addAction(driveForwardMagic);
		driveState1.addEvent(timer2);
		
		// turn right 180 degrees.  Do it in two 90 degree turns (for PID)
		AutoState turnRightState0 = new AutoState("<Turn right State 0>");
		TurnPIDAction turnRightPidAction0 = new TurnPIDAction("<Turn right PID action 0>", 90.0, 0.3, true);
		TimeEvent timer3 = new TimeEvent(5.0);  // allow PID time to settle
		turnRightState0.addAction(turnRightPidAction0);
		turnRightState0.addEvent(timer3);

		AutoState turnRightState1 = new AutoState("<Turn right State 1>");
		TurnPIDAction turnRightPidAction1 = new TurnPIDAction("<Turn right PID action 0>", 90.0, 0.3, true);
		TimeEvent timer4 = new TimeEvent(5.0);  // allow PID time to settle
		turnRightState1.addAction(turnRightPidAction1);
		turnRightState1.addEvent(timer4);
		
		/*
		AutoState turnRightState = new AutoState("<Turn to 180 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn to 180 deg action>", 180, 0.3, false);
		GyroAngleEvent gyroRight = new GyroAngleEvent(175, false, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for -90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);
		*/
		
		/*
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.3, false, 180);
		TimeEvent timer5 = new TimeEvent(2.0);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer5);
		*/
		
		AutoState driveState2 = new AutoState("<Drive Magic State 2>");
		DriveForwardMagicAction driveForwardMagic2 = new DriveForwardMagicAction("<Drive Forward Magic Action>", 120.0, 150, true, 0.0);
		TimeEvent timer5 = new TimeEvent(5.0);  // drive forward timer event
		driveState2.addAction(driveForwardMagic2);
		driveState2.addEvent(timer5);
		
		// turn left 180 degrees.  Do it in two 90 degree turns (for PID)
		AutoState turnLeftState0 = new AutoState("<Turn left State 0>");
		TurnPIDAction turnLeftPidAction0 = new TurnPIDAction("<Turn left PID action 0>", -90.0, 0.3, true);
		TimeEvent timer6 = new TimeEvent(5.0);  // allow PID time to settle
		turnLeftState0.addAction(turnLeftPidAction0);
		turnLeftState0.addEvent(timer6);

		AutoState turnLeftState1 = new AutoState("<Turn left State 1>");
		TurnPIDAction turnLeftPidAction1 = new TurnPIDAction("<Turn left PID action 1>", -90.0, 0.3, true);
		TimeEvent timer7 = new TimeEvent(5.0);  // allow PID time to settle
		turnLeftState1.addAction(turnLeftPidAction1);
		turnLeftState1.addEvent(timer7);
		
		/*
		AutoState turnLeftState = new AutoState("<Turn to 0 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn to 0 deg action>", 5, 0.3, false);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(5, false, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for +90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
		*/
		
		// connect each event with a state to move to
		// last state loops back!
		idleState.associateNextState(driveState1);
		driveState1.associateNextState(turnRightState0);
		turnRightState0.associateNextState(turnRightState1);
		turnRightState1.associateNextState(driveState2);
		driveState2.associateNextState(turnLeftState0);
		turnLeftState0.associateNextState(turnLeftState1);
		turnLeftState1.associateNextState(driveState1);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState1);
		autoNet.addState(turnRightState0);
		autoNet.addState(turnRightState1);
		autoNet.addState(driveState2);
		autoNet.addState(turnLeftState0);
		autoNet.addState(turnLeftState1);
				
		return autoNet;
	}
	
	
		
	/*****************************************************************************************************/
	/**** DEBUG NETWORKS **** Networks below this are used only for debug - disable during competition ***/
	/*****************************************************************************************************/	

	
	// **** DRIVE AND SHOOT (NEAR) Network ***** 
	// 1) Move camera
	// 2) drive forward for a number of sec
	// 3) Calibrate shooter
	// 4) Shoot at high goal until end of auto
	private static AutoNetwork createDriveAndShootNear() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Near) Network>");

		double x = NEAR_TARGET_X;
		double y = NEAR_TARGET_Y; 
				
		AutoState camState = new AutoState("<Camera Move>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);
		
		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1 - COARSE>", x, y, 5, 5, CAL_SPEED_COARSE_X, CAL_SPEED_COARSE_Y);  
		CalibratedEvent calEvent1 = new CalibratedEvent(x, y, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		AutoState targetCalState2 = new AutoState("<Cal Target State 2>");
		CalibrateTargetAction calTarget2 = new CalibrateTargetAction("<Cal Target Action 2 - FINE>", x, y, 1, 1, CAL_SPEED_FINE_X, CAL_SPEED_FINE_Y); 
		CalibratedEvent calEvent2 = new CalibratedEvent(x, y, 1, 1);
		targetCalState2.addAction(calTarget2);
		targetCalState2.addEvent(calEvent2);
		
		AutoState targetCalState3 = new AutoState("<Cal Target State 3>");
		CalibrateTargetAction calTarget3 = new CalibrateTargetAction("<Cal Target Action 3 - EXTRAFINE>", x, y, 1, 10, CAL_SPEED_EXTRAFINE_X, CAL_SPEED_EXTRAFINE_Y); 
		CalibratedEvent calEvent3 = new CalibratedEvent(x, y, 1, 10);
		targetCalState3.addAction(calTarget3);
		targetCalState3.addEvent(calEvent3);
		
		// Shoot state - keep shooting until end of auto (do not leave state)
		AutoState shootState = new AutoState("<Shoot state - low>");
		ShootAction shootAct = new ShootAction("<Shoot Action - low>", BallManagement.MOTOR_LOW);
		shootState.addAction(shootAct);
				
		// connect each event with a state to move to
		camState.associateNextState(targetCalState);
		targetCalState.associateNextState(targetCalState2);
		targetCalState2.associateNextState(targetCalState3);
		targetCalState3.associateNextState(shootState);
						
		autoNet.addState(camState);
		autoNet.addState(targetCalState);
		autoNet.addState(targetCalState2);
		autoNet.addState(targetCalState3);
		autoNet.addState(shootState);
		
		return autoNet;
	}

	// **** DRIVE AND SHOOT (MEDIUM) Network ***** 
	// 1) Move camera
	// 2) drive forward for a number of sec
	// 3) Calibrate shooter
	// 4) Shoot at high goal until end of auto
	private static AutoNetwork createDriveAndShootMedium() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Medium) Network>");
		
		double x = MEDIUM_TARGET_X;
		double y = MEDIUM_TARGET_Y; 
				
		AutoState camState = new AutoState("<Camera Move>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);
		
		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1 - COARSE>", x, y, 5, 5, CAL_SPEED_COARSE_X, CAL_SPEED_COARSE_Y);  
		CalibratedEvent calEvent1 = new CalibratedEvent(x, y, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		AutoState targetCalState2 = new AutoState("<Cal Target State 2>");
		CalibrateTargetAction calTarget2 = new CalibrateTargetAction("<Cal Target Action 2 - FINE>", x, y, 1, 1, CAL_SPEED_FINE_X, CAL_SPEED_FINE_Y); 
		CalibratedEvent calEvent2 = new CalibratedEvent(x, y, 1, 1);
		targetCalState2.addAction(calTarget2);
		targetCalState2.addEvent(calEvent2);
		
		AutoState targetCalState3 = new AutoState("<Cal Target State 3>");
		CalibrateTargetAction calTarget3 = new CalibrateTargetAction("<Cal Target Action 3 - EXTRAFINE>", x, y, 1, 10, CAL_SPEED_EXTRAFINE_X, CAL_SPEED_EXTRAFINE_Y); 
		CalibratedEvent calEvent3 = new CalibratedEvent(x, y, 1, 10);
		targetCalState3.addAction(calTarget3);
		targetCalState3.addEvent(calEvent3);
		
		// Shoot state - keep shooting until end of auto (do not leave state)
		AutoState shootState = new AutoState("<Shoot state - medium>");
		ShootAction shootAct = new ShootAction("<Shoot Action - medium>", BallManagement.MOTOR_MEDIUM);
		shootState.addAction(shootAct);
				
		// connect each event with a state to move to
		camState.associateNextState(targetCalState);
		targetCalState.associateNextState(targetCalState2);
		targetCalState2.associateNextState(targetCalState3);
		targetCalState3.associateNextState(shootState);
						
		autoNet.addState(camState);
		autoNet.addState(targetCalState);
		autoNet.addState(targetCalState2);
		autoNet.addState(targetCalState3);
		autoNet.addState(shootState);
		
		return autoNet;
	}
	
	/*****************************************************************************************/
	/**** LEGACY NETWORKS **** Networks below this are for reference only and are not used ***/
	/*****************************************************************************************/
	// **** DEPOSIT GEAR AND SHOOT BLUE CENTER Network ***** 
	// 1) move camera down
	// 2) drive forward for a number of sec, then stop
	// 3) WAIT for human player to pull gear
	// 4) back up a number of sec
	// 5) turn to the left -135 deg (toward boiler) and move camera up 
	// 6) calibrate to medium target
	// 7) shoot until end of auto
	private static AutoNetwork createDepositGearAndShootBlueCenter() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear and Shoot (Blue Center) Network>");

		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);
								
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action 1>", 0.3, true, 0.0);
		TimeEvent timer3 = new TimeEvent(3.0);  // drive forward timer event -OR-
		driveState.addAction(driveForward);
		driveState.addEvent(timer3);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction idleAct = new IdleAction("<idle Action>");
		TimeEvent timer4 = new TimeEvent(3.0);  // wait for gear to be pulled
		idleState2.addAction(idleAct);
		idleState2.addEvent(timer4);
		
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveBackward = new DriveForwardAction("<Drive Backward Action>", -0.3, true, 0.0);
		TimeEvent timer5 = new TimeEvent(1.0);  // drive forward timer event
		driveState2.addAction(driveBackward);
		driveState2.addEvent(timer5);
		
		AutoState turnLeftState = new AutoState("<Turn around and cam move>");
		TurnAction turnLeftAction = new TurnAction("<Turn around action>",-95.0, 0.4, true);
		CameraAction camAct2 = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		GyroAngleEvent gyroRight = new GyroAngleEvent(-95.0, true, GyroAngleEvent.AnglePolarity.kLessThan);
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addAction(camAct2);
		turnLeftState.addEvent(gyroRight);
				
		// Shoot state - keep shooting until end of auto (do not leave state)
		AutoState shootState = new AutoState("<Shoot state - Medium>");
		ShootAction shootAct = new ShootAction("<Shoot Action - Medium>", BallManagement.MOTOR_MEDIUM);
		shootState.addAction(shootAct);
				
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(idleState2);
		idleState2.associateNextState(driveState2);
		driveState2.associateNextState(turnLeftState);
		turnLeftState.associateNextState(shootState);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(idleState2);
		autoNet.addState(driveState2);
		autoNet.addState(turnLeftState);
		autoNet.addState(shootState);
		
		return autoNet;
	}	
		
	// **** DEPOSIT GEAR AND SHOOT RED CENTER Network ***** 
	// 1) move camera down
	// 2) drive forward for a number of sec, then stop
	// 3) WAIT for human player to pull gear
	// 4) back up a number of sec
	// 5) turn around (toward boiler) and move camera up 
	// 6) calibrate to medium target
	// 7) shoot until end of auto
	private static AutoNetwork createDepositGearAndShootRedCenter() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear and Shoot (Red Center) Network>");

		AutoState camState = new AutoState("<Move Camera>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.GEAR_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);
								
		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action 1>", 0.3, true, 0.0);
		TimeEvent timer3 = new TimeEvent(3.0);  // drive forward timer event -OR-
		driveState.addAction(driveForward);
		driveState.addEvent(timer3);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction idleAct = new IdleAction("<idle Action>");
		TimeEvent timer4 = new TimeEvent(3.0);  // wait for gear to be pulled
		idleState2.addAction(idleAct);
		idleState2.addEvent(timer4);
		
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveBackward = new DriveForwardAction("<Drive Backward Action>", -0.3, true, 0.0);
		TimeEvent timer5 = new TimeEvent(1.0);  // drive forward timer event
		driveState2.addAction(driveBackward);
		driveState2.addEvent(timer5);
		
		AutoState turnRightState = new AutoState("<Turn around and cam move>");
		TurnAction turnRightAction = new TurnAction("<Turn around action>",95, 0.4, true);
		CameraAction camAct2 = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		GyroAngleEvent gyroRight = new GyroAngleEvent(95, true, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addAction(camAct2);
		turnRightState.addEvent(gyroRight);
				
		// Shoot state - keep shooting until end of auto (do not leave state)
		AutoState shootState = new AutoState("<Shoot state - Medium>");
		ShootAction shootAct = new ShootAction("<Shoot Action - Medium>", BallManagement.MOTOR_MEDIUM);
		shootState.addAction(shootAct);
				
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(idleState2);
		idleState2.associateNextState(driveState2);
		driveState2.associateNextState(turnRightState);
		turnRightState.associateNextState(shootState);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(idleState2);
		autoNet.addState(driveState2);
		autoNet.addState(turnRightState);
		autoNet.addState(shootState);
		
		return autoNet;
	}

	// **** DRIVE AND SHOOT BLUE LEFT SIDE (NEAR) Network ***** 
	// 1) Move camera
	// 2) drive forward for a number of sec
	// 3) Turn LEFT a number of degrees
	// 4) Shoot at high goal until end of auto
	private static AutoNetwork createDriveAndShootBlueLeft() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Blue Left Side) Network>");
				
		AutoState camState = new AutoState("<Camera Move>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.4, true, 0.0);
		TimeEvent timer2 = new TimeEvent(2.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		/*
		AutoState driveState = new AutoState("<Drive Magic State 1>");
		DriveForwardMagicAction driveForwardMagic = new DriveForwardMagicAction("<Drive Forward Magic Action>", 60.0, 150, true, 0.0);
		TimeEvent timer2 = new TimeEvent(3.0);  // drive forward timer event
		driveState.addAction(driveForwardMagic);
		driveState.addEvent(timer2);
		*/
		
		AutoState turnLeftState = new AutoState("<Turn Left State>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-145, 0.4, false);     // don't reset gyro - use -145 deg heading
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-145, false, GyroAngleEvent.AnglePolarity.kLessThan);
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
		
		// Shoot state - keep shooting until end of auto (do not leave state)
		AutoState shootState = new AutoState("<Shoot state - auto>");
		ShootAction shootAct = new ShootAction("<Shoot Action - auto>", BallManagement.MOTOR_AUTO_BLUE);
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4 -reset>", 0.0, true, 0.0);  // reset gyro 
		shootState.addAction(shootAct);
		shootState.addAction(driveForward4);
				
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(shootState);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(turnLeftState);
		autoNet.addState(shootState);
		
		return autoNet;
	}
	
	// **** DRIVE AND SHOOT RED RIGHT SIDE (Near) Network ***** 
	// 1) move camera
	// 2) drive forward for a number of sec
	// 3) Turn RIGHT a number of degrees
	// 4) Shoot at high goal until end of auto 
	private static AutoNetwork createDriveAndShootRedRight() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Red Right Side) Network>");

		AutoState camState = new AutoState("<Camera Move>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);		
		camState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.4, true, 0.0);
		TimeEvent timer2 = new TimeEvent(2.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		/*
		AutoState driveState = new AutoState("<Drive Magic State 1>");
		DriveForwardMagicAction driveForwardMagic = new DriveForwardMagicAction("<Drive Forward Magic Action>", 60.0, 150, true, 0.0);
		TimeEvent timer2 = new TimeEvent(3.0);  // drive forward timer event
		driveState.addAction(driveForwardMagic);
		driveState.addEvent(timer2);
		*/
		
		AutoState turnRightState = new AutoState("<Turn Right State>");
		TurnAction turnRightAction = new TurnAction("<Turn Right action>",145, 0.4, false);     // don't reset gyro - use 145 deg heading
		GyroAngleEvent gyroRight = new GyroAngleEvent(145, false, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);
		
		// Shoot state - keep shooting until end of auto (do not leave state)
		AutoState shootState = new AutoState("<Shoot state - auto>");
		ShootAction shootAct = new ShootAction("<Shoot Action - auto>", BallManagement.MOTOR_AUTO_RED);
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4 -reset>", 0.0, true, 0.0);  // reset gyro 
		shootState.addAction(shootAct);
		shootState.addAction(driveForward4);
				
		// connect each event with a state to move to
		camState.associateNextState(driveState);
		driveState.associateNextState(turnRightState);
		turnRightState.associateNextState(shootState);
						
		autoNet.addState(camState);
		autoNet.addState(driveState);
		autoNet.addState(turnRightState);
		autoNet.addState(shootState);
		
		return autoNet;
	}

	// **** SHOOT AND DRIVE BLUE LEFT SIDE (NEAR) Network ***** 
	// 1) Move camera
	// 2) Shoot at high goal for a number of sec
	// 3) drive backward for a number of sec
	// 4) Turn RIGHT a number of degrees
	// 5) drive forward (across baseline)
	// 6) go to idle and stay there
	private static AutoNetwork createShootAndDriveBlueLeft() {
		
		AutoNetwork autoNet = new AutoNetwork("<Shoot and Drive (Blue Left Side) Network>");
				
		AutoState camState = new AutoState("<Camera Move>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);

		// Shoot state
		AutoState shootState = new AutoState("<Shoot state - auto>");
		ShootAction shootAct = new ShootAction("<Shoot Action - auto>", BallManagement.MOTOR_AUTO_BLUE);
		TimeEvent timer2 = new TimeEvent(8.0);  // shooting timer event
		shootState.addAction(shootAct);
		shootState.addEvent(timer2);

		AutoState driveState = new AutoState("<Drive Backward State>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Backward Action>", -0.3, true, 0.0);
		TimeEvent timer3 = new TimeEvent(0.5);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer3);
		
		AutoState turnRightState = new AutoState("<Turn right State>");
		TurnAction turnRightAction = new TurnAction("<Turn right action>",90, 0.4, true);
		GyroAngleEvent gyroRight = new GyroAngleEvent(90, true, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);
		
		AutoState driveState2 = new AutoState("<Drive Forward State>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action>", 0.4, true, 0.0);
		TimeEvent timer4 = new TimeEvent(2.0);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer4);
		
		AutoState idleState = new AutoState("<Idle State>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState.addAction(deadEnd);
			
		// connect each event with a state to move to
		camState.associateNextState(shootState);
		shootState.associateNextState(driveState);
		driveState.associateNextState(turnRightState);
		turnRightState.associateNextState(driveState2);
		driveState2.associateNextState(idleState);
						
		autoNet.addState(camState);
		autoNet.addState(shootState);
		autoNet.addState(driveState);
		autoNet.addState(turnRightState);
		autoNet.addState(driveState2);
		autoNet.addState(idleState);
		
		return autoNet;
	}
		
	// **** SHOOT AND DRIVE RED RIGHT SIDE (NEAR) Network ***** 
	// 1) Move camera
	// 2) Shoot at high goal for a number of sec
	// 3) drive backward for a number of sec
	// 4) Turn LEFT a number of degrees
	// 5) drive forward (across baseline)
	// 6) go to idle and stay there
	private static AutoNetwork createShootAndDriveRedRight() {
		
		AutoNetwork autoNet = new AutoNetwork("<Shoot and Drive (Red Right Side) Network>");
				
		AutoState camState = new AutoState("<Camera Move>");
		CameraAction camAct = new CameraAction("<Camera Move>",CameraControl.BOILER_CAM_POS);
		TimeEvent timer1 = new TimeEvent(0.1);  // timer event
		camState.addAction(camAct);
		camState.addEvent(timer1);

		// Shoot state
		AutoState shootState = new AutoState("<Shoot state - auto>");
		ShootAction shootAct = new ShootAction("<Shoot Action - auto>", BallManagement.MOTOR_AUTO_RED);
		TimeEvent timer2 = new TimeEvent(8.0);  // shooting timer event
		shootState.addAction(shootAct);
		shootState.addEvent(timer2);

		AutoState driveState = new AutoState("<Drive Backward State>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Backward Action>", -0.3, true, 0.0);
		TimeEvent timer3 = new TimeEvent(0.5);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer3);
		
		AutoState turnLeftState = new AutoState("<Turn left State>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-90, 0.4, true);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-90, true, GyroAngleEvent.AnglePolarity.kLessThan);
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
		
		AutoState driveState2 = new AutoState("<Drive Forward State>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action>", 0.4, true, 0.0);
		TimeEvent timer4 = new TimeEvent(2.0);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer4);
		
		AutoState idleState = new AutoState("<Idle State>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState.addAction(deadEnd);
			
		// connect each event with a state to move to
		camState.associateNextState(shootState);
		shootState.associateNextState(driveState);
		driveState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveState2);
		driveState2.associateNextState(idleState);
						
		autoNet.addState(camState);
		autoNet.addState(shootState);
		autoNet.addState(driveState);
		autoNet.addState(turnLeftState);
		autoNet.addState(driveState2);
		autoNet.addState(idleState);
		
		return autoNet;
	}
	
	
	// ****  [FOLLOW TARGET] Network - mainly for autotargeting testing - does not shoot ***** 
	// 1) be idle for a number of sec
	// 2) calibrate shooter continuously!  Never stop following target!  NEVER!
	private static AutoNetwork createTargetFollowerNetwork() {

		AutoNetwork autoNet = new AutoNetwork("<Target Follower Network>");

		// create states
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction idleStart = new IdleAction("<Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(idleStart);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);
		
		AutoState targetCalState = new AutoState("<Cal Target FOREVER State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>",80, 60, 5, 5, 0.2, 0.2);
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		targetCalState.addAction(calTarget);
		targetCalState.addAction(doSomething4);
		targetCalState.addAction(doSomething5);
		
		// connect each state with a state to move to
		idleState.associateNextState(targetCalState);
						
		autoNet.addState(idleState);
		autoNet.addState(targetCalState);
		
		return autoNet;
	}

	// **** MOVE FORWARD Network - slow and steady ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) go back to idle and stay there 
	private static AutoNetwork createDriveForwardNetwork_Slow() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive Forward Network - Slow>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action - Slow>", 0.5, true, 0.0);
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		TimeEvent timer2 = new TimeEvent(3.0);  // drive forward timer event
		driveState.addAction(driveForward);
		idleState.addAction(doSomething4);
		idleState.addAction(doSomething5);
		driveState.addEvent(timer2);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}
	
	// **** MOVE FORWARD FOREVER Network - slow and steady ***** 
	// 1) be idle for a number of sec
	// 2) drive forward forever (never stop)
	private static AutoNetwork createDriveForwardForeverNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive Forward Network - Slow>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action - Slow>", 0.5, true, 0.0);
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		driveState.addAction(driveForward);
		idleState.addAction(doSomething4);
		idleState.addAction(doSomething5);
						
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
				
		return autoNet;
	}
	
	// **** COMPLEX DRIVING Network - drive in L pattern and return to original spot ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec X
	// 3) Turn 90 deg left
	// 4) drive forward for a number of sec Y
	// 5) Turn around (180 deg right)
	// 6) drive forward for a number of sec Y
	// 7) Turn 90 deg right
	// 8) drive forward for a number of sec X
	// 9) Turn around (180 deg left)
	// 10) go back to idle and stay there 
	private static AutoNetwork createComplexDrivingNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Complex Driving Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState1 = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward1 = new DriveForwardAction("<Drive Forward Action 1>", 0.75, true, 0.0);
		TimeEvent timer2 = new TimeEvent(1.5);  // drive forward timer event
		driveState1.addAction(driveForward1);
		driveState1.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -90 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-90, 0.5, true);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-90, true, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
			
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.75, true, 0.0);
		TimeEvent timer3 = new TimeEvent(1.5);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer3);
		
		AutoState aboutFaceRightState = new AutoState("<About Face Right State +180 deg>");
		TurnAction aboutFaceRightAction = new TurnAction("<About Face right action>",180, 0.5, true);
		GyroAngleEvent gyroAboutFaceRight = new GyroAngleEvent(180, true, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +180 deg
		aboutFaceRightState.addAction(aboutFaceRightAction);
		aboutFaceRightState.addEvent(gyroAboutFaceRight);

		AutoState driveState3 = new AutoState("<Drive State 3>");
		DriveForwardAction driveForward3 = new DriveForwardAction("<Drive Forward Action 3>", 0.75, true, 0.0);
		TimeEvent timer4 = new TimeEvent(1.5);  // drive forward timer event
		driveState3.addAction(driveForward3);
		driveState3.addEvent(timer4);

		AutoState turnRightState = new AutoState("<Turn Right State +90 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn right action>",90, 0.5, true);
		GyroAngleEvent gyroRight = new GyroAngleEvent(90, true, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState driveState4 = new AutoState("<Drive State 4>");
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4>", 0.75, true, 0.0);
		TimeEvent timer5 = new TimeEvent(1.5);  // drive forward timer event
		driveState4.addAction(driveForward4);
		driveState4.addEvent(timer5);

		AutoState aboutFaceLeftState = new AutoState("<About Face Left State -180 deg>");
		TurnAction aboutFaceLeftAction = new TurnAction("<About Face left action>",-180, 0.5, true);
		GyroAngleEvent gyroAboutFaceLeft = new GyroAngleEvent(-180, true, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -180 deg
		aboutFaceLeftState.addAction(aboutFaceLeftAction);
		aboutFaceLeftState.addEvent(gyroAboutFaceLeft);

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState1);
		driveState1.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveState2);
		driveState2.associateNextState(aboutFaceRightState);
		aboutFaceRightState.associateNextState(driveState3);
		driveState3.associateNextState(turnRightState);
		turnRightState.associateNextState(driveState4);
		driveState4.associateNextState(aboutFaceLeftState);
		aboutFaceLeftState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState1);
		autoNet.addState(turnLeftState);
		autoNet.addState(driveState2);
		autoNet.addState(aboutFaceRightState);
		autoNet.addState(driveState3);
		autoNet.addState(turnRightState);
		autoNet.addState(driveState4);
		autoNet.addState(aboutFaceLeftState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}

	// **** ABSOLUTE COMPLEX DRIVING Network - drive in L pattern and return to original spot ***** 
	// Uses absolute angle headings instead of relative angles between states
	// Note: this network does NOT reset the gyro!!
	//
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec X
	// 3) Turn to -90 deg heading
	// 4) drive forward for a number of sec Y
	// 5) Turn to +90 deg heading
	// 6) drive forward for a number of sec Y
	// 7) Turn to +180 deg heading
	// 8) drive forward for a number of sec X
	// 9) Turn to 0 deg heading
	// 10) go back to idle and stay there 
	private static AutoNetwork createAbsoluteComplexDrivingNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Absolute Complex Driving Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState1 = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward1 = new DriveForwardAction("<Drive Forward Action 1>", 0.75, false, 0.0);
		TimeEvent timer2 = new TimeEvent(1.5);  // drive forward timer event
		driveState1.addAction(driveForward1);
		driveState1.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn to -90 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn to -90 deg action>",-90, 0.5, false);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-90, false, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
			
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.75, false, -90);
		TimeEvent timer3 = new TimeEvent(1.5);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer3);
		
		AutoState aboutFaceRightState = new AutoState("<Turn to 90 deg>");
		TurnAction aboutFaceRightAction = new TurnAction("<Turn to 90 deg action>", 90, 0.5, false);
		GyroAngleEvent gyroAboutFaceRight = new GyroAngleEvent(90, false, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +180 deg
		aboutFaceRightState.addAction(aboutFaceRightAction);
		aboutFaceRightState.addEvent(gyroAboutFaceRight);

		AutoState driveState3 = new AutoState("<Drive State 3>");
		DriveForwardAction driveForward3 = new DriveForwardAction("<Drive Forward Action 3>", 0.75, false, 90);
		TimeEvent timer4 = new TimeEvent(1.5);  // drive forward timer event
		driveState3.addAction(driveForward3);
		driveState3.addEvent(timer4);

		AutoState turnRightState = new AutoState("<Turn to 180 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn to 180 deg action>",180, 0.5, false);
		GyroAngleEvent gyroRight = new GyroAngleEvent(180, false, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState driveState4 = new AutoState("<Drive State 4>");
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4>", 0.75, false, 180);
		TimeEvent timer5 = new TimeEvent(1.5);  // drive forward timer event
		driveState4.addAction(driveForward4);
		driveState4.addEvent(timer5);

		AutoState aboutFaceLeftState = new AutoState("<Turn to 0 deg>");
		TurnAction aboutFaceLeftAction = new TurnAction("<Turn to 0 deg action>", 0, 0.5, false);
		GyroAngleEvent gyroAboutFaceLeft = new GyroAngleEvent(0, false, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -180 deg
		aboutFaceLeftState.addAction(aboutFaceLeftAction);
		aboutFaceLeftState.addEvent(gyroAboutFaceLeft);

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState1);
		driveState1.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveState2);
		driveState2.associateNextState(aboutFaceRightState);
		aboutFaceRightState.associateNextState(driveState3);
		driveState3.associateNextState(turnRightState);
		turnRightState.associateNextState(driveState4);
		driveState4.associateNextState(aboutFaceLeftState);
		aboutFaceLeftState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState1);
		autoNet.addState(turnLeftState);
		autoNet.addState(driveState2);
		autoNet.addState(aboutFaceRightState);
		autoNet.addState(driveState3);
		autoNet.addState(turnRightState);
		autoNet.addState(driveState4);
		autoNet.addState(aboutFaceLeftState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}
	
	// **** SPINNY Network - Spin X revolutions in one direction, pause, Spin X revolutions back ***** 
	// 1) be idle for a number of sec
	// 2) Turn 1080 deg left (three turns)
	// 3) be idle for a number of sec
	// 4) Turn 1080 deg right (three turns)
	// 5) go back to idle and stay there 
	private static AutoNetwork createSpinnyNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Spinny Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -1080 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-1080, 0.5, true);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-1080, true, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
	
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction pauseAction = new IdleAction("<Pause Action>");
		TimeEvent timer2 = new TimeEvent(5.0);  // timer event
		idleState2.addAction(pauseAction);
		idleState2.addEvent(timer2);
		
		AutoState turnRightState = new AutoState("<Turn Right State +1080 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn right action>",1080,  0.5, true);
		GyroAngleEvent gyroRight = new GyroAngleEvent(1080, true, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState idleState3 = new AutoState("<Idle State 3>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState3.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(idleState2);
		idleState2.associateNextState(turnRightState);
		turnRightState.associateNextState(idleState3);
						
		autoNet.addState(idleState);
		autoNet.addState(turnLeftState);
		autoNet.addState(idleState2);
		autoNet.addState(turnRightState);
		autoNet.addState(idleState3);
				
		return autoNet;
	}
	
	// **** Test Network - does nothing except transitions states ***** 
	private static AutoNetwork createTestNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Test Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(10.0);  // timer event
		idleState.addAction(startIdle);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction startIdle2 = new IdleAction("<Start Idle Action 2>");
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		TimeEvent timer2 = new TimeEvent(10.0);  // timer event
		idleState2.addAction(startIdle2);
		idleState2.addAction(doSomething4);
		idleState2.addAction(doSomething5);
		idleState2.addEvent(timer2);
		
		AutoState idleState3 = new AutoState("<Idle State 3>");
		IdleAction startIdle3 = new IdleAction("<Start Idle Action 3>");
		IdleAction doSomething6 = new IdleAction("<Placeholder Action 6>");
		IdleAction doSomething7 = new IdleAction("<Placeholder Action 7>");
		TimeEvent timer3 = new TimeEvent(10.0);  // timer event
		idleState3.addAction(startIdle3);
		idleState3.addAction(doSomething6);
		idleState3.addAction(doSomething7);
		idleState3.addEvent(timer3);
		
		AutoState idleState4 = new AutoState("<Idle State 4>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState4.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(idleState2);
		idleState2.associateNextState(idleState3);
		idleState3.associateNextState(idleState4);
						
		autoNet.addState(idleState);
		autoNet.addState(idleState2);
		autoNet.addState(idleState3);
		autoNet.addState(idleState4);
				
		return autoNet;
	}
	
}
