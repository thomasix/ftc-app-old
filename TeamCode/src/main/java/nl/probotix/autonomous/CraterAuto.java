package nl.probotix.autonomous;

import android.util.Log;

import com.disnodeteam.dogecv.CameraViewDisplay;
import com.disnodeteam.dogecv.DogeCV;
import com.disnodeteam.dogecv.detectors.roverrukus.GoldDetector;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import nl.probotix.RuckusHardware;
import nl.probotix.helpers.AutoHelper;
import nl.probotix.helpers.HeavyLiftStages;

/**
 * Copyright 2018 (c) ProBotiX
 */

@Autonomous(name = "Auto: Crater", group = "RuckusAuto")
public class CraterAuto extends LinearOpMode {

    private RuckusHardware ruckusHardware;
    private AutoHelper autoHelper;
    private String opModeName = "AutoCrater";

    private GoldDetector detector;

    private int stage = 4;

    @Override
    public void runOpMode() throws InterruptedException {

        //start of initialization
        telemetry.addData("Status", "Initializing....");
        telemetry.update();
        Log.d("Ruckus", opModeName + " > Status > Initializing...");

        //setting up hardware class and helpers
        ruckusHardware = new RuckusHardware(hardwareMap);
        autoHelper = new AutoHelper(ruckusHardware, this);

        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Now setting up DogeCV");
        //initializing DogeCV
        detector = new GoldDetector();
        detector.init(hardwareMap.appContext, CameraViewDisplay.getInstance());
        detector.useDefaults();

        //optional tuning
        detector.downscale = 0;
        detector.areaScoringMethod = DogeCV.AreaScoringMethod.MAX_AREA;
        detector.maxAreaScorer.weight = 0.005;
        detector.ratioScorer.weight = 5;
        detector.ratioScorer.perfectRatio = 1.0;

        //initialization done
        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Initialized. Press start...");

        //wait for user to press start
        waitForStart();
        //player presses start

        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Autonomous started");
        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Autonomous started; Landing...");


//7s    //landing the robot
        ruckusHardware.heavyLiftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ruckusHardware.heavyLiftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ruckusHardware.heavyLiftMotor.setPower(0.8);
        ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.OUT.getTicks());
        ElapsedTime timeout = new ElapsedTime();
        timeout.reset();
        while (opModeIsActive() && timeout.milliseconds() < 12000 && ruckusHardware.heavyLiftMotor.isBusy()) {
        }

        DcMotor.RunMode runMode = ruckusHardware.lfWheel.getMode();
        ruckusHardware.setDcMotorMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ElapsedTime elapsedTime = new ElapsedTime();
        elapsedTime.reset();
        while(elapsedTime.milliseconds() < 500 && opModeIsActive()) {
        }
        ruckusHardware.setDcMotorMode(runMode);
        while(elapsedTime.milliseconds() < 1000 && opModeIsActive()) {
        }
        ruckusHardware.telemetryData(telemetry, "DriveAndWait", "CurrentEncoders", "LF: " + ruckusHardware.lfWheel.getCurrentPosition() + " " + ruckusHardware.lfWheel.getTargetPosition() + "\n" +
                "RF: " + ruckusHardware.rfWheel.getCurrentPosition() + " " + ruckusHardware.rfWheel.getTargetPosition() + "\n" +
                "LR: " + ruckusHardware.lrWheel.getCurrentPosition() + " " + ruckusHardware.lrWheel.getTargetPosition() + "\n" +
                "RR: " + ruckusHardware.rrWheel.getCurrentPosition() + " " + ruckusHardware.rrWheel.getTargetPosition());


//0.5s  //unhook
        autoHelper.driveAndWait(0, 80, 0, 0.1, 0.25);

        ElapsedTime liftTimeout = new ElapsedTime();
        liftTimeout.reset();
        //retract landing system (will finish while driving autonomous)
        ruckusHardware.heavyLiftMotor.setPower(1.0);
        ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.IN.getTicks());

//1s    //drive 10 cm forward
        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Landing done; Driving...");
        detector.enable();
        autoHelper.driveAndWait(100, 0, 0, 0.25, 0.5);

//0.5s  //back to middle
        autoHelper.driveAndWait(0, -80, 0, 0.1, 0.25);

        boolean found = false;
//2s    //turn 45 degree left
        autoHelper.driveAndWait(0, 0, 70, 1, 2);

//5s    //turn 90 degree right until seeing gold mineral
        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Drive done; Scanning...");
        autoHelper.driveEncoded(0, 0, -140, 4);
        ElapsedTime runtime = new ElapsedTime();
        runtime.reset();
        while (opModeIsActive() && ruckusHardware.lfWheel.isBusy() && runtime.milliseconds() < 5000) {
            //scan for gold mineral
            double x = detector.getScreenPosition().x;
            double y = detector.getScreenPosition().y;
            if (x > 420 && x < 500 && y > 200) {
                //set motors to current position
                found = true;
                ruckusHardware.stopEncodedDrive();
            }
        }
        if(!found) {
            autoHelper.driveAndWait(0, 0, 70, 2, 3);
        }
        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Gold mineral found: " + found + "; Hitting mineral...");
        detector.disable();


        ruckusHardware.telemetryData(telemetry, "DriveAndWait", "CurrentEncoders", "LF: " + ruckusHardware.lfWheel.getCurrentPosition() + " " + ruckusHardware.lfWheel.getTargetPosition() + "\n" +
                "RF: " + ruckusHardware.rfWheel.getCurrentPosition() + " " + ruckusHardware.rfWheel.getTargetPosition() + "\n" +
                "LR: " + ruckusHardware.lrWheel.getCurrentPosition() + " " + ruckusHardware.lrWheel.getTargetPosition() + "\n" +
                "RR: " + ruckusHardware.rrWheel.getCurrentPosition() + " " + ruckusHardware.rrWheel.getTargetPosition());

        int lfTicks = ruckusHardware.lfWheel.getCurrentPosition();
        int lrTicks = ruckusHardware.lrWheel.getCurrentPosition();

        if(lfTicks < 0 && lrTicks < 0) {
            //left mineral
            autoHelper.driveAndWait(650, 0, 0, 1.5, 2);
            autoHelper.driveAndWait(0, 0, -45, 1, 2);
            autoHelper.driveAndWait(50, 0, 0, 0.25, 0.5);
        } else if(lfTicks > 600 && lrTicks > 600) {
            //right mineral
            autoHelper.driveAndWait(650, 0, 0, 1.5, 2);
            autoHelper.driveAndWait(0, 0, 45, 1, 2);
            autoHelper.driveAndWait(50, 0, 0, 0.25, 0.5);
        } else {
            //middle mineral
            autoHelper.driveAndWait(560, 0, 0, 2, 3);
        }



        ruckusHardware.craterServo.setPosition(0.15);
        elapsedTime = new ElapsedTime();
        elapsedTime.reset();
        while(opModeIsActive() && elapsedTime.milliseconds() < 1000) {
        }

        while(ruckusHardware.heavyLiftMotor.isBusy() && opModeIsActive()) {
        }

//28s   //AUTONOMOUS DONE
        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Autonomous done");
    }
}
