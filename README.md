# Ubiquitous File Transmission (UFT)
Ubiquitous File Transmission (UFT) using Tuple Space (TS) and Sockets implemented in Java with JavaSpaces

## Requirements
**ApacheRiver - JavaSpace implementation (MacOS, Windows or Ubuntu)**
- [apache-river (releases)](https://river.apache.org/user-doc/releases.html)
- OBS.: You must start all apache-river services

**JRE (MacOS or Windows)**
- [jre-8u251-macosx-x64](https://www.oracle.com/java/technologies/javase-jre8-downloads.html) (MacOS)
- [jre-8u251-windows-x64](https://www.oracle.com/java/technologies/javase-jre8-downloads.html) (Windows)

**openssh-server, openjdk and openjfx (Ubuntu)**
- [openssh-server](https://help.ubuntu.com/lts/serverguide/openssh-server.html)
- [openjdk-8-jre](https://openjdk.java.net/install/)
- [openjfx 8](https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX+8u)

**P.S.: In Ubuntu 18.04 you might install the requirements as follows:**
- sudo apt install openssh-server
- sudo apt install openjdk-8-jre
- sudo apt install openjfx=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2
- sudo apt-mark hold openjfx libopenjfx-jni libopenjfx-java

## Screens

#### UFT login screen view
 - Device name: The name of the device to be registered
 - Ip address: The Ip address to be used on the socket
 - Port number: The port number to be used on the socket
 - x-axis,y-axis: Initial Cartesian device location
 
<p align="center">
  <a target="blank"><img src="https://user-images.githubusercontent.com/19287934/93331933-69aab780-f7f7-11ea-92f4-ef0898e27754.png" width="300" alt="UFT Login" /></a>
</p>

#### UFT main screen view
 - Devices where it has its location (x, y) are only in environments of other devices if the maximum distance between them is respected 10 units. Devices can only exchange files within the same environment.
 
<p align="center">
  <a target="blank"><img src="https://user-images.githubusercontent.com/19287934/93330999-079d8280-f7f6-11ea-9b20-9e2fa7e2aec2.png" width="400" alt="UFT main" /></a>
</p>
