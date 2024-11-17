package org.firstinspires.ftc.teamcode.systems;

import java.util.Map;

public abstract class AbstractSystem implements SystemInterface {

    protected boolean isComplete = true;

    public abstract void update();
    public abstract void init(Map<String,Object> parameters);



}
