# Installing the Application

## Downloading

When ready for public release, you should find a installer [here](https://github.com/aless2003/Tachidesk-VaadinUI/releases/latest). Just choose the appropriate file type for your OS. (e.g .exe or .msi for Windows, .dmg for Mac...)

## Installing

### Windows

Just double click either the EXE or the MSI and follow the instructions of it until you come to the end. After that it should be installed on your System.

### Mac

On Mac just double click the downloaded DMG file and drag the Tachidesk Vaadin UI icon onto the Applications folder

### Linux

On Linux install the appropriate package you downloaded for your system with the package manager of your choice. An example would be

``` bash
sudo dpkg -i vaadinui.deb
```

# Troubleshooting

## When starting it's stuck on "Waiting for Server to start"

*If you haven't been brought here by the Application if you have this issue, please report it, that's a bug.*

Make sure you have the JRE or JDK installed on your system with a version greater or equal to 17. While every JVM should work I have been testing it with Liberica from Bellsoft so if you need help to install the JRE you can follow these steps to install Liberica on your system.

### Downloading from Bellsoft's site

- Go to [**this**](https://bell-sw.com/pages/downloads/) link and select JDK 17 LTS
- Search for your OS Icon under the available installers (for example the Windows Logo)
- Click on the Dropdown where it says "Package: Standard JDK" and select keep it at "Standard JDK" or "Standard JRE". You can also choose the Full versions respectively, but it shouldn't be necessary to run the Manga Reader.
- Click on the Download button on the right of where you selected the your OS. Make sure to Download the Installer for your OS and not a packaging format like Zip or Tar.gz
- Now just follow the installer until it installed.

### Package Managers

For updated versions of these commands please see [**this**](https://bell-sw.com/pages/package-managers/) and [**this**](https://bell-sw.com/pages/repositories/) link.
\#### Winget

``` powershell
winget BellSoft.LibericaJDK17
```

#### Scoop

``` powershell
scoop bucket add java
scoop install liberica17
```

#### Brew

``` bash
brew tap bell-sw/liberica
brew install --cask liberica-jdk17
```

#### APT

``` bash
wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | sudo apt-key add -
echo "deb [arch=amd64] https://apt.bell-sw.com/ stable main" | sudo tee /etc/apt/sources.list.d/bellsoft.list
```

------------------------------------------------------------------------

After the above steps this issue should now be fixed. All you have to do is restart the App (You can close it via the Task Tray) and it should be running perfectly now!
