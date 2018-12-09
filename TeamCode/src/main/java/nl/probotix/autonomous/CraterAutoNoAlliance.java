package nl.probotix.autonomous;

import android.util.Log;

import com.disnodeteam.dogecv.CameraViewDisplay;
import com.disnodeteam.dogecv.DogeCV;
import com.disnodeteam.dogecv.detectors.roverrukus.SamplingOrderDetector;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import nl.probotix.RuckusHardware;
import nl.probotix.helpers.AutoHelper;
import nl.probotix.helpers.HeavyLiftStages;

@Autonomous(name = "Crater: NoAlliance", group = "Crater")
public class CraterAutoNoAlliance extends LinearOpMode {

    private RuckusHardware ruckusHardware;
    private AutoHelper autoHelper;
    private String opModeName = "AutoCrater";

    private SamplingOrderDetector detector;

    private SamplingOrderDetector.GoldLocation goldLocation;

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
        //initializing DogeCV SamplingOrderDetector
        detector = new SamplingOrderDetector();
        detector.init(hardwareMap.appContext, CameraViewDisplay.getInstance());
        detector.useDefaults();
        // Optional tuning
        detector.areaScoringMethod = DogeCV.AreaScoringMethod.MAX_AREA;
        detector.maxAreaScorer.weight = 0.001;
        detector.ratioScorer.weight = 15;
        detector.ratioScorer.perfectRatio = 1.0;

        detector.enable();

        //initialization done
        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Initialized. Press start...");

        //wait for user to press start
        waitForStart();
        //player presses start

        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Autonomous started");
        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Autonomous started; Reading sampling order...");

        //reading sampling order
        ElapsedTime elapsedTime = new ElapsedTime();
        elapsedTime.reset();
        boolean looping = true;
        while(elapsedTime.milliseconds() < 1000 && opModeIsActive() && looping) {
            SamplingOrderDetector.GoldLocation loc = detector.getCurrentOrder();
            if(loc != SamplingOrderDetector.GoldLocation.UNKNOWN) {
                goldLocation = loc;
                looping = false;
            }
        }

        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Sampling order detection done; Gold position is " + goldLocation.toString() + "; Landing...");

//10s   //landing the robot
        ruckusHardware.heavyLiftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ruckusHardware.heavyLiftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ruckusHardware.heavyLiftMotor.setPower(1.0);
        ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.OUT.getTicks());
        ElapsedTime timeout = new ElapsedTime();
        timeout.reset();
        while (opModeIsActive() && timeout.milliseconds() < 10000 && ruckusHardware.heavyLiftMotor.isBusy()) {
        }

        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous","Landing done; Unhooking...");

//1s    //unhook
        autoHelper.driveAndWait(0, 80, 0, 0.1, 0.25);
        //retract landing system
        ElapsedTime liftTimeout = new ElapsedTime();
        liftTimeout.reset();
        ruckusHardware.heavyLiftMotor.setPower(1.0);
        ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.IN.getTicks());
        //drive 10 cm forward
        detector.enable();
        autoHelper.driveAndWait(100, 0, 0, 0.25, 0.5);
        //back to middle
        autoHelper.driveAndWait(0, -80, 0, 0.1, 0.25);

        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Unhooking done; Driving...");

        //todo drive to mineral
















    }
}
