package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actions.ActionInterface;
import org.firstinspires.ftc.teamcode.actions.ActionState;
import org.firstinspires.ftc.teamcode.components.Component;
import org.firstinspires.ftc.teamcode.components.motion.DriveTrain;
import org.firstinspires.ftc.teamcode.components.imu.GoBildaPinpointComponent;
import org.firstinspires.ftc.teamcode.components.imu.IMUSensor;

import java.util.HashMap;
import java.util.Map;

public class Afrobot {
    // Component management
    private Map<String, Component> components;
    private Map<String, ActionInterface> actionMap;

    // Core robot systems
    private TelemetryManager telemetryManager;
    private HardwareMap hardwareMap;
    private Gamepad gamepad1;
    private Gamepad gamepad2;

    // Frequently accessed components
    private DriveTrain driveTrain;
    private IMUSensor imuSensor;
    private GoBildaPinpointComponent goBildaPinpointComponent;

    private boolean isInitialized = false;

    public Afrobot(HardwareMap hardwareMap, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2) {
        this.components = new HashMap<>();
        this.actionMap = new HashMap<>();
        this.telemetryManager = new TelemetryManager(telemetry);
        this.hardwareMap = hardwareMap;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;

        try {
            initializeComponents();
            isInitialized = true;
            telemetryManager.info("Robot initialized successfully");
        } catch (Exception e) {
            telemetryManager.error("Robot initialization failed: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        // Initialize drive train
        try {
            driveTrain = new DriveTrain();
            registerComponent(driveTrain);
        } catch (Exception e) {
            telemetryManager.error("Failed to initialize drive train: " + e.getMessage());
            throw e; // Re-throw as this is a critical component
        }

//        // Initialize IMU
//        try {
//            imuSensor = new IMUSensor();
//            registerComponent(imuSensor);
//        } catch (Exception e) {
//            telemetryManager.warning("IMU initialization failed - some features may be limited: " + e.getMessage());
//        }


        try {
            goBildaPinpointComponent = new GoBildaPinpointComponent();
            registerComponent(goBildaPinpointComponent);
        }
        catch ( Exception e )
        {
            telemetryManager.warning("GoBildaPinpoint initialization failed - direction features will be limited: " + e.getMessage() );
        }
    }

    public void registerComponent(Component component) {
        try {
            component.init(this);
            components.put(component.getName(), component);
            telemetryManager.info("Registered component: " + component.getName());
        } catch (Exception e) {
            telemetryManager.error("Failed to register component " +
                    component.getName() + ": " + e.getMessage());
            throw new RuntimeException("Component registration failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(String name) {
        return (T) components.get(name);
    }

    public void bindAction(String button, ActionInterface action) {
        try {
            if (button == null || button.trim().isEmpty()) {
                throw new IllegalArgumentException("Button identifier cannot be null or empty");
            }
            if (action == null) {
                throw new IllegalArgumentException("Action cannot be null");
            }

            actionMap.put(button, action);
            telemetryManager.info("Action bound to button: " + button);
        } catch (Exception e) {
            telemetryManager.error("Failed to bind action: " + e.getMessage());
        }
    }

    public void executeActions(Gamepad gamepad1, Gamepad gamepad2) {
        executeButtonAction("gamepad1_a", gamepad1.a);
        executeButtonAction("gamepad1_b", gamepad1.b);
        executeButtonAction("gamepad1_x", gamepad1.x);
        executeButtonAction("gamepad1_y", gamepad1.y);
        executeButtonAction("gamepad1_left_bumper", gamepad1.left_bumper);
        executeButtonAction("gamepad1_right_bumper", gamepad1.right_bumper);
        executeButtonAction("gamepad1_dpad_up", gamepad1.dpad_up);
        executeButtonAction("gamepad1_dpad_down", gamepad1.dpad_down);
        executeButtonAction("gamepad1_dpad_left", gamepad1.dpad_left);
        executeButtonAction("gamepad1_dpad_right", gamepad1.dpad_right);

        executeButtonAction("gamepad2_a", gamepad2.a);
        executeButtonAction("gamepad2_b", gamepad2.b);
        executeButtonAction("gamepad2_x", gamepad2.x);
        executeButtonAction("gamepad2_y", gamepad2.y);
        executeButtonAction("gamepad2_left_bumper", gamepad2.left_bumper);
        executeButtonAction("gamepad2_right_bumper", gamepad2.right_bumper);
        executeButtonAction("gamepad2_dpad_up", gamepad2.dpad_up);
        executeButtonAction("gamepad2_dpad_down", gamepad2.dpad_down);
        executeButtonAction("gamepad2_dpad_left", gamepad2.dpad_left);
        executeButtonAction("gamepad2_dpad_right", gamepad2.dpad_right);
    }

    private void executeButtonAction(String button, boolean isPressed) {
        ActionInterface action = actionMap.get(button);
        if (action == null || !action.isOperational()) return;

        try {
            switch (action.getType()) {
                case CONTINUOUS:
                    if (isPressed && action.getState() == ActionState.READY) {
                        // Start continuous action on initial press
                        action.start();
                        telemetryManager.addToBatch("Executed Action",
                                String.format("Started Continuous Action: %s (%s)",
                                        action.getName(), button));
                    }
                    else if (action.getState() == ActionState.RUNNING) {
                        // Continue running until complete
                        action.update();
                        telemetryManager.addToBatch("Action Status",
                                String.format("%s: Running", action.getName()));
                    }
                    break;

                case MOMENTARY:
                    if (isPressed) {
                        if (action.getState() == ActionState.READY) {
                            // Start momentary action when button first pressed
                            action.start();
                            telemetryManager.addToBatch("Executed Action",
                                    String.format("Started Momentary Action: %s (%s)",
                                            action.getName(), button));
                        }
                        else if (action.getState() == ActionState.RUNNING) {
                            // Continue while button held
                            action.update();
                        }
                    }
                    else if (action.getState() == ActionState.RUNNING) {
                        // Stop when button released
                        action.end();
                        telemetryManager.addToBatch("Action Status",
                                String.format("%s: Ended", action.getName()));
                    }
                    break;
            }
        } catch (Exception e) {
            telemetryManager.error(String.format("Action execution failed (%s): %s",
                    action.getName(), e.getMessage()));

            // Log the error that was captured by the action
            if (action.getLastError() != null) {
                telemetryManager.error("Action error details: " + action.getLastError().getMessage());
            }
        }
    }

    public void start() {
        telemetryManager.info("Robot starting...");
        // Additional start logic if needed
    }

    public void loop() {
        if (!isInitialized) {
            telemetryManager.error("Robot not properly initialized!");
            return;
        }

        try {
            // Update all components
            for (Component component : components.values()) {
                try {
                    component.update();
                    telemetryManager.addMapToBatch(
                            component.getName(),
                            component.getTelemetry()
                    );
                } catch (Exception e) {
                    telemetryManager.error("Component " + component.getName() +
                            " update failed: " + e.getMessage());
                }
            }

            // Process drive controls
            if (driveTrain != null && driveTrain.isOperational()) {
                driveTrain.driveWithGamepad(gamepad1);
            }

            // Execute button actions
            //executeActions(gamepad1, gamepad2);

            // Write telemetry to device
            //
            telemetryManager.writeBatch();

        } catch (Exception e) {
            telemetryManager.error("Error in robot loop: " + e.getMessage());
            emergencyStop();
        }
    }

    public void stop() {
        telemetryManager.info("Robot stopping...");
        components.values().forEach(Component::stop);
    }

    private void emergencyStop() {
        telemetryManager.error("EMERGENCY STOP INITIATED");
        stop();
        isInitialized = false;
    }

    // Getters
    public DriveTrain getDriveTrain() {
        return driveTrain;
    }

    public HardwareMap getHardwareMap() {
        return hardwareMap;
    }

    public TelemetryManager getTelemetryManager() {
        return telemetryManager;
    }

    public boolean isOperational() {
        return isInitialized && driveTrain != null && driveTrain.isOperational();
    }
}