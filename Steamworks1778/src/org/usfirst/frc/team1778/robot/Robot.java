
package org.usfirst.frc.team1778.robot;

import FreezyDrive.FreezyDriveTrain;
import NetworkComm.InputOutputComm;
import NetworkComm.RPIComm;
import StateMachine.AutoStateMachine;
import Systems.ShooterAssembly;
import edu.wpi.first.wpilibj.IterativeRobot;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	protected AutoStateMachine autoSM;
	/**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	  	    	
		RPIComm.initialize();
		InputOutputComm.initialize();
        ShooterAssembly.initialize();
        FreezyDriveTrain.initialize();
        
		autoSM = new AutoStateMachine();
		
    	InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"MainLog","robot initialized...");        
    }
    
    public void autonomousInit() {
    	InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"MainLog","autonomous mode...");
    	RPIComm.autoInit();
    	    	
    	autoSM.start();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	RPIComm.updateValues();
    	
    	autoSM.process();
    	
    }

    public void teleopInit() {
    	InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"MainLog","teleop mode...");

    	RPIComm.teleopInit();
    	ShooterAssembly.teleopInit();  	
    	FreezyDriveTrain.teleopInit();	
    }
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	
    	RPIComm.updateValues();  
        ShooterAssembly.teleopPeriodic();	
        FreezyDriveTrain.teleopPeriodic();    
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
