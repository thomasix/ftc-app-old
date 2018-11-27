package nl.probotix.ftc.roverruckus;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class RuckusHardware {

    private DcMotor lfWheel, rfWheel, lrWheel, rrWheel;
    private Servo craterServo, tmServo;

    private HardwareMap hardwareMap;

    public RuckusHardware(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        this.init();
    }

    public void init() {
        this.lfWheel = hardwareMap.dcMotor.get("lfWheel");
        this.rfWheel = hardwareMap.dcMotor.get("rfWheel");
        this.lrWheel = hardwareMap.dcMotor.get("lrWheel");
        this.rrWheel = hardwareMap.dcMotor.get("rrWheel");
    }
}
