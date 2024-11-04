// Component.java
package org.firstinspires.ftc.teamcode.components;

import org.firstinspires.ftc.teamcode.Afrobot;
import java.util.Map;

public interface Component {
    /**
     * Get the unique identifier for this component
     */
    String getName();


    ComponentType getType();

    /**
     * Initialize the component with robot reference
     * @throws RuntimeException if initialization fails
     */
    void init(Afrobot robot);

    /**
     * Update component state - called each loop iteration
     */
    void update();

    /**
     * Check if component is properly initialized and operational
     */
    boolean isOperational();

    /**
     * Stop all component activities safely
     */
    void stop();

    /**
     * Get telemetry data for this component
     * @return Map of telemetry key-value pairs
     */
    Map<String, Object> getTelemetry();
}