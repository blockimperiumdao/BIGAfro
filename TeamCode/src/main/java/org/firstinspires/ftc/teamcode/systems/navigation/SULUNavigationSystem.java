package org.firstinspires.ftc.teamcode.systems.navigation;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.components.ComponentType;
import org.firstinspires.ftc.teamcode.components.imu.GoBildaPinpointComponent;
import org.firstinspires.ftc.teamcode.components.motion.DriveTrain;
import org.firstinspires.ftc.teamcode.systems.AbstractSystem;

import java.util.Collections;
import java.util.Map;

public class SULUNavigationSystem extends AbstractSystem {

    public static String DRIVE_TRAIN_PARAMETER = "driveTrain";
    public static String IMU_SYSTEM_PARAMETER = "imuSystem";

    private final double POSITION_THRESHOLD = 0.1; // Stop when close enough
    private final double HEADING_THRESHOLD = 1.0; // Stop when aligned
    private final double ROTATION_GAIN = 0.01; // Adjust rotation sensitivity
    private final double DRIVE_GAIN = 0.05; // Adjust drive sensitivity

    private DriveTrain driveTrain;
    private GoBildaPinpointComponent imuComponent;

    // Initial and target parameters
    double currentX = 0.0, currentY = 0.0, currentHeading = 0.0; // Starting position and heading
    double targetX = 10.0, targetY = 10.0, targetHeading = 90.0; // Target position and heading

    @Override
    public void init(Map<String,Object> parameters)
    {
        this.driveTrain = (DriveTrain) parameters.get( DRIVE_TRAIN_PARAMETER );
        this.imuComponent = (GoBildaPinpointComponent) parameters.get( IMU_SYSTEM_PARAMETER );
    }

    public void setCourse( double target_x, double target_y, double target_heading )
    {
        this.targetX = target_x;
        this.targetY = target_y;
        this.targetHeading = target_heading;
    }

    @Override
    public void update() {
        if (!isComplete)
        {
            currentX = imuComponent.getPosition().getX(DistanceUnit.MM);
            currentY = imuComponent.getPosition().getY(DistanceUnit.MM);
            currentHeading = imuComponent.getHeading();

            // Calculate distance to target
            double deltaX = targetX - currentX;
            double deltaY = targetY - currentY;
            double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            // Calculate target angle and heading error
            double targetAngle = Math.toDegrees(Math.atan2(deltaY, deltaX));
            double headingError = normalizeAngle(targetAngle - currentHeading);

            // If within position and heading thresholds, stop
            if (distanceToTarget <= POSITION_THRESHOLD &&
                    Math.abs(normalizeAngle(targetHeading - currentHeading)) <= HEADING_THRESHOLD) {
                driveTrain.stop();
                return;
            }

            // Proportional control for movement
            double drivePower = DRIVE_GAIN * distanceToTarget; // Move forward
            double rotationPower = ROTATION_GAIN * headingError; // Rotate to face the target

            // Instruct the drivetrain
            driveTrain.driveWithPower(drivePower, 0, rotationPower);
        }
    }

    @Override
    public String getName() {
        return "Sulu";
    }

    @Override
    public void stop() {
        isComplete = true;
    }

    @Override
    public Map<String, Object> getTelemetry() {
        return Collections.emptyMap();
    }

    // Normalize angle to range [-180, 180]
    public static double normalizeAngle(double angle) {
        angle = (angle + 180) % 360 - 180;
        return angle < -180 ? angle + 360 : angle;
    }

}
