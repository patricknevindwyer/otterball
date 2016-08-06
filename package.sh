#!/bin/sh
mkdir release
java -jar bin/packr.jar --platform mac --jdk bin/jdk1.8.0_25.jdk.zip --executable OtterBall --classpath assets/*.jar --mainclass com.programmish.otterball.OBCore --vmargs Xmx1G XstartOnFirstThread --output release/OtterBall.app --icon assets/Network.icns