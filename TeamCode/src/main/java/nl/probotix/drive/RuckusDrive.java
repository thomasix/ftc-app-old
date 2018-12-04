package nl.probotix.drive;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import nl.probotix.RuckusHardware;

/**
 * Copyright 2018 (c) ProBotiX
 */

@TeleOp( name = "Ruckus: Drive", group = "Ruckus")
public class RuckusDrive extends LinearOpMode {

    private RuckusHardware ruckusHardware;
    private ElapsedTime runtime = new ElapsedTime();
    private String opModeName = "Drive";

    private DriveSpeed driveSpeed = DriveSpeed.FOURTH;

    @Override
    public void runOpMode() {
        //start of initialization
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        Log.d("Ruckus", opModeName + " > Status > Initializing...");

        //setting up ethanHardware
        ruckusHardware = new RuckusHardware(hardwareMap);

        //initialization done
        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Initialized. Press start...");

        waitForStart();
        //user pressed start
        runtime.reset();
        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Opmode started");

        /*
         * KNOB FUNCTIONS
         * GAMEPAD1:
         *   LEFTSTICK//DPAD: MECANUM DRIVE
         *   RIGHTSTICK: ROTATE
         *   RIGHT_TRIGGER: SPEED
         *
         *   A: FIRST
         *   B: SECOND
         *   X: THIRD
         *   Y: FOURTH
         */


        while(opModeIsActive()) {
            long startTime = System.currentTimeMillis();

            if(gamepad1.a) {
                driveSpeed = DriveSpeed.FIRST;
            } else if(gamepad1.b) {
                driveSpeed = DriveSpeed.SECOND;
            } else if(gamepad1.x) {
                driveSpeed = DriveSpeed.THIRD;
            } else if(gamepad1.y) {
                driveSpeed = DriveSpeed.FOURTH;
            }

            /*if(gamepad2.dpad_up) {
                ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.OUT.getTicks());
            }
            if(gamepad2.dpad_down) {
                ruckusHardware.heavyLiftMotor.setTargetPosition(HeavyLiftStages.IN.getTicks());
            }*/
            ruckusHardware.heavyLiftMotor.setPower(gamepad2.right_stick_y);
            if(gamepad2.right_bumper) {
                ruckusHardware.teamMarkerServo.setPosition(0.95);
            } else {
                ruckusHardware.teamMarkerServo.setPosition(0.1);
            }

            if(gamepad2.left_bumper) {
                ruckusHardware.craterServo.setPosition(0.1);
            } else {
                ruckusHardware.craterServo.setPosition(0.85);
            }

            //=======================DRIVE MECHANISM===========================
            //DPAD DRIVE
            double leftFrontWheel = 0;
            double leftRearWheel = 0;
            double rightFrontWheel = 0;
            double rightRearWheel = 0;

            //Which directions are pressed on gamepad1?
            boolean left = gamepad1.dpad_right;
            boolean right = gamepad1.dpad_left;
            boolean front = gamepad1.dpad_down;
            boolean back = gamepad1.dpad_up;

            //make speed variable
            double velocity = gamepad1.right_trigger;

            //drive in pressed directions
            if (left) {
                //left
                leftFrontWheel = -velocity;
                rightFrontWheel = velocity;
                leftRearWheel = velocity;
                rightRearWheel = -velocity;
            }
            if (back) {
                //backwards
                leftFrontWheel = -velocity;
                rightFrontWheel = -velocity;
                leftRearWheel = -velocity;
                rightRearWheel = -velocity;
            }
            if (right) {

                leftFrontWheel = velocity;
                rightFrontWheel = -velocity;
                leftRearWheel = -velocity;
                rightRearWheel = velocity;
            }
            if (front) {
                leftFrontWheel = velocity;
                rightFrontWheel = velocity;
                leftRearWheel = velocity;
                rightRearWheel = velocity;
            }
            if (left && front) {
                leftRearWheel = velocity;
                rightFrontWheel = velocity;
                leftFrontWheel = 0;
                rightRearWheel = 0;
            }
            if (left && back) {
                leftRearWheel = 0;
                rightFrontWheel = 0;
                leftFrontWheel = -velocity;
                rightRearWheel = -velocity;
            }
            if (right && front) {
                leftFrontWheel = velocity;
                rightRearWheel = velocity;
                leftRearWheel = 0;
                rightFrontWheel = 0;
            }
            if (right && back) {
                leftFrontWheel = 0;
                rightRearWheel = 0;
                leftRearWheel = -velocity;
                rightFrontWheel = -velocity;
            }

            //rotation on place
            double xases = gamepad1.right_stick_x;

            if (xases != 0) {
                leftFrontWheel = leftFrontWheel + xases;
                leftRearWheel = leftRearWheel + xases;
                rightFrontWheel = rightFrontWheel - xases;
                rightRearWheel = rightRearWheel - xases;
            }

            ruckusHardware.setMotorPowers(leftFrontWheel * driveSpeed.getSpeed(), rightFrontWheel * driveSpeed.getSpeed()
                    , leftRearWheel * driveSpeed.getSpeed(), rightRearWheel * driveSpeed.getSpeed());

        }

        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Stopping robot...");

        //for security reset robot
        ruckusHardware.reset();

        runtime.reset();
        ruckusHardware.telemetryData(telemetry, opModeName, "Status", "Robot stopped.");
    }

    public static enum DriveSpeed {
        FIRST(0.25), SECOND(0.5), THIRD(0.75), FOURTH(1);

        private double speed;

        DriveSpeed(double speed) {
            this.speed = speed;
        }

        public double getSpeed() {
            return speed;
        }
    }
}