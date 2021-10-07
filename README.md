DockDockBuild: Docker Based Makefile Support for IntelliJ-Based IDEs
====================================================================

DockDockBuild is an open source plugin for IntelliJ that dockerizes your makefile compilation.

![logo](https://github.com/intuit/DockDockBuild/blob/master/images/logo_banner.png?raw=true)
[![GitHub Build Status](https://img.shields.io/github/workflow/status/intuit/DockDockBuild/Java%20CI)](https://github.com/intuit/DockDockBuild/tree/master)
[![codecov](https://codecov.io/gh/intuit/DockDockBuild/branch/master/graph/badge.svg)](https://codecov.io/gh/intuit/DockDockBuild)
[![GitHub release](https://img.shields.io/github/release/intuit/DockDockBuild.svg)](https://github.com/intuit/DockDockBuild/releases)
[![IntelliJ plugin](https://img.shields.io/jetbrains/plugin/v/13740-dockdockbuild)](https://plugins.jetbrains.com/plugin/13740-dockdockbuild)


When compiling a target in a makefile, this plugin builds a docker container and runs the compilation in that container. 
The container shares the host's code root path by using *Docker's volume mapping*, making the compiled code available and persistent. 
  
DockDockBuild allows developers to compile their project on an immutable, uniform environment, 
without dedicated installations on their machine - just Docker.

This plugin fully supports GNU Make syntax, and provides:

 * Syntax highlighting
 * Run configurations
 * Gutter marks & context actions to run targets
 * and more
  
Based on https://plugins.jetbrains.com/plugin/9333-makefile-support

Prerequisites
-------------
  1. Docker - see installation instructions for [Mac][Mac], [Windows][Windows] and [Ubuntu][Ubuntu].
      >Please note that in some cases, it is recommended that Mac and Windows users install [Docker Toolbox][Docker Toolbox] rather than Docker Desktop, as the latter might cause compatibility issues with Hyper-V - read more [here](https://docs.docker.com/docker-for-mac/docker-toolbox/).
  		
  2. IntelliJ 2020.02 and above - if you have an older version, download the latest one [here][intelliJ download]. It is recommended to uninstall the old version silently, so that you don't lose your old configuration.
  
Installation
------------
### From the IntelliJ Plugin Marketplace
In Settings, click on Plugins. You will find DockDockBuild in the Marketplace tab.
Click on **Install**.

###### You may be required to restart your IDE following the installation.
### From zip
In your IntelliJ Settings screen, click on **Plugins**. Click on **Settings** and select **Install Plugin From Disk**:
 
![Install plugin from zip](https://github.com/intuit/DockDockBuild/blob/master/images/install_from_zip.png?raw=true)

Select the zip file, and then click on **Install**. 

###### You may be required to restart your IDE following the installation.


Configuration
-------------
### Plugin Configuration
Since DockDockBuild builds a docker container and runs the make command on it, you need to define the paths to your Docker executable, code root and Maven cache, for example:

Docker Desktop for Mac:
![plugin_config](https://github.com/intuit/DockDockBuild/blob/master/images/plugin_config.png?raw=true)

Set the **Path to Docker executable** to be the location of the *docker.exe* file on your machine.

Set the **Path to code root** to be the location of your project's source code root folder.

Set the **Path to Maven cache** as the location of your .m2 folder.

Optionally: Set the **Advanced docker settings** with additional settings for the docker run command (mount additional volumes, etc.).

### Run Configuration
DockDockBuild's **run configuration** is where you can select which Makefile and Dockerfile are used for building your project, 
as well as define environment variables. 

When compiling a specific target from your Makefile, a default Run Configuration is set and used 
instantly by DockDockBuild, based on the location of the Makefile and the target being compiled.

You may also define multiple run configurations manually, and select which one to use each
time you compile a target.

![run config.jpg](https://github.com/intuit/DockDockBuild/blob/master/images/run_config.png?raw=true)


* Name your configuration.
* Select the local Dockerfile or Docker image you would like to use.
* Select the Makefile you would like to run.
* Specify the target to be built.
* [OPTIONAL] Select a script to be run on the container before the *make* command, that sets environment variables, 
runs installations etc.  

### Additional Configuration for Docker-Toolbox Users
[Here](./DOCKER_TOOLBOX_SETUP.md)

Usage
-----
There are multiple ways to build your project using DockDockBuild:

1. Select a run configuration from the drop-down list and click on the "play" button. ![select_run_config.png](https://github.com/intuit//DockDockBuild/blob/master/images/select_run_config.png?raw=true)
2. Go to your Makefile, and click on the "play" button next to the relevant target.
 
Contribution
------------
We welcome contributions from everyone. Please refer to the documentation [here](.github/CONTRIBUTING.md) for more info.

[Mac]:https://docs.docker.com/docker-for-mac/install/
[Windows]:https://docs.docker.com/docker-for-windows/install/
[Ubuntu]:https://docs.docker.com/install/linux/docker-ee/ubuntu/
[Docker Toolbox]:https://docs.docker.com/toolbox/overview/

[intelliJ download]:https://www.jetbrains.com/idea/download
[gradle-intellij-plugin]:https://github.com/JetBrains/gradle-intellij-plugin
[Grammar-Kit]:https://github.com/jetbrains/grammar-kit
[Grammar-Kit plugin]:https://plugins.jetbrains.com/plugin/6606-grammar-kit
