# SmartLamp
A Java Service to control a Sonoff switch with a tasmota firmware via a system tray icon

The program will add an icon to the system tray, by clicking on it the state of the lamp can be toggled.
Depending whether the light is on or off a different icon will be displayed to indicate the state of the lamp.

## Getting started
You need maven to build an executable jar file. To do so run package.
When running the jar file, make sure to pass the address as argument to the java program, i.e.
```bash
java -jar SmartLamp.jar 192.168.178.12
```