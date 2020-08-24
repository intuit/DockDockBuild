
If you are using Docker Toolbox, you need to set up your Virtual Machine to enable folder sharing between your host machine
and the VM.
In order to compile your target on a container, your code root folder needs to be shared with the container. 
Since your container is running using the VirtualBox hypervisor, you need to allow your VM access to the relevant folder:
1. Make sure Docker Toolbox is not running, by running the `docker-machine stop` command in your terminal.
2. Open VirtualBox.
3. Right-click on the machine that is used by Docker-Toolbox. It is usually named "default", unless you have changed it 
manually.
4. Click on "Settings", and go to the Shared Folders tab: ![shared folders screen.jpg](https://github.com/intuit/DockDockBuild/blob/master/images/vm_shared_folders_screen.JPG?raw=true)
Click on the **+** button.
5. Select your project's code root folder, and **give it the same name as defined in your DockDockBuild run configuration as code root**.
  > For Windows users:
 Please note that the path configuration cannot include any special characters. For example,
To define the path *C:/Users/Me/MyProject*, the configuration should be */c/Users/Me/MyProject*.
6. Start Docker Toolbox by running the command `docker-machine start` in your terminal.
7. Connect to your docker-machine by SSH: `docker-machine ssh`.
8. Create a folder as a mount point for your volume, for example - `mkdir mount_here`.
9. Mount the shared folder, which you have defined in VirtualBox, at the folder you have created - 
`sudo mount -t vboxsf -o uid=1000,gid=50 MyProject /home/docker/mount_here`.

Your docker machine now has access to your project's source code root. 
