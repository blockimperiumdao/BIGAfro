package org.firstinspires.ftc.teamcode.teleop;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Afrobot;

import java.util.HashMap;
import java.util.Map;

@TeleOp(name="BasicMecanumDrive", group="TeleOp")
public class BasicMecanumDrive extends OpMode {
    private Afrobot robot;

    @Override
    public void init() {
        // Initialize the robot hardware
        robot = new Afrobot(hardwareMap, telemetry, gamepad1, gamepad2);
    }

    @Override
    public void start()
    {
        robot.start();
    }

    @Override
    public void loop() {
        // move control of the loop over to the robot class
        robot.loop();
    }

    @Override
    public void stop()
    {
        robot.stop();
    }
}