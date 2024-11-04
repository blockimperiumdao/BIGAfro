package org.firstinspires.ftc.teamcode.actions;

import org.firstinspires.ftc.teamcode.actions.ActionInterface;
import org.firstinspires.ftc.teamcode.actions.ActionState;

// Base abstract action with common functionality
public abstract class AbstractAction implements ActionInterface {
    private final String name;
    private final String description;
    protected ActionState state = ActionState.READY;
    protected Exception lastError;

    public AbstractAction(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public ActionState getState() {
        return state;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}