# Tachidesk-VaadinUI

A free and open source manga reader that
uses [Tachidesk-Server](https://github.com/Suwayomi/Tachidesk-Server).

This is written in Java with the help of the Vaadin Framework.

# Is this Application usable?

Yes, but any bug reports are welcome. If you want to contribute, please do so.

Here's a list of features that are currently implemented:

- [x] Managing Extensions
- [x] Exploring Sources
- [x] Searching for Manga
- [x] Reading Manga/Chapters
- [x] Downloading Manga/Chapters
- [x] Managing Manga in a Library
- [x] Managing Categories
- [x] External Server Instance Support
- [x] Tracking via AniList
- [x] Search via AniList

# How do I get this Application

As of right now there's only a Windows build available. You can build installers for your OS via
Gradle though. Just run 'gradlew buildLinuxDeb' for Debian based Linux distributions, 'gradlew
buildLinuxRpm' for
RPM based Linux distributions or 'gradlew buildMacDmg' for MacOS.

I'll eventually get around to making builds for other OSes as well, but I'll have to explore how I
can do so via GitHub actions.

# Troubleshooting

## I started the Application, but it doesn't open in my browser

You can manually open the Application by going to http://localhost:8080 or go in the task tray and
double click the icon there.

# Support and Help

Join the [Discord Server](https://discord.com/invite/DDZdqZWaHA) and ask your questions in the
#support channel. You can Ping @diamondlp. Note that I might not respond immediately.

# How do I build this Application

## Jar

To build a jar file, just run 'gradlew buildReleaseJar'. The jar file will be located in the
build/libs folder and should be named Tachidesk-VaadinUI-x.y.z.jar.

## Windows

To build a Windows installer, just run 'gradlew buildWindowsExe' for an .exe installer or 'gradlew
buildWindowsMsi' for an .msi installer (recommended). The installer will be located in the
build/installer folder.

## Linux

To build a Linux installer, just run 'gradlew buildLinuxDeb' for a Debian based Linux distribution
or 'gradlew buildLinuxRpm' for a RPM based Linux distribution. The installer will be located in the
build/installer folder.

## MacOS

To build a MacOS installer, just run 'gradlew buildMacDmg' for a .dmg file or 'gradlew buildMacPkg'
for a .pkg file. Whichever you prefer. The installer will be located in the build/installer folder.

# Credit

The `Tachidesk-Server` project is developed by [AriaMoradi](https://github.com/AriaMoradi) and other
contributors. Feel free to check it out [here](https://github.com/Suwayomi/Tachidesk-Server).
