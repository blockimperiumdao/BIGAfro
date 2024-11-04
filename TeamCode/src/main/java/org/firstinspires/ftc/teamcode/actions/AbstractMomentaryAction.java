package org.firstinspires.ftc.teamcode.actions;

public abstract class AbstractMomentaryAction implements ActionInterface {
    private final String name;
    private final String description;
    protected ActionState state = ActionState.READY;
    protected Exception lastError;
    private boolean isOperational = true;

    public AbstractMomentaryAction(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public final ActionType getType() {
        return ActionType.MOMENTARY;
    }

    @Override
    public final ActionState getState() {
        return state;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final void start() {
        try {
            if (state == ActionState.READY && isOperational) {
                state = ActionState.RUNNING;
                onStart();
            }
        } catch (Exception e) {
            lastError = e;
            state = ActionState.FAILED;
            throw e;
        }
    }

    @Override
    public final void update() {
        try {
            if (state == ActionState.RUNNING && isOperational) {
                onUpdate();
            }
        } catch (Exception e) {
            lastError = e;
            state = ActionState.FAILED;
            end();
            throw e;
        }
    }

    @Override
    public final void end() {
        try {
            state = ActionState.READY; // Momentary actions reset to READY
            onEnd();
        } catch (Exception e) {
            lastError = e;
            state = ActionState.FAILED;
            throw e;
        }
    }

    @Override
    public final boolean isComplete() {
        return false; // Momentary actions are never "complete" - they run until button is released
    }

    @Override
    public boolean isOperational() {
        return isOperational;
    }

    @Override
    public Exception getLastError() {
        return lastError;
    }

    /**
     * Sets the operational state of the action
     */
    protected void setOperational(boolean operational) {
        isOperational = operational;
    }

    /**
     * Called when the action starts. Override to implement action-specific startup behavior.
     */
    protected abstract void onStart();

    /**
     * Called each update while the action is running. Override to implement action-specific behavior.
     */
    protected abstract void onUpdate();

    /**
     * Called when the action ends (when button is released).
     * Override to implement action-specific cleanup behavior.
     */
    protected abstract void onEnd();
}