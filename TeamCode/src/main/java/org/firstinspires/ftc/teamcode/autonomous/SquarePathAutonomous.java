package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Afrobot;

@Autonomous(name="SquarePathAutonomous", group="Autonomous")
public class SquarePathAutonomous extends LinearOpMode {
    private Afrobot robot;

    @Override
    public void runOpMode() {
        // Initialize the robot hardware
        robot = new Afrobot(hardwareMap, telemetry, gamepad1, gamepad2);

        // Wait for start command
        waitForStart();

        // Repeat the square path 4 times
        for (int i = 0; i < 4; i++) {
            // Move forward for 5 seconds
            robot.getDriveTrain().moveForward(0.5);  // Set power to 0.5
            sleep(5000);  // Move forward for 5 seconds

            // Stop briefly before turning
            robot.getDriveTrain().stop();
            sleep(500);   // Pause for 0.5 seconds

            // Turn 90 degrees to the right
            robot.getDriveTrain().turnRight(0.5);  // Set turn power to 0.5
            sleep(1000);  // Adjust timing based on your robot's turn speed

            // Stop briefly after turning
            robot.getDriveTrain().stop();
            sleep(500);   // Pause for 0.5 seconds
        }
    }
}