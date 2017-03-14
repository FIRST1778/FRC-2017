
package org.usfirst.frc.team1778.robot;

import FreezyDrive.FreezyDriveTrain;
import NetworkComm.InputOutputComm;
import NetworkComm.RPIComm;
import StateMachine.AutoStateMachine;
import Systems.BallManagement;
import Systems.CameraControl;
import Systems.ClimberAssembly;
import Systems.NavXSensor;
import Systems.RioDuinoAssembly;
import Systems.UltrasonicSensor;
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
        BallManagement.initialize();
        FreezyDriveTrain.initialize();
        CameraControl.initialize();
        ClimberAssembly.initialize();
        RioDuinoAssembly.initialize();
        
		autoSM = new AutoStateMachine();
		
    	InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"MainLog","robot initialized...");        
    }
    
    public void autonomousInit() {
    	InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"MainLog","autonomous mode...");
    	RPIComm.autoInit();
    	CameraControl.autoInit();
    	UltrasonicSensor.autoInit();
    	BallManagement.autoInit();
    	RioDuinoAssembly.autonomousInit();
    	
    	autoSM.start();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	RPIComm.updateValues();
    	
    	autoSM.process();
 
    	// debug only
    	//getGyroAngle();
   	
    }

    public void teleopInit() {
    	InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"MainLog","teleop mode...");

    	RPIComm.teleopInit();
    	BallManagement.teleopInit();  	
    	FreezyDriveTrain.teleopInit();	
    	CameraControl.teleopInit();
    	ClimberAssembly.teleopInit();
    	RioDuinoAssembly.teleopInit();
    }
    /**
     * This function is called periodically during operator control
     */
    
	private double getGyroAngle() {
		//double gyroAngle = 0.0;
		//double gyroAngle = NavXSensor.getYaw();  // -180 deg to +180 deg
		double gyroAngle = NavXSensor.getAngle();  // continuous angle (can be larger than 360 deg)
		
		//System.out.println("getGyroAngle:  Gyro angle = " + gyroAngle);
			
		// send output data for test & debug
	    InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/IMU_Connected",NavXSensor.isConnected());
	    InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/IMU_Calibrating",NavXSensor.isCalibrating());

		//System.out.println("gyroAngle = " + gyroAngle);
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Auto/GyroAngle", gyroAngle);		

		return gyroAngle;
	}

    public void teleopPeriodic() {
    	
    	RPIComm.updateValues();  
        BallManagement.teleopPeriodic();	
        FreezyDriveTrain.teleopPeriodic();   
        CameraControl.teleopPeriodic();
        ClimberAssembly.teleopPeriodic();
    }
    
    public void disabledInit() {
    	BallManagement.resetMotors();
    	RPIComm.disabledInit();
    	RioDuinoAssembly.disabledInit();
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
