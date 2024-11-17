package org.firstinspires.ftc.teamcode.systems;

import org.firstinspires.ftc.teamcode.components.ComponentType;

import java.util.Map;

public interface SystemInterface
{
    void init(Map<String,Object> parameters);
    void update();


    /**
     * Get the unique identifier for this component
     */
    String getName();

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
