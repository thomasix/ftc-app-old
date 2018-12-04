package nl.probotix.helpers;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import nl.probotix.RuckusHardware;

/**
 * Copyright 2018 (c) ProBotiX
 */

public class AutoHelper {

    private RuckusHardware ruckusHardware;
    private LinearOpMode opMode;

    public AutoHelper(RuckusHardware ruckusHardware, LinearOpMode opMode) {
        this.ruckusHardware = ruckusHardware;
        this.opMode = opMode;
    }

    public void driveAndWait(double linearX, double linearY, double angularZ, double time, double timeout) {
        if(opMode.opModeIsActive()) {
            driveEncoded(linearX, linearY, angularZ, time);
            ElapsedTime runtime = new ElapsedTime();
            runtime.reset();

            while (opMode.opModeIsActive() && runtime.milliseconds() < timeout * 1000 && ruckusHardware.lfWheel.isBusy() &&
                    ruckusHardware.rfWheel.isBusy() && ruckusHardware.lrWheel.isBusy() &&
                    ruckusHardware.rrWheel.isBusy()) {
                ruckusHardware.telemetryData(opMode.telemetry, "DriveAndWait", "Encoders", "LF: " + ruckusHardware.lfWheel.getCurrentPosition() + " " + ruckusHardware.lfWheel.getTargetPosition() + "\n" +
                        "RF: " + ruckusHardware.rfWheel.getCurrentPosition() + " " + ruckusHardware.rfWheel.getTargetPosition() + "\n" +
                        "LR: " + ruckusHardware.lrWheel.getCurrentPosition() + " " + ruckusHardware.lrWheel.getTargetPosition() + "\n" +
                        "RR: " + ruckusHardware.rrWheel.getCurrentPosition() + " " + ruckusHardware.rrWheel.getTargetPosition());
            }
            ruckusHardware.setMotorPowers(0, 0, 0, 0);
            ruckusHardware.setDcMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public void driveEncoded(double linearX, double linearY, double angularZ, double time) {
        //Needed numbers
        double WHEEL_DIAMETER = 100;
        double WHEEL_SEPERATION_WIDTH = 384;
        double WHEEL_SEPARATION_LENGTH = 336;
        double GEAR_RATIO = 1.6;
        double COUNTS_PER_REV = 1478.4;
        double WHEEL_MAX_RPM = 125;

        double avwB = angularZ / 180 * Math.PI / time;


        double avwFL = (1 / (WHEEL_DIAMETER / 2)) * (linearX / time - linearY / time - (WHEEL_SEPERATION_WIDTH + WHEEL_SEPARATION_LENGTH) / 2 * avwB);
        double avwFR = (1 / (WHEEL_DIAMETER / 2)) * (linearX / time + linearY / time + (WHEEL_SEPERATION_WIDTH + WHEEL_SEPARATION_LENGTH) / 2 * avwB);
        double avwRL = (1 / (WHEEL_DIAMETER / 2)) * (linearX / time + linearY / time - (WHEEL_SEPERATION_WIDTH + WHEEL_SEPARATION_LENGTH) / 2 * avwB);
        double avwRR = (1 / (WHEEL_DIAMETER / 2)) * (linearX / time - linearY / time + (WHEEL_SEPERATION_WIDTH + WHEEL_SEPARATION_LENGTH) / 2 * avwB);

        double rpmFL = (avwFL * 30 / Math.PI) / GEAR_RATIO;
        double rpmFR = (avwFR * 30 / Math.PI) / GEAR_RATIO;
        double rpmRL = (avwRL * 30 / Math.PI) / GEAR_RATIO;
        double rpmRR = (avwRR * 30 / Math.PI) / GEAR_RATIO;

        Double ticksFLD = (rpmFL / 60 * COUNTS_PER_REV * time);
        Double ticksFRD = (rpmFR / 60 * COUNTS_PER_REV * time);
        Double ticksRLD = (rpmRL / 60 * COUNTS_PER_REV * time);
        Double ticksRRD = (rpmRR / 60 * COUNTS_PER_REV * time);

        int ticksFL = ticksFLD.intValue();
        int ticksFR = ticksFRD.intValue();
        int ticksRL = ticksRLD.intValue();
        int ticksRR = ticksRRD.intValue();

        ruckusHardware.setDcMotorMode(DcMotor.RunMode.RUN_TO_POSITION);

        ruckusHardware.addWheelTicks(ticksFL, ticksFR, ticksRL, ticksRR);

        ruckusHardware.setMotorPowers(rpmFL / WHEEL_MAX_RPM, rpmFR / WHEEL_MAX_RPM, rpmRL / WHEEL_MAX_RPM, rpmRR / WHEEL_MAX_RPM);
    }

    public void stopEncodedDrive() {
        ruckusHardware.setNewWheelTargets(ruckusHardware.lfWheel.getCurrentPosition(), ruckusHardware.rfWheel.getCurrentPosition(),
                ruckusHardware.lrWheel.getCurrentPosition(), ruckusHardware.rrWheel.getCurrentPosition());
    }
}
