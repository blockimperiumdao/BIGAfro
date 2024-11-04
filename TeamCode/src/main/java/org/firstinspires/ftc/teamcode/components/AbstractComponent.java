// AbstractComponent.java
package org.firstinspires.ftc.teamcode.components;

import org.firstinspires.ftc.teamcode.Afrobot;
import org.firstinspires.ftc.teamcode.TelemetryManager;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractComponent implements Component {
    protected Afrobot robot;
    protected TelemetryManager telemetryManager;
    protected boolean isInitialized = false;
    protected Map<String, Object> telemetryData;

    @Override
    public void init(Afrobot robot) {
        this.robot = robot;
        this.telemetryManager = robot.getTelemetryManager();
        this.telemetryData = new HashMap<>();
        try {
            initializeComponent();
            isInitialized = true;
            telemetryManager.info(getName() + " initialized successfully");
        } catch (Exception e) {
            isInitialized = false;
            telemetryManager.error(getName() + " initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize " + getName(), e);
        }
    }

    /**
     * Component-specific initialization logic
     */
    protected abstract void initializeComponent() throws Exception;

    @Override
    public boolean isOperational() {
        return isInitialized;
    }

    @Override
    public Map<String, Object> getTelemetry() {
        return telemetryData;
    }

    protected void emergencyStop(String reason) {
        telemetryManager.error("EMERGENCY STOP: " + reason);
        stop();
        isInitialized = false;
        telemetryManager.warning("Component [" + getName() + "] disabled in Emergency Stop - requires re-initialization");
    }
}