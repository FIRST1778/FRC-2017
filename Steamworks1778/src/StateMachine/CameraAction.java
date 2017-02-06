package StateMachine;

import Systems.CameraControl;

public class CameraAction extends Action {
	
	private double camPos = CameraControl.GEAR_CAM_POS;
	
	public CameraAction() {
		this.name = "<Camera Action>";
	}
	
	public CameraAction(String name, double camPos) {
		this.name = name;
		this.camPos = camPos;
	}
	
	// action entry
	public void initialize() {
		CameraControl.initialize();	
		CameraControl.moveToPos(camPos);
		
		// in auto, turn on extra LEDs!
		CameraControl.setCameraLed(true);
		
		super.initialize();
	}
	
	public void process() {
		
		super.process();
	}
	
	public void cleanup() {
		
		super.cleanup();
	}

}
