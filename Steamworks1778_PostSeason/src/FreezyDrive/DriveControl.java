package FreezyDrive;
import NetworkComm.InputOutputComm;
import Utility.SimpleUtil;

public class DriveControl {
	
	double oldWheel, quickStopAccumulator;
	
	// "Deadband" is the dead zone of the joysticks, for throttle and steering
    private double throttleDeadband = 0.04;
    private double wheelDeadband = 0.02;
	
	public DriveControl(){
		
	}
	
	public void calculateDrive(double throttle, double wheel, boolean isQuickTurn){
		
		
		double wheelNonLinearity;
		wheel = handleDeadband(wheel, wheelDeadband);
        throttle = handleDeadband(throttle, throttleDeadband);
        
		throttle = throttle / 0.6;
		
        if(throttle > 0)
        	wheel = -wheel;
        
        InputOutputComm.putDouble(InputOutputComm.LogTable.kDriveLog,"Teleop/Throttle", throttle);		
		InputOutputComm.putDouble(InputOutputComm.LogTable.kDriveLog,"Teleop/Wheel", wheel);
		
        
        
		double negInertia = wheel - oldWheel;
        oldWheel = wheel;
        
        wheelNonLinearity = 0.5;
        // Apply a sin function that's scaled to make it feel better.
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
                / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
                / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
                / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
		
        double sensitivity;
        double angularPower;
        double linearPower;
        
        double negInertiaAccumulator = 0.0;
        double negInertiaScalar;
        
        
     	if (wheel * negInertia > 0) {
            negInertiaScalar = 2.5;
        } else {
            if (Math.abs(wheel) > 0.65) {
                negInertiaScalar = 5.0;
            } else {
                negInertiaScalar = 3.0;
            }
        }
        
	
        double negInertiaPower = negInertia * negInertiaScalar;
        negInertiaAccumulator += negInertiaPower;

        wheel = wheel + negInertiaAccumulator;
        if (negInertiaAccumulator > 1) {
            negInertiaAccumulator -= 1;
        } else if (negInertiaAccumulator < -1) {
            negInertiaAccumulator += 1;
        } else {
            negInertiaAccumulator = 0;
        }
        linearPower = throttle;
        
        double rightPower,leftPower,overPower;
        
        sensitivity = .85;        
        
        // Calculates quick turn (top right swtich)
        if (isQuickTurn) {
            if (Math.abs(linearPower) < 0.2) {
                double alpha = 0.1;
                quickStopAccumulator = (1 - alpha) * quickStopAccumulator
                        + alpha * 5 * -1 * SimpleUtil.limit(true, wheel, 1);
            }
            overPower = 1.0;
            sensitivity = 1.0;
            angularPower = wheel;
        } else {
            overPower = 0.0;
            angularPower = Math.abs(throttle) * wheel * sensitivity
                    - quickStopAccumulator;
            if (quickStopAccumulator > 1) {
                quickStopAccumulator -= 1;
            } else if (quickStopAccumulator < -1) {
                quickStopAccumulator += 1;
            } else {
                quickStopAccumulator = 0.0;
            }
        }    
        
        rightPower = leftPower = linearPower;
        leftPower += angularPower;
        rightPower -= angularPower;
        
        if (leftPower > 1.0) {
            rightPower -= overPower * (leftPower - 1.0);
            leftPower = 1.0;
        } else if (rightPower > 1.0) {
            leftPower -= overPower * (rightPower - 1.0);
            rightPower = 1.0;
        } else if (leftPower < -1.0) {
            rightPower += overPower * (-1.0 - leftPower);
            leftPower = -1.0;
        } else if (rightPower < -1.0) {
            leftPower += overPower * (-1.0 - rightPower);
            rightPower = -1.0;
        }	
        
        
		
		// sends final values to drive train
		FreezyDrive.FreezyDriveTrain.ChangeSpeed(-leftPower,rightPower);
		
		InputOutputComm.putDouble(InputOutputComm.LogTable.kDriveLog,"Teleop/leftPower", -leftPower);		
		InputOutputComm.putDouble(InputOutputComm.LogTable.kDriveLog,"Teleop/rightPower", rightPower);

	}
	
	// calculates the deadband of the value
	
	public double handleDeadband(double val, double deadband) {
        return (Math.abs(val) > Math.abs(deadband)) ? val : 0.0;
	}
}
