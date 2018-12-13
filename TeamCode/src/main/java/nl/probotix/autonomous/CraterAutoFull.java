package nl.probotix.autonomous;

import android.util.Log;

import com.disnodeteam.dogecv.CameraViewDisplay;
import com.disnodeteam.dogecv.DogeCV;
import com.disnodeteam.dogecv.detectors.roverrukus.GoldDetector;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.opencv.core.Point;
import nl.probotix.RuckusHardware;
import nl.probotix.helpers.AutoHelper;
import nl.probotix.helpers.HeavyLiftStages;
import nl.probotix.helpers.MineralAlign;

/**
 * This OpMode does: Landing, sampling own field, sampling alliance field, teammarker in depot, park in crater
 * THIS OPMODE IS UNDER CONSTRUCTION AND ISN'T READY FOR MATCHES!
 * TODO servos
 * */


@Autonomous(name = "Crater: Full", group = "RuckusAuto")
public class CraterAutoFull extends LinearOpMode {

    private RuckusHardware ruckusHardware;
    private AutoHelper autoHelper;
    private String opModeName = "Autonomous: Crater Full";

    private GoldDetector detector;
    private MineralAlign mineralAlign = MineralAlign.UNKNOWN;

    @Override
    public void runOpMode() throws InterruptedException {
        double tileDiagonal = Math.sqrt((60*60)*2);

        //start of initialization
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        Log.d("Ruckus", opModeName + " > Status > Initializing...");

        //setting up hardware and helpers
        this.ruckusHardware = new RuckusHardware(hardwareMap);
        this.autoHelper = new AutoHelper(ruckusHardware, this);

        //setting up DogeCV with camera
        detector = new GoldDetector();
        detector.init(hardwareMap.appContext, CameraViewDisplay.getInstance());
        detector.useDefaults();
        //optional tuning
        detector.areaScoringMethod = DogeCV.AreaScoringMethod.MAX_AREA;
        detector.maxAreaScorer.weight = 0.005;
        detector.ratioScorer.weight = 5;
        detector.ratioScorer.perfectRatio = 1.0;
        detector.enable();

        //init done
        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Initialized; Waiting for start...");

        //wait for user to press start
        waitForStart();

        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Autonomous started!");
        ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Autonomous started; Landing...");

//9s    //landing the robot
        ruckusHardware.heavyLiftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ruckusHardware.heavyLiftMotor.setPower(1);
        ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.OUT.getTicks());
        ElapsedTime liftTime = new ElapsedTime();
        liftTime.reset();
        while(opModeIsActive() && liftTime.milliseconds() < 9000 && ruckusHardware.heavyLiftMotor.isBusy()) {}

        if(opModeIsActive()) {autoHelper.resetEncoders();}

//0.5s  //unhook
        if(opModeIsActive()) {
            ruckusHardware.telemetryData(telemetry, opModeName, "Autonomous", "Landed; Unhooking...");
            autoHelper.driveAndWait(0, 100, 0, 0.1, 0.25);
        }

        //retracting lift system
        if(opModeIsActive()) {
            liftTime.reset();
            ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.IN.getTicks());
            //this can be done while driving all other stuff.. only one thing: make sure it's done at the end of auto
        }

//2.5s  //drive forward towards minerals
        if(opModeIsActive()) {
            autoHelper.driveAndWait(tileDiagonal * 10, -100, 0, 2, 2.5);
        }

        if(opModeIsActive() && detector.isFound()) {
            Point p = detector.getScreenPosition();
            double x = p.x;
            double y = p.y;
            ruckusHardware.telemetryData(telemetry, opModeName, "Minerals", "Found a mineral in screen position x:" + x + " y:" + y + " and alignment is CENTER!");
        }
        detector.disable();

//19.5s
    }
}