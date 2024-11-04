package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

public class IMUSensor extends AbstractComponent {
    // Hardware components
    private IMU imu;
    private RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
    private RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

    // Constants and state tracking
    private static final double INVALID_READING = Double.NaN;
    private double lastYaw = 0.0;
    private double lastPitch = 0.0;
    private double lastRoll = 0.0;
    private long lastReadingTime = 0;

    @Override
    public String getName() {
        return "IMU";
    }

    public ComponentType getType()
    {
        return ComponentType.SENSOR;
    }

    @Override
    protected void initializeComponent() throws Exception {
        // Get IMU from hardware map
        imu = robot.getHardwareMap().get(IMU.class, "imu");
        if (imu == null) {
            throw new RuntimeException("IMU not found in hardware map");
        }

        // Create orientation parameters
        RevHubOrientationOnRobot orientationOnRobot =
                new RevHubOrientationOnRobot(logoDirection, usbDirection);

        IMU.Parameters parameters = new IMU.Parameters(orientationOnRobot);

        // Initialize IMU with parameters
        boolean initialized = imu.initialize(parameters);
        if (!initialized) {
            throw new RuntimeException("IMU failed to initialize");
        }

        // Reset IMU
        imu.resetYaw();
    }

    @Override
    public void update() {
        if (!isOperational()) {
            setErrorReadings();
            return;
        }

        try {
            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
            AngularVelocity angularVelocity = imu.getRobotAngularVelocity(AngleUnit.DEGREES);

            if (orientation == null || angularVelocity == null) {
                throw new RuntimeException("Failed to get IMU readings");
            }

            // Get current time for rate calculations
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastReadingTime) / 1000.0; // Convert to seconds

            // Store orientation data
            double currentYaw = orientation.getYaw(AngleUnit.DEGREES);
            double currentPitch = orientation.getPitch(AngleUnit.DEGREES);
            double currentRoll = orientation.getRoll(AngleUnit.DEGREES);

            // Update telemetry data
            telemetryData.clear();
            telemetryData.put("Yaw", currentYaw);
            telemetryData.put("Pitch", currentPitch);
            telemetryData.put("Roll", currentRoll);

            // Calculate rate of change if we have valid previous readings
            if (lastReadingTime != 0 && deltaTime > 0) {
                telemetryData.put("Yaw_Rate", (currentYaw - lastYaw) / deltaTime);
                telemetryData.put("Pitch_Rate", (currentPitch - lastPitch) / deltaTime);
                telemetryData.put("Roll_Rate", (currentRoll - lastRoll) / deltaTime);
            }

            // Store angular velocity
            telemetryData.put("Angular_Velocity_X", angularVelocity.xRotationRate);
            telemetryData.put("Angular_Velocity_Y", angularVelocity.yRotationRate);
            telemetryData.put("Angular_Velocity_Z", angularVelocity.zRotationRate);

            // Update cached values
            lastYaw = currentYaw;
            lastPitch = currentPitch;
            lastRoll = currentRoll;
            lastReadingTime = currentTime;

        } catch (Exception e) {
            telemetryManager.error("IMU reading failed: " + e.getMessage());
            setErrorReadings();
        }
    }

    @Override
    public void stop() {
        // Nothing to stop for IMU
    }

    private void setErrorReadings() {
        telemetryData.clear();
        telemetryData.put("Yaw", INVALID_READING);
        telemetryData.put("Pitch", INVALID_READING);
        telemetryData.put("Roll", INVALID_READING);
        telemetryData.put("Yaw_Rate", INVALID_READING);
        telemetryData.put("Pitch_Rate", INVALID_READING);
        telemetryData.put("Roll_Rate", INVALID_READING);
        telemetryData.put("Angular_Velocity_X", INVALID_READING);
        telemetryData.put("Angular_Velocity_Y", INVALID_READING);
        telemetryData.put("Angular_Velocity_Z", INVALID_READING);
    }

    // Utility methods
    public double getYaw() {
        try {
            return isOperational() ?
                    imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES) :
                    INVALID_READING;
        } catch (Exception e) {
            telemetryManager.error("Failed to get yaw: " + e.getMessage());
            return INVALID_READING;
        }
    }

    public double getPitch() {
        try {
            return isOperational() ?
                    imu.getRobotYawPitchRollAngles().getPitch(AngleUnit.DEGREES) :
                    INVALID_READING;
        } catch (Exception e) {
            telemetryManager.error("Failed to get pitch: " + e.getMessage());
            return INVALID_READING;
        }
    }

    public double getRoll() {
        try {
            return isOperational() ?
                    imu.getRobotYawPitchRollAngles().getRoll(AngleUnit.DEGREES) :
                    INVALID_READING;
        } catch (Exception e) {
            telemetryManager.error("Failed to get roll: " + e.getMessage());
            return INVALID_READING;
        }
    }

    public double getAngularVelocity() {
        if (!isOperational()) return INVALID_READING;
        try {
            AngularVelocity velocity = imu.getRobotAngularVelocity(AngleUnit.DEGREES);
            return velocity != null ? velocity.zRotationRate : INVALID_READING;
        } catch (Exception e) {
            telemetryManager.error("Failed to get angular velocity: " + e.getMessage());
            return INVALID_READING;
        }
    }

    // Configuration methods
    public void setOrientation(RevHubOrientationOnRobot.LogoFacingDirection logo,
                               RevHubOrientationOnRobot.UsbFacingDirection usb) {
        this.logoDirection = logo;
        this.usbDirection = usb;
        // Re-initialize with new orientation if already operational
        if (isOperational()) {
            try {
                initializeComponent();
            } catch (Exception e) {
                telemetryManager.error("Failed to reinitialize IMU with new orientation: " + e.getMessage());
            }
        }
    }

    public void resetYaw() {
        if (isOperational()) {
            try {
                imu.resetYaw();
                telemetryManager.info("IMU yaw reset successful");
            } catch (Exception e) {
                telemetryManager.error("Failed to reset IMU yaw: " + e.getMessage());
            }
        }
    }
}