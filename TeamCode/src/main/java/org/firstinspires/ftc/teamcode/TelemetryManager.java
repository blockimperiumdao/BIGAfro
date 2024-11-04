package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TelemetryManager {
    private final Telemetry telemetry;
    private final Map<String, Object> batchData;
    private final List<String> logMessages;
    private boolean autoClear;
    private boolean autoUpdate;
    private int maxLogSize;
    private final SimpleDateFormat timeFormat;

    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }

    public TelemetryManager(Telemetry telemetry) {
        this.telemetry = telemetry;
        this.batchData = new HashMap<>();
        this.logMessages = new ArrayList<>();
        this.autoClear = false;
        this.autoUpdate = true;
        this.maxLogSize = 50;
        this.timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    }

    // Single item write with optional auto-update
    public void write(String key, Object value) {
        if (autoClear) {
            telemetry.clear();
        }
        telemetry.addData(key, value);
        if (autoUpdate) {
            telemetry.update();
        }
    }

    public void write(String key, String format, Object... args) {
        if (autoClear) {
            telemetry.clear();
        }
        telemetry.addData(key, format, args);
        if (autoUpdate) {
            telemetry.update();
        }
    }

    // Batch operations
    public void addToBatch(String key, Object value) {
        batchData.put(key, value);
    }

    // Add a map of data to the batch
    public void addMapToBatch(Map<String, Object> dataMap) {
        if (dataMap != null) {
            batchData.putAll(dataMap);
        }
    }

    // Add a map of data to the batch with a prefix for all keys
    public void addMapToBatch(String prefix, Map<String, Object> dataMap) {
        if (dataMap != null) {
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                batchData.put(key, entry.getValue());
            }
        }
    }

    // Convenience method to add map with optional prefix
    public void addMapToBatch(String prefix, Map<String, Object> dataMap, boolean usePrefix) {
        if (usePrefix) {
            addMapToBatch(prefix, dataMap);
        } else {
            addMapToBatch(dataMap);
        }
    }

    public void clearBatch() {
        batchData.clear();
    }

    public void writeBatch() {
        if (autoClear) {
            telemetry.clear();
        }

        for (Map.Entry<String, Object> entry : batchData.entrySet()) {
            telemetry.addData(entry.getKey(), entry.getValue());
        }

        telemetry.update();
        clearBatch();
    }



    // Logging operations
    public void log(LogLevel level, String message) {
        String timestamp = timeFormat.format(new Date());
        String logEntry = String.format("[%s] %s: %s", timestamp, level, message);

        logMessages.add(0, logEntry); // Add to front for most recent first

        // Trim log if it exceeds max size
        while (logMessages.size() > maxLogSize) {
            logMessages.remove(logMessages.size() - 1);
        }

        if (autoUpdate) {
            displayLogs();
        }
    }

    public void displayLogs() {
        if (autoClear) {
            telemetry.clear();
        }

        // Display current batch data if any
        for (Map.Entry<String, Object> entry : batchData.entrySet()) {
            telemetry.addData(entry.getKey(), entry.getValue());
        }

        // Add a separator if we have both data and logs
        if (!batchData.isEmpty() && !logMessages.isEmpty()) {
            telemetry.addLine("----------------");
        }

        // Display logs
        for (String log : logMessages) {
            telemetry.addLine(log);
        }

        telemetry.update();
    }

    // Debug convenience methods
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    // Configuration methods
    public void setAutoClear(boolean autoClear) {
        this.autoClear = autoClear;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public void setMaxLogSize(int maxLogSize) {
        this.maxLogSize = maxLogSize;
    }

    public void clear() {
        telemetry.clear();
        telemetry.update();
    }

    public void clearLogs() {
        logMessages.clear();
        if (autoUpdate) {
            telemetry.update();
        }
    }

    // Force an update regardless of autoUpdate setting
    public void update() {
        telemetry.update();
    }

    // Example usage in a game loop
    public void beginLoop() {
        if (autoClear) {
            clear();
        }
    }

    public void endLoop() {
        displayLogs();
    }
}