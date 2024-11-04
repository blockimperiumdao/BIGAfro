package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class DriveTrain extends AbstractComponent {
    // Constants for safety limits
    private static final double MAX_POWER = 1.0;
    private static final double MIN_POWER = -1.0;
    private static final double POWER_CHANGE_LIMIT = 0.5; // Max power change per cycle
    private static final double MINIMUM_MOVEMENT_THRESHOLD = 0.05; // Dead zone for inputs

    // Hardware components
    private DcMotor mtr_frontLeft, mtr_frontRight, mtr_backLeft, mtr_backRight;

    // State tracking
    private Map<String, Double> lastMotorPowers;
    private long lastUpdateTime = 0;

    @Override
    public String getName() {
        return "DriveTrain";
    }

    public ComponentType getType()
    {
        return ComponentType.DRIVE;
    }

    @Override
    protected void initializeComponent() throws Exception {
        this.lastMotorPowers = new HashMap<>();
        HardwareMap hardwareMap = robot.getHardwareMap();

        try {
            mtr_frontLeft = hardwareMap.get(DcMotor.class, "motor_front_left");
            mtr_frontRight = hardwareMap.get(DcMotor.class, "motor_front_right");
            mtr_backLeft = hardwareMap.get(DcMotor.class, "motor_back_left");
            mtr_backRight = hardwareMap.get(DcMotor.class, "motor_back_right");

            // Verify all motors were found
            if (mtr_frontLeft == null || mtr_frontRight == null ||
                    mtr_backLeft == null || mtr_backRight == null) {
                throw new RuntimeException("One or more motors not found in hardware map");
            }

            // Initialize each motor
            initializeMotor(mtr_frontLeft, "Front Left");
            initializeMotor(mtr_frontRight, "Front Right");
            initializeMotor(mtr_backLeft, "Back Left");
            initializeMotor(mtr_backRight, "Back Right");

            // Set motor directions
            mtr_frontLeft.setDirection(DcMotor.Direction.REVERSE);
            mtr_backLeft.setDirection(DcMotor.Direction.REVERSE);
            mtr_frontRight.setDirection(DcMotor.Direction.FORWARD);
            mtr_backRight.setDirection(DcMotor.Direction.FORWARD);

            // Initialize last power values
            lastMotorPowers.put("frontLeft", 0.0);
            lastMotorPowers.put("frontRight", 0.0);
            lastMotorPowers.put("backLeft", 0.0);
            lastMotorPowers.put("backRight", 0.0);

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            telemetryManager.error("Stack trace: " + sw.toString());
            throw new RuntimeException("Motor initialization failed: " + e.getMessage());
        }
    }

    private void initializeMotor(DcMotor motor, String name) {
        if (motor == null) {
            throw new RuntimeException(name + " motor not found in hardware map");
        }

        try {
            // Reset encoder to ensure clean starting state
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            // For general driving, RUN_USING_ENCODER provides velocity PID control
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            // Use FLOAT for smoother mecanum drive operation
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setPower(0);

            // Verify encoder is connected and functioning
            if (!motor.isBusy() && motor.getCurrentPosition() == 0) {
                telemetryManager.info(name + " encoder initialized successfully");
            } else {
                telemetryManager.warning(name + " encoder may not be connected or functioning properly");
            }
        } catch (Exception e) {
            telemetryManager.error("Failed to initialize " + name + " encoder: " + e.getMessage());
            // Still allow motor to run, but warn about reduced functionality
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            telemetryManager.warning(name + " falling back to RUN_WITHOUT_ENCODER mode");
        }
    }

    @Override
    public void update() {
        if (!isOperational()) return;
        reportMotorPowers("update");
    }

    private void setMotorPower(DcMotor motor, double power, String motorName) {
        if (!isOperational()) {
            telemetryManager.error("Drive train not properly initialized!");
            return;
        }

        try {
            // Apply dead zone
            if (Math.abs(power) < MINIMUM_MOVEMENT_THRESHOLD) {
                power = 0.0;
            }

            // Bound the power
            power = Math.max(MIN_POWER, Math.min(MAX_POWER, power));

            // Get the last power value and time
            double lastPower = lastMotorPowers.getOrDefault(motorName, 0.0);
            long currentTime = System.currentTimeMillis();
            double timeElapsed = (currentTime - lastUpdateTime) / 1000.0;

            // Limit acceleration if enough time has passed
            if (lastUpdateTime != 0 && timeElapsed > 0) {
                double maxChange = POWER_CHANGE_LIMIT * timeElapsed;
                double powerChange = power - lastPower;
                if (Math.abs(powerChange) > maxChange) {
                    power = lastPower + Math.signum(powerChange) * maxChange;
                }
            }

            // Set the power and update tracking
            motor.setPower(power);
            lastMotorPowers.put(motorName, power);
            lastUpdateTime = currentTime;

        } catch (Exception e) {
            telemetryManager.error(String.format("Failed to set %s motor power: %s",
                    motorName, e.getMessage()));
            emergencyStop(String.format("Motor %s failure", motorName));
        }
    }

    public void setTargetPosition(String motorName, int position, double power) {
        if (!isOperational()) return;

        DcMotor motor = null;
        switch(motorName.toLowerCase()) {
            case "frontleft":
                motor = mtr_frontLeft;
                break;
            case "frontright":
                motor = mtr_frontRight;
                break;
            case "backleft":
                motor = mtr_backLeft;
                break;
            case "backright":
                motor = mtr_backRight;
                break;
            default:
                telemetryManager.error("Invalid motor name: " + motorName);
                return;
        }

        try {
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motor.setTargetPosition(position);
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motor.setPower(Math.abs(power));

            telemetryManager.addToBatch(motorName + " Target", position);
            telemetryManager.addToBatch(motorName + " Current", motor.getCurrentPosition());
        } catch (Exception e) {
            telemetryManager.error("Failed to set target position for " + motorName + ": " + e.getMessage());
        }
    }

    public void moveForward(double power) {
        if (!isOperational()) {
            telemetryManager.error("Cannot move: Drive train not initialized");
            return;
        }

        telemetryManager.addToBatch("Drive Status", "Moving forward");

        setMotorPower(mtr_frontLeft, power, "frontLeft");
        setMotorPower(mtr_backLeft, power, "backLeft");
        setMotorPower(mtr_frontRight, power, "frontRight");
        setMotorPower(mtr_backRight, power, "backRight");

        reportMotorPowers("forward");
    }

    public void moveBackward(double power) {
        moveForward(-power);
    }

    public void turnRight(double power) {
        if (!isOperational()) return;

        telemetryManager.addToBatch("Drive Status", "Turning right");

        setMotorPower(mtr_frontLeft, power, "frontLeft");
        setMotorPower(mtr_backLeft, power, "backLeft");
        setMotorPower(mtr_frontRight, -power, "frontRight");
        setMotorPower(mtr_backRight, -power, "backRight");

        reportMotorPowers("turn right");
    }

    public void turnLeft(double power) {
        turnRight(-power);
    }

    public void driveWithGamepad(Gamepad gamepad) {
        if (!isOperational()) {
            telemetryManager.error("Drive system not initialized!");
            return;
        }

        try {
            double drive = -boundInput(gamepad.left_stick_y);
            double strafe = -boundInput(gamepad.left_stick_x);
            double rotate = boundInput(gamepad.right_stick_x);

            double[] powers = calculateWheelPowers(drive, strafe, rotate);

            setMotorPower(mtr_frontLeft, powers[0], "frontLeft");
            setMotorPower(mtr_backLeft, powers[1], "backLeft");
            setMotorPower(mtr_frontRight, powers[2], "frontRight");
            setMotorPower(mtr_backRight, powers[3], "backRight");

            telemetryData.put("Drive Input", drive);
            telemetryData.put("Strafe Input", strafe);
            telemetryData.put("Rotate Input", rotate);

            reportMotorPowers("gamepad");

        } catch (Exception e) {
            telemetryManager.error("Drive control error: " + e.getMessage());
            emergencyStop("Gamepad control failure");
        }
    }

    private double boundInput(double input) {
        if (Math.abs(input) < MINIMUM_MOVEMENT_THRESHOLD) {
            return 0.0;
        }
        return Math.max(MIN_POWER, Math.min(MAX_POWER, input));
    }

    private double[] calculateWheelPowers(double drive, double strafe, double rotate) {
        double[] powers = new double[4];
        powers[0] = drive + strafe + rotate;
        powers[1] = drive - strafe + rotate;
        powers[2] = drive - strafe - rotate;
        powers[3] = drive + strafe - rotate;

        double maxMagnitude = 0;
        for (double power : powers) {
            maxMagnitude = Math.max(maxMagnitude, Math.abs(power));
        }

        if (maxMagnitude > 1.0) {
            for (int i = 0; i < powers.length; i++) {
                powers[i] /= maxMagnitude;
            }
        }

        return powers;
    }

    private void reportMotorPowers(String context) {
        telemetryData.put("Front Left Power", String.format("%.2f", mtr_frontLeft.getPower()));
        telemetryData.put("Back Left Power", String.format("%.2f", mtr_backLeft.getPower()));
        telemetryData.put("Front Right Power", String.format("%.2f", mtr_frontRight.getPower()));
        telemetryData.put("Back Right Power", String.format("%.2f", mtr_backRight.getPower()));

        // Add encoder positions
        telemetryData.put("Front Left Position", mtr_frontLeft.getCurrentPosition());
        telemetryData.put("Back Left Position", mtr_backLeft.getCurrentPosition());
        telemetryData.put("Front Right Position", mtr_frontRight.getCurrentPosition());
        telemetryData.put("Back Right Position", mtr_backRight.getCurrentPosition());
    }

    @Override
    public void stop() {
        if (!isOperational()) return;

        telemetryManager.addToBatch("Drive Status", "Stopping Motors");

        setMotorPower(mtr_frontLeft, 0, "frontLeft");
        setMotorPower(mtr_backLeft, 0, "backLeft");
        setMotorPower(mtr_frontRight, 0, "frontRight");
        setMotorPower(mtr_backRight, 0, "backRight");

        reportMotorPowers("stop");
    }

    public void enableBrakeMode(boolean enable) {
        if (!isOperational()) return;

        DcMotor.ZeroPowerBehavior behavior = enable ?
                DcMotor.ZeroPowerBehavior.BRAKE :
                DcMotor.ZeroPowerBehavior.FLOAT;

        try {
            mtr_frontLeft.setZeroPowerBehavior(behavior);
            mtr_frontRight.setZeroPowerBehavior(behavior);
            mtr_backLeft.setZeroPowerBehavior(behavior);
            mtr_backRight.setZeroPowerBehavior(behavior);

            telemetryManager.info("Zero power behavior set to: " + behavior);
        } catch (Exception e) {
            telemetryManager.error("Failed to set zero power behavior: " + e.getMessage());
        }
    }

}