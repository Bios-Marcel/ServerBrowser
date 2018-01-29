# SA-MP Server Browser

[![Build Status](https://travis-ci.org/Bios-Marcel/ServerBrowser.svg?branch=master)](https://travis-ci.org/Bios-Marcel/ServerBrowser)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7e9eab6cb5644617a590ef4e81a2e466)](https://www.codacy.com/app/Bios-Marcel/ServerBrowser?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Bios-Marcel/ServerBrowser&amp;utm_campaign=Badge_Grade)

## Description

This application offers a row of tools for the `San Andreas-Multiplayer` mod for the `Grand Theft Auto: San Andreas` game.

### Features

* Save your favourite servers
* Access to a list of currently over 2000 servers
* Version changer
* Username history
* Access to SA-MP Settings via graphical userinterface
* Access to SA-MP chatlogs (with colors and timestamps)
* Server history
* MORE TO COME ...
  * Serverspecific usernames
  * Screenshot viewer

## Donate

If you want to make me feel better, since i am dedicating my free time to do this, feel free to donate something on https://ko-fi.com/marcelschr .

### Languages

This software is currently available in the following languages (some might not be perfectly and completly translated):

* English
* German 
* Georgian (Translated by Medzvel)
* Greek (Translated by vassilis)
* Dutch (Translated by Jsytlez)
* Russian (Translated by Codeah)
* Polish (Translated by AbyssMorgan)
* Romanian (Translated by IstuntmanI)
* Spanish (Translated by Unrea1, updated by RIDE2DAY)
* Turkish (Translated by MustafaKemalAtaturk)

## Installation

Overall, you have 3 options:

#### Downloading the latest launcher.jar file

The latest `launcher.jar` is available under https://github.com/Bios-Marcel/ServerBrowser/releases/latest , but it will **require** you to have Java 8 or later installed, i strongly suggest installing **Java 9**, since future versions of this project will use **Java 9**.

#### Downloading the latest Installer

The latest installer is also available under https://github.com/Bios-Marcel/ServerBrowser/releases/latest
Unlike when using the `launcher.jar` file, the installer **won't require** you to download anything other than the installer itself.

#### Build all the stuff yourself

Incase you are paranoid and scared that i might have infected the files in the release section, feel free to build the project yourself.
For further information on how to build the project, check the [Build Section](https://github.com/Bios-Marcel/ServerBrowser#build) below

## Troubleshooting

#### Your client isn't able to fetch servers

If your Client isn't able to fetch any servers anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

The second thing you might want to check, is your firewall. Make sure you haven't blocked the application itself.

#### Your client doesn't start anymore

In case your client doesn't start anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

If downloading the latest version of the client doens't help, try removing your currently installed Java runtime, if you have never installed a Java runtime, deinstall the ServerBrowser using the default Windows uninstalling procedure. After deleting Java and/or the ServerBrowser reinstall it.

## Pictures

There is a light and a dark theme, here are screenshots including both themes:

![Servers Dark](https://i.imgur.com/aRGXsam.png)

![Servers Light](https://i.imgur.com/qA1YuNw.png)

![Past Usernames Dark](https://i.imgur.com/wq0VcJF.png)

![Past Usernames Light](https://i.imgur.com/BCDKzCB.png)

![Version Changer Dark](https://i.imgur.com/Ye8VQSU.png)

![Version Changer Light](https://i.imgur.com/B8ZnpDv.png)

![Files Dark](https://i.imgur.com/3sTYXAw.png)

![Files Light](https://i.imgur.com/RnTOzla.png)

![Settings Dark](https://i.imgur.com/RiFGrv7.png)

![Settings Light](https://i.imgur.com/CVds2Od.png)

## Documentation

Javadoc is available under: (https://bios-marcel.github.io/ServerBrowser-Doc/overview-summary.html)

User Documentation (not complete) is available under: (https://github.com/Bios-Marcel/ServerBrowser/wiki)

## You want to help?
You can help by [reporting bugs](https://github.com/Bios-Marcel/ServerBrowser/issues), [recommending new features](https://github.com/Bios-Marcel/ServerBrowser/issues) or [creating pull requests](https://github.com/Bios-Marcel/ServerBrowser/pulls).

Another way for you to contribute, is to help localizing the application

### How to create a new language file

If you want to translate the program into another language, simply copy `/client/src/main/resources/com/msc/serverbrowser/localization/Lang_en.properties` and name it `lang_YOUR_COUNTRIES_SHORTCUT.propeties`.
After having done the translation, send me the file or do a pull request on GitHub. Please be careful to not modify any of the key names, that means only edit whats behind a `=`.
If possible try to escape all characters, since there is currently to UTF-8 support for language files.

### To be localized

Note: Some of the follwing key-value pairs might already be correct, in that case i don't know it though.

#### German
Done

#### Georgian
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
pageSize=PageSize
fetchingServers=Fetching servers, please wait a moment.
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
gtaNotFoundPrompt=Will be automatically detected if left empty
rememberLastView=Remember last view
inputMethodText=Input method text editing and language switching
cantFindGTA=Can't find GTA installation
customSampPath=Custom SA-MP Path
noFavouriteServers=You don't have any favourites.
directmode=Directmode (Fix chat text drawing problems)
tr=Turkish
activePlayers=Active players: {0}
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
removeUsernamePlural=Remove usernames
connectingToServer=Connecting to server
allowCachingSampVersions=Allow caching downloaded SA-MP versions
retrieving=Retrieving ...
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
multicoreUsage=Multicore usage
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Greek
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
fetchingServers=Fetching servers, please wait a moment.
chatTimestamps=Chat Timestamps
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
gtaNotFoundPrompt=Will be automatically detected if left empty
generalSettingsTitle=General
sampVersion=SA-MP Version {0}
cantFindGTA=Can't find GTA installation
ping=Ping:
noFavouriteServers=You don't have any favourites.
tr=Turkish
informationSettingsTitle=Information
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
versionInfo=Version: {0}
history=History
chatlogs=Chatlogs
removeUsernamePlural=Remove usernames
connectingToServer=Connecting to server
retrieving=Retrieving ...
servers=Servers: {0}
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Dutch
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
fetchingServers=Fetching servers, please wait a moment.
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
gamemodeTableHeader=Gamemode
cantFindGTA=Can't find GTA installation
ping=Ping:
downloadSettingTitle=Downloads
map=Map:
website=Website:
noFavouriteServers=You don't have any favourites.
tr=Turkish
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
removeUsernamePlural=Remove usernames
updatesSettingTitle=Updates
connectingToServer=Connecting to server
retrieving=Retrieving ...
servers=Servers: {0}
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
serverOffline=Server is offline.
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Russian
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
cantFindGTA=Can't find GTA installation
ping=Ping:
noFavouriteServers=You don't have any favourites.
tr=Turkish
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
removeUsernamePlural=Remove usernames
connectingToServer=Connecting to server
retrieving=Retrieving ...
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Polish
```
favourites=Favourites
lagcomp=lagcomp:
showTimestamps=Show times if available
ping=Ping:
tr=Turkish
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
removeUsernamePlural=Remove usernames
checkingForUpdates=Checking for updates
```

#### Romanian
```
favourites=Favourites
lagcomp=lagcomp:
showTimestamps=Show times if available
generalSettingsTitle=General
ping=Ping:
tr=Turkish
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
removeUsernamePlural=Remove usernames
checkingForUpdates=Checking for updates
```

#### Spanish
```
favourites=Favourites
showTimestamps=Show times if available
generalSettingsTitle=General
noFavouriteServers=You don't have any favourites.
tr=Turkish
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
removeUsernamePlural=Remove usernames
no=No
connectingToServer=Connecting to server
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
```

#### Turkish
```
favourites=Favourites
pageSize=PageSize
showTimestamps=Show times if available
gamemodeTableHeader=Gamemode
ping=Ping:
fpsLimit=FPS Limit
website=Website:
showChatlogColors=Show colors
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
removeUsernamePlural=Remove usernames
retrieving=Retrieving ...
checkingForUpdates=Checking for updates
```

## Build

This project is managed using [gradle](https://gradle.org).

### Structure

It is a multi-project consisting of 1 subproject

```
ServerBrowser			The parent project
|__ client				The Windows GUI client with which humans interact
```

The fact, that this is a multi-project is due to two components that don't exist anymore, this will soon be refactored.

### Building with gradle

To see which tasks are available, run:

**on Unix:**

``` shell
$ ./gradlew tasks
```

**on Windows:**

``` shell
$ ./gradlew.bat tasks
```

The first time you execute __gradlew__ (gradle wrapper) it will download a local copy of gradle into the project folder __.gradle__. This will not be committed to version control.

You are of course free to use your own or systems copy of gradle, but this approach has the advantage that we all share the same version of gradle.

### Running tasks on subprojects

To run tasks on the subprojects, you can either __cd__ into the subproject and run the task, e.g.:

``` shell
$ cd client
$ ../gradlew run
```

Or run it from the parent project by prefixing the task with the subprojects name and a ":" (colon).

``` shell
$ ./gradlew client:run
```

Assemble and test the build outputs. You will find the results in the __build__ folder of __client__.

``` shell
$ ./gradlew build
```

### Syncing gradle with Eclipse

Typically, when you import a gradle project into eclipse, it takes care of creating the eclipse project files via the plugin [buildship](https://github.com/eclipse/buildship).

However, if you want to be extra sure, don´t want to use the plugin or need to fix some synchronization issue between eclipse and gradle; It is useful to know how to manually do it:

From the parent project run:

``` shell
$ ./gradlew eclipseClean eclipse
```

Eclipse will instantly reload the fresh project settings files.

### Pipeline

__ServerBrowser__ utilizes advanced build techniques in order to assemble and optimize the output. The goal is to build the smallest possible 'self-contained' executable for Windows for the client.

__self-contained__ in this context means the JVM is bundled with the output.

In order to achieve this the following 2 step process is used:

1. Put all build outputs into a single JAR (fat JAR), including (transitive) dependencies.
2. Bundle the optimized JAR with a JVM and build a native container around them (exe/deb).

These steps map to these tools:

1. [Gradle Shadow Plugin](http://imperceptiblethoughts.com/shadow/#introduction)
2. [javapackager](https://github.com/FibreFoX/javafx-gradle-plugin) + [JavaFX-Gradle-Plugin](https://github.com/FibreFoX/javafx-gradle-plugin)

Our build scripts are largely glue around those tools.

### Building the native output

``` shell
$ ./gradlew jfxNative
```

This will generate an installable artifact in `client/build/jfx/native`. Depending on which platform you are building from, an EXE is generated on Windows, DEB/RPM on Linux and PKG/DMG on OSX.

We support Windows and Linux and in order to be able to generate an installer, some dependencies need to be installed.

#### Windows

- Inno Setup 5 or later

#### Linux (rpm)

- RPMBuild

#### Linux (deb)

- Debian packaging tools

### Javapackager

The underlying technology for building native installer bundles is `javapackager`.
Learn more about it here:

- https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/self-contained-packaging.html
- https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javapackager.html
- https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html

### Realising a signed build

Our builds are cryptographically signed. In order to build with signing enabled you need to setup 2 things.

* A `local.properties` file:

```
localSecretKeystorePassword = "choose super secret pw"
localSecretKeyPassword = "choose super secret pw yet again!"
```

* A keystore and private key

Once you have your passwords setup, these can automatically generated for you with:

``` shell
$ ./gradlew jfxGenerateKeyStore
```

None of these files should __ever__ be commited to version control !

## You need help?
[Send me an E-Mail](mailto:marceloschr@gmail.com)
