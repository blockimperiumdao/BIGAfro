package org.firstinspires.ftc.teamcode.opmodes.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Afrobot;
import org.firstinspires.ftc.teamcode.TelemetryManager;
import org.firstinspires.ftc.teamcode.systems.navigation.SULUNavigationSystem;

@Autonomous(name="SuluNavigationTest", group="Autonomous")
public class SuluNavigationTest extends LinearOpMode {

    private Afrobot robot;
    private TelemetryManager telemetryManager;

    @Override
    public void runOpMode() {
        // Initialize the robot hardware
        robot = new Afrobot(hardwareMap, telemetry, gamepad1, gamepad2);

        // Wait for start command
        waitForStart();

        telemetryManager = robot.getTelemetryManager();

        // get sulu
        SULUNavigationSystem sulu = (SULUNavigationSystem) robot.getSystem(SULUNavigationSystem.SYSTEM_NAME);


        // go to a particular location
        sulu.setCourse( 0.0, 100.0, -20.0 );

        telemetryManager.info("Course set");

        while ( !sulu.isComplete || !isStopRequested() )
        {
            robot.loop();
        }

        sleep( 5000 );

        sulu.setCourse( 0.0, 0.0, 0.0 );

        while ( !sulu.isComplete || !isStopRequested() )
        {
            robot.loop();
        }

    }
}