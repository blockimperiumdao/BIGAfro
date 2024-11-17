package org.firstinspires.ftc.teamcode.components.imu;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.components.AbstractComponent;
import org.firstinspires.ftc.teamcode.components.ComponentType;
import org.firstinspires.ftc.teamcode.drivers.GoBildaPinpointDriver;

import java.util.Locale;

public class GoBildaPinpointComponent extends AbstractComponent {
    // Hardware reference
    private GoBildaPinpointDriver pinpoint;



    // Configuration constants - these could be made configurable
//    private static final double DEFAULT_X_OFFSET = -84.0;  // mm
//    private static final double DEFAULT_Y_OFFSET = -168.0; // mm

    // https://www.gobilda.com/content/user_manuals/3110-0002-0001%20User%20Guide.pdf
    // the X/forward pod is -4 inches (-101.6mm) off center (to the right of center which makes it negative)
    // the Y pod offset is -6 3/4 inches (-171.45)off the center point (below the center)
    private static final double DEFAULT_X_OFFSET = -101.6;  // mm
    private static final double DEFAULT_Y_OFFSET = -171.45; // mm


    // State tracking
    private long lastUpdateTime = 0;
    private double updateFrequency = 0;

    @Override
    public String getName() {
        return "GoBildaPinpoint";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.SENSOR;
    }

    @Override
    protected void initializeComponent() throws Exception {
        try {
            // Get hardware reference
            pinpoint = robot.getHardwareMap().get(GoBildaPinpointDriver.class, "odo");

            // Configure default settings
            pinpoint.setOffsets(DEFAULT_X_OFFSET, DEFAULT_Y_OFFSET);

            //TODO, this needs to be configurable
            pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);

            // x increases forward
            // y increases to the right
            pinpoint.setEncoderDirections(
                    GoBildaPinpointDriver.EncoderDirection.FORWARD,
                    GoBildaPinpointDriver.EncoderDirection.REVERSED
            );

            // Reset position and calibrate
            resetPosAndIMU();

            // recalibrate for starting state
            recalibrateIMU();

            Pose2D initialPosition = new Pose2D(DistanceUnit.MM, 0, 0, AngleUnit.DEGREES, 0);
            pinpoint.setPosition( initialPosition );

            // Log initialization info
            telemetryManager.info("GoBilda Pinpoint initialized successfully");
            telemetryManager.info(String.format("Device Version: %s", pinpoint.getDeviceVersion()));
            telemetryManager.info(String.format("Yaw Scalar: %f", pinpoint.getYawScalar()));

        } catch (Exception e) {
            telemetryManager.error("Failed to initialize GoBilda Pinpoint: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update() {
        if (!isOperational()) {
            return;
        }

        try {
            long currentTime = System.currentTimeMillis();

            // Pull the data from the odometry computer
            pinpoint.update();

            // Calculate update frequency
            if (lastUpdateTime != 0) {
                double deltaTime = (currentTime - lastUpdateTime) / 1000.0;
                updateFrequency = 1.0 / deltaTime;
            }
            lastUpdateTime = currentTime;

            // Get current position and velocity
            Pose2D position = pinpoint.getPosition();
            Pose2D velocity = pinpoint.getVelocity();

            // Update telemetry data
            telemetryData.clear();
//            telemetryData.put("Status", pinpoint.getDeviceStatus().toString());
            telemetryData.put("Position", String.format(Locale.US,
                    "X: %.2f, Y: %.2f, H: %.2f",
                    position.getX(DistanceUnit.MM),
                    position.getY(DistanceUnit.MM),
                    position.getHeading(AngleUnit.DEGREES)));
//            telemetryData.put("Velocity", String.format(Locale.US,
//                    "X: %.2f, Y: %.2f, H: %.2f",
//                    velocity.getX(DistanceUnit.MM),
//                    velocity.getY(DistanceUnit.MM),
//                    velocity.getHeading(AngleUnit.DEGREES)));
            telemetryData.put("Update Frequency", String.format("%.1f Hz", updateFrequency));
            telemetryData.put("Device Frequency", String.format("%.1f Hz", pinpoint.getFrequency()));

        } catch (Exception e) {
            telemetryManager.error("Pinpoint update failed: " + e.getMessage());
            emergencyStop("Pinpoint failure");
        }
    }

    @Override
    public void stop() {
        // Nothing special needed for stopping
    }

    // Configuration methods
    public void setOffsets(double xOffset, double yOffset) {
        if (!isOperational()) return;
        try {
            pinpoint.setOffsets(xOffset, yOffset);
            telemetryManager.info(String.format("Offsets updated - X: %.2f, Y: %.2f", xOffset, yOffset));
        } catch (Exception e) {
            telemetryManager.error("Failed to set offsets: " + e.getMessage());
        }
    }

    public void setEncoderType(GoBildaPinpointDriver.GoBildaOdometryPods podType) {
        if (!isOperational()) return;
        try {
            pinpoint.setEncoderResolution(podType);
            telemetryManager.info("Encoder type updated to: " + podType);
        } catch (Exception e) {
            telemetryManager.error("Failed to set encoder type: " + e.getMessage());
        }
    }

    public void setCustomEncoderResolution(double ticksPerMM) {
        if (!isOperational()) return;
        try {
            pinpoint.setEncoderResolution(ticksPerMM);
            telemetryManager.info(String.format("Custom encoder resolution set: %.4f ticks/mm", ticksPerMM));
        } catch (Exception e) {
            telemetryManager.error("Failed to set custom encoder resolution: " + e.getMessage());
        }
    }

    public void setEncoderDirections(
            GoBildaPinpointDriver.EncoderDirection xDirection,
            GoBildaPinpointDriver.EncoderDirection yDirection) {
        if (!isOperational()) return;
        try {
            pinpoint.setEncoderDirections(xDirection, yDirection);
            telemetryManager.info(String.format("Encoder directions set - X: %s, Y: %s",
                    xDirection, yDirection));
        } catch (Exception e) {
            telemetryManager.error("Failed to set encoder directions: " + e.getMessage());
        }
    }

    // Calibration methods
    public void recalibrateIMU() {
        if (!isOperational()) return;
        try {
            pinpoint.recalibrateIMU();
            telemetryManager.info("IMU recalibration initiated");
        } catch (Exception e) {
            telemetryManager.error("IMU recalibration failed: " + e.getMessage());
        }
    }

    public void resetPosAndIMU() {
        if (!isOperational()) return;
        try {
            pinpoint.resetPosAndIMU();
            telemetryManager.info("Position reset and IMU recalibrated");
        } catch (Exception e) {
            telemetryManager.error("Position/IMU reset failed: " + e.getMessage());
        }
    }

    // Data access methods
    public Pose2D getPosition() {
        if (!isOperational()) {
            return new Pose2D(DistanceUnit.MM, 0, 0, AngleUnit.DEGREES, 0);
        }
        try {
            return pinpoint.getPosition();
        } catch (Exception e) {
            telemetryManager.error("Failed to get position: " + e.getMessage());
            return new Pose2D(DistanceUnit.MM, 0, 0, AngleUnit.DEGREES, 0);
        }
    }

    public Pose2D getVelocity() {
        if (!isOperational()) {
            return new Pose2D(DistanceUnit.MM, 0, 0, AngleUnit.DEGREES, 0);
        }
        try {
            return pinpoint.getVelocity();
        } catch (Exception e) {
            telemetryManager.error("Failed to get velocity: " + e.getMessage());
            return new Pose2D(DistanceUnit.MM, 0, 0, AngleUnit.DEGREES, 0);
        }
    }

    public double getHeading() {
        return isOperational() ?
                pinpoint.getPosition().getHeading(AngleUnit.DEGREES) : 0.0;
    }

    public double getUpdateFrequency() {
        return updateFrequency;
    }

    public GoBildaPinpointDriver.DeviceStatus getDeviceStatus() {
        return isOperational() ? pinpoint.getDeviceStatus() : null;
    }
}