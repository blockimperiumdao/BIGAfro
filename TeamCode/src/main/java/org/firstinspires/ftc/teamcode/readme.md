## TeamCode Module

Welcome!

This is the Afrobotics TeamCode project which contains a framework for building FTC robotics 
applications. While this framework will likely work fine using VSCode or some other development
framework, it has been tested using AndroidStudio.

## Creating your own OpModes

The easiest way to create your own OpMode is to copy one of the OpModes in the opmodes package. 
There is a simple opmode for teleop which will allow you to move a GoBilda specified swing arm
robot using the GoBilda odometry pods.

The Autonomous modes use the frameworks SULU (Sensor Utilization for Location Updates) framework to
plot courses around the map. SULU assumes that the tracking point is the center point of the robot,
but it can be any arbitrary point if you handle the rotation around that point properly.

### Package Overview

To gain a better understanding of how the packages are organized.


Actions:  	These is an action framework which defines a type of action that can be bound to a 
            button or otherwise executed as part of a larger framework such as a finite state 
            machine.

Components: This package represents all of the components that can be added to the robot. These 
            represent the base cases for these components and should be extended or replaced as
            necessary to meet a specific use case. Components make up the core functionality of the
            robot and are added using a composition pattern.

Drivers:	Any specific drivers or third party source files are located in this package so they
            can be compiled with the rest of the project and managed accordingly

Opmodes:	This contains the basic opmodes that can be modified or extended. These represent the 
            core use cases for how the robot is intended to be uses.


Each OpMode sample class begins with several lines of code like the ones shown below:

```
 @TeleOp(name="Template: Linear OpMode", group="Linear Opmode")
 @Disabled
```

The name that will appear on the driver station's "opmode list" is defined by the code:
 ``name="Template: Linear OpMode"``
You can change what appears between the quotes to better describe your opmode.
The "group=" portion of the code can be used to help organize your list of OpModes.

As shown, the current OpMode will NOT appear on the driver station's OpMode list because of the
  ``@Disabled`` annotation which has been included.
This line can simply be deleted , or commented out, to make the OpMode visible.


