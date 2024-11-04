package org.firstinspires.ftc.teamcode.actions;

public enum ActionState {
    READY,      // Initial state
    RUNNING,    // Action is currently executing
    COMPLETED,  // Action has finished successfully
    CANCELLED,  // Action was cancelled before completion
    FAILED      // Action encountered an error
}