package StateMachine;

import Systems.BallManagement;

public class ShootAction extends Action {

	private int shooterStrength;
	
	public ShootAction() {
		this.name = "<Shoot Action>";
		shooterStrength = BallManagement.MOTOR_MEDIUM;
	}
	
	public ShootAction(String name, int shootStrength) {
		this.name = name;
		this.shooterStrength = shootStrength;
	}
	
	// action entry
	public void initialize() {
		BallManagement.initialize();
		BallManagement.setShooterStrength(shooterStrength);
		
		super.initialize();
	}
	
	public void process() {
		
		super.process();
	}
	
	public void cleanup() {
		BallManagement.resetMotors();
		
		super.cleanup();
	}
}
