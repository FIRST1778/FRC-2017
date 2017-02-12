# Steamworks1778
The is the 2017 Java code for Chillout 1778, used for the 2017 Season, SteamWorks.

The code is organized in the following groups:

org/usfirst/frc/team1778/robot.java:
Main robot class, derived from WPILib IterativeRobot base class

Systems:
Contain static classes that control physical subsystems on the robot

StateMachine:
Classes that control the robot during the autonomous phase

FreezyDrive:
Classes for control of the drivetrain during teleop phase

NetworkComm:
Classes responsible for communicating outside the Roborio (RPi, Driver station, etc)

Utility:
Helper classes used by other packages