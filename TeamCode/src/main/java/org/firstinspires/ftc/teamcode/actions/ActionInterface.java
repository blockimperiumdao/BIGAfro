package org.firstinspires.ftc.teamcode.actions;

public interface ActionInterface {
    /**
     * Get the type of action (CONTINUOUS or MOMENTARY)
     */
    ActionType getType();

    /**
     * Get the current state of the action
     */
    ActionState getState();

    /**
     * Get the name of this action
     */
    String getName();

    /**
     * Get the description of this action
     */
    String getDescription();

    /**
     * Called when the action should begin executing
     */
    void start();

    /**
     * Called each loop iteration while the action is running
     */
    void update();

    /**
     * Called when the action should stop executing
     */
    void end();

    /**
     * Returns true if a CONTINUOUS action has completed its task
     * For MOMENTARY actions, this should always return false
     */
    boolean isComplete();

    /**
     * Returns true if the action was initialized properly and is ready to execute
     */
    boolean isOperational();

    /**
     * Returns the most recent error that occurred during execution, if any
     */
    Exception getLastError();
}