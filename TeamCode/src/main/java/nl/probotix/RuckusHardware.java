package nl.probotix;

import android.util.Log;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.HashMap;

/**
 * Copyright 2018 (c) ProBotiX
 */
public class RuckusHardware {

    private HardwareMap hardwareMap;
    public DcMotor lfWheel, rfWheel, lrWheel, rrWheel, heavyLiftMotor;

    public Servo teamMarkerServo, craterServo;
    //public Servo phoneServo;

    private HashMap<String, String> telemetryData;

    public RuckusHardware(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        this.telemetryData = new HashMap<>();

        lfWheel = hardwareMap.dcMotor.get("lfWheel");
        rfWheel = hardwareMap.dcMotor.get("rfWheel");
        lrWheel = hardwareMap.dcMotor.get("lrWheel");
        rrWheel = hardwareMap.dcMotor.get("rrWheel");
        heavyLiftMotor = hardwareMap.dcMotor.get("heavyLiftMotor");

        rfWheel.setDirection(DcMotorSimple.Direction.REVERSE);
        rrWheel.setDirection(DcMotorSimple.Direction.REVERSE);

        setDcMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setDcMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
        heavyLiftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        heavyLiftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        setZeroMode(DcMotor.ZeroPowerBehavior.BRAKE);
        heavyLiftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        teamMarkerServo = hardwareMap.servo.get("teamMarkerServo");
        craterServo = hardwareMap.servo.get("craterServo");
        teamMarkerServo.setPosition(0.1);
        craterServo.setPosition(0.85);

        reset();
    }

    public void setDcMotorMode(DcMotor.RunMode mode) {
        lfWheel.setMode(mode);
        rfWheel.setMode(mode);
        lrWheel.setMode(mode);
        rrWheel.setMode(mode);
    }

    public void setMotorPowers(double lf, double rf, double lr, double rr) {
        lfWheel.setPower(lf);
        rfWheel.setPower(rf);
        lrWheel.setPower(lr);
        rrWheel.setPower(rr);
    }

    public void reset() {
        lfWheel.setPower(0.0);
        rfWheel.setPower(0.0);
        lrWheel.setPower(0.0);
        rrWheel.setPower(0.0);

    }

    public void telemetryData(Telemetry telemetry, String opModeName, String caption, String text) {
        telemetryData.put(caption, text);
        for(String s : telemetryData.keySet()) {
            //s is caption
            telemetry.addData(s, telemetryData.get(s));
        }
        telemetry.update();
        Log.d("Ruckus", opModeName + " > " + caption + " > " + text);
    }

    public void addWheelTicks(int lf, int rf, int lr, int rr) {
        lfWheel.setTargetPosition(lf + lfWheel.getCurrentPosition());
        rfWheel.setTargetPosition(rf + rfWheel.getCurrentPosition());
        lrWheel.setTargetPosition(lr + lrWheel.getCurrentPosition());
        rrWheel.setTargetPosition(rr + rrWheel.getCurrentPosition());
    }

    public void setNewWheelTargets(int lf, int rf, int lr, int rr) {
        lfWheel.setTargetPosition(lf);
        rfWheel.setTargetPosition(rf);
        lrWheel.setTargetPosition(lr);
        rrWheel.setTargetPosition(rr);
    }

    public void stopEncodedDrive() {
        lfWheel.setTargetPosition(lfWheel.getCurrentPosition());
        rfWheel.setTargetPosition(rfWheel.getCurrentPosition());
        lrWheel.setTargetPosition(lrWheel.getCurrentPosition());
        rrWheel.setTargetPosition(rrWheel.getCurrentPosition());
    }

    public void setZeroMode(DcMotor.ZeroPowerBehavior zeroMode) {
        lfWheel.setZeroPowerBehavior(zeroMode);
        rfWheel.setZeroPowerBehavior(zeroMode);
        lrWheel.setZeroPowerBehavior(zeroMode);
        rrWheel.setZeroPowerBehavior(zeroMode);
    }
}
