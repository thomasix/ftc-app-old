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
            autoHelper.driveAndWait(tileDiagonal, -100, 0, 2, 2.5);
        }

        if(opModeIsActive() && detector.isFound() && mineralAlign == MineralAlign.UNKNOWN) {
            Point p = detector.getScreenPosition();
            double x = p.x;
            double y = p.y;
            ruckusHardware.telemetryData(telemetry, opModeName, "Minerals", "Found a mineral in screen position x:" + x + " y:" + y + " and alignment is CENTER!");
            mineralAlign = MineralAlign.CENTER;
            autoHelper.driveAndWait(150, 0, 0, 0.25, 0.5);
            autoHelper.driveAndWait(-150, 0, 0, 0.25, 0.5);
        }

//1.5s
        if(opModeIsActive()) {
            autoHelper.driveAndWait(0, autoHelper.inchToMM(-14.5), 0, 1, 1.5);
        }

        if(opModeIsActive() && detector.isFound() && mineralAlign == MineralAlign.UNKNOWN) {
            Point p = detector.getScreenPosition();
            double x = p.x;
            double y = p.y;
            ruckusHardware.telemetryData(telemetry, opModeName, "Minerals", "Found a mineral in screen position x:" + x + " y:" + y + " and alignment is RIGHT!");
            mineralAlign = MineralAlign.RIGHT;
            autoHelper.driveAndWait(150, 0, 0, 0.25, 0.5);
            autoHelper.driveAndWait(-150, 0, 0, 0.25, 0.5);
        }

//3s
        if(opModeIsActive()) {
            autoHelper.driveAndWait(0, autoHelper.inchToMM(29), 0, 2, 3);
        }

        if(opModeIsActive() && detector.isFound() && mineralAlign == MineralAlign.UNKNOWN) {
            Point p = detector.getScreenPosition();
            double x = p.x;
            double y = p.y;
            ruckusHardware.telemetryData(telemetry, opModeName, "Minerals", "Found a mineral in screen position x:" + x + " y:" + y + " and alignment is LEFT!");
            mineralAlign = MineralAlign.LEFT;
            autoHelper.driveAndWait(150, 0, 0, 0.25, 0.5);
            autoHelper.driveAndWait(-150, 0, 0, 0.25, 0.5);
        }

        if(opModeIsActive() && mineralAlign == MineralAlign.UNKNOWN) {
            autoHelper.driveAndWait(150, 0, 0, 0.25, 0.5);
            autoHelper.driveAndWait(-150, 0, 0, 0.25, 0.5);
        }

//7s
        autoHelper.driveAndWait(0, 0.5*tileDiagonal, 0, 1, 1.5);
        autoHelper.driveAndWait(0, 0, 45, 1, 1.5);
        autoHelper.driveAndWait(0, 600, 0, 1, 1.5);
        autoHelper.driveAndWait(0, 0, 45, 1, 1.5);
        autoHelper.driveAndWait(0, 150, 0, 0.5, 1);

        if(mineralAlign == MineralAlign.RIGHT) {
            autoHelper.driveAndWait(400, 0, 0, 1, 1.5);
            autoHelper.driveAndWait(0, 0, 45, 1, 1.5);
            autoHelper.driveAndWait(750, 0, 0, 1, 1.5);
            autoHelper.driveAndWait(0, 0, 180, 2, 2.5);
        } else if(mineralAlign == MineralAlign.CENTER) {
            autoHelper.driveAndWait(0, autoHelper.inchToMM(14.5), 0, 1, 1.5);
            autoHelper.driveAndWait(1000, 0, 0, 1.5, 2);
            autoHelper.driveAndWait(0, 0, -135, 1, 1.5);
        } else if(mineralAlign == MineralAlign.LEFT || mineralAlign == MineralAlign.UNKNOWN) {
            autoHelper.driveAndWait(0, autoHelper.inchToMM(29), 0, 1.5, 2);
            autoHelper.driveAndWait(600, 0, 0, 1, 1.5);
            autoHelper.driveAndWait(0, 0, -45, 1, 1.5);
            autoHelper.driveAndWait(600, 0, 0, 1, 1.5);
            autoHelper.driveAndWait(0, 0, -90, 1, 1.5);
        }
        autoHelper.driveAndWait(20, 0, 0, 1.5, 2);
        autoHelper.driveAndWait(1800, 0, 0, 2, 3);
        autoHelper.driveAndWait(600, 0, 0, 3, 4);

//19.5s
    }
}