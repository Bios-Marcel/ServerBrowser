# SA-MP Server Browser

[![Codacy grade](https://img.shields.io/codacy/grade/7e9eab6cb5644617a590ef4e81a2e466.svg?style=flat-square)](https://app.codacy.com/app/Bios-Marcel/ServerBrowser/dashboard)
[![Waffle.io - Columns and their card count](https://badge.waffle.io/Bios-Marcel/ServerBrowser.svg?columns=all&style=flat-square)](https://waffle.io/Bios-Marcel/ServerBrowser) 

master: [![Build Status](https://img.shields.io/travis/Bios-Marcel/ServerBrowser.svg?branch=master&style=flat-square)](https://travis-ci.org/Bios-Marcel/ServerBrowser)

develop: [![Build Status](https://img.shields.io/travis/Bios-Marcel/ServerBrowser.svg?branch=develop&style=flat-square)](https://travis-ci.org/Bios-Marcel/ServerBrowser)

## Description

This application offers a row of tools for the `San Andreas-Multiplayer` mod for the `Grand Theft Auto: San Andreas` game.

### Features

* Save your favourite servers
* Access to a list of currently over 2000 servers
* Version changer
* Username history
* Access to SA-MP Settings via graphical user interface
* Access to SA-MP chat logs (with colors and timestamps)
* Server history
* MORE TO COME ...
  * Server-specific usernames
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
* Bosnian (Translated by Tagic)

## Installation

Overall, you have 3 options:

#### Downloading the latest launcher.jar file

The latest `launcher.jar` is available under https://github.com/Bios-Marcel/ServerBrowser/releases/latest , but it will **require** you to have Java 8 or later installed, i strongly suggest installing **Java 9**, since future versions of this project will use **Java 9**.

#### Downloading the latest Installer (NOT SUPPORTED ANYMORE)

The latest installer is also available under https://github.com/Bios-Marcel/ServerBrowser/releases/tag/8.5.7
Unlike when using the `launcher.jar` file, the installer **won't require** you to download anything other than the installer itself.
The installer isn't supported after version 8.5.7 anymore.

#### Build all the stuff yourself

In case you are paranoid and scared that i might have infected the files in the release section, feel free to build the project yourself.
For further information on how to build the project, check the [Build Section](https://github.com/Bios-Marcel/ServerBrowser#build) below

## Troubleshooting

#### Your client isn't able to fetch servers

If your Client isn't able to fetch any servers anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

The second thing you might want to check, is your firewall. Make sure you haven't blocked the application itself.

#### Your client doesn't start anymore

In case your client doesn't start anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

If downloading the latest version of the client doesn't help, try removing your currently installed Java runtime, if you have never installed a Java runtime, uninstall the ServerBrowser using the default Windows uninstalling procedure. After deleting Java and/or the ServerBrowser reinstall it.

## Pictures

There is a light and a dark theme, here are screenshots including both themes:

![Servers Dark](https://i.imgur.com/aRGXsam.png)

![Servers Light](https://i.imgur.com/qA1YuNw.png)

![Past Usernames Dark](https://i.imgur.com/wq0VcJF.png)

![Past Usernames Light](https://i.imgur.com/BCDKzCB.png)

![Version Changer Dark](https://i.imgur.com/Ye8VQSU.png)

![Version Changer Light](https://i.imgur.com/B8ZnpDv.png)

![Files Dark](https://i.imgur.com/3sTYXAw.png)

![Files Light](https://i.imgur.com/UvEcPHo.png)

![Settings Dark](https://i.imgur.com/RiFGrv7.png)

![Settings Light](https://i.imgur.com/CVds2Od.png)

## Documentation (Outdated)

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

Note: Some of the following key-value pairs might already be correct, in that case i don't know it though.

#### German
Done

#### Georgian
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
pageSize=PageSize
fetchingServers=Fetching servers, please wait a moment.
connectToServerUsingPassword=Connect using password
enterFilterValue=Enter filter value
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
lastVisit=Last visit
visitWebsite=Visit website
gtaNotFoundPrompt=Will be automatically detected if left empty
addToFavourites=Add to favourites
rememberLastView=Remember last view
inputMethodText=Input method text editing and language switching
ba=Bosnian
sureYouWantToRestoreLegacySettingsAswell=Do you also, in addition to the application settings, want to reset the SA-MP settings?
cantFindGTA=Can't find GTA installation
sureYouWantToRestoreLegacySettings=Are you sure, that you want to reset the SA-MP settings?
customSampPath=Custom SA-MP Path
noFavouriteServers=You don't have any favourites.
directmode=Directmode (Fix chat text drawing problems)
usePreReleases=Use pre-releases
tr=Turkish
activePlayers=Active players: {0}
showChatlogColors=Show colors
showChatlogColorsAsText=Show colors as text
copyIpAddressAndPort=Copy IP address and port
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
restoreLegacySettingsToDefault=Restore SA-MP settings to default
removeUsernamePlural=Remove usernames
connectingToServer=Connecting to server
allowCachingSampVersions=Allow caching downloaded SA-MP versions
retrieving=Retrieving ...
connectToServer=Connect to server
removeFromFavourites=Remove from favourites
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
multicoreUsage=Multicore usage
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Greek
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
fetchingServers=Fetching servers, please wait a moment.
chatTimestamps=Chat Timestamps
connectToServerUsingPassword=Connect using password
enterFilterValue=Enter filter value
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
lastVisit=Last visit
visitWebsite=Visit website
gtaNotFoundPrompt=Will be automatically detected if left empty
addToFavourites=Add to favourites
generalSettingsTitle=General
sureYouWantToRestoreLegacySettingsAswell=Do you also, in addition to the application settings, want to reset the SA-MP settings?
sampVersion=SA-MP Version {0}
cantFindGTA=Can't find GTA installation
ping=Ping:
sureYouWantToRestoreLegacySettings=Are you sure, that you want to reset the SA-MP settings?
noFavouriteServers=You don't have any favourites.
usePreReleases=Use pre-releases
informationSettingsTitle=Information
showChatlogColors=Show colors
showChatlogColorsAsText=Show colors as text
copyIpAddressAndPort=Copy IP address and port
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
versionInfo=Version: {0}
history=History
chatlogs=Chatlogs
restoreLegacySettingsToDefault=Restore SA-MP settings to default
removeUsernamePlural=Remove usernames
connectingToServer=Connecting to server
retrieving=Retrieving ...
connectToServer=Connect to server
servers=Servers: {0}
removeFromFavourites=Remove from favourites
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Dutch
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
fetchingServers=Fetching servers, please wait a moment.
connectToServerUsingPassword=Connect using password
enterFilterValue=Enter filter value
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
gamemodeTableHeader=Gamemode
lastVisit=Last visit
visitWebsite=Visit website
addToFavourites=Add to favourites
ba=Bosnian
sureYouWantToRestoreLegacySettingsAswell=Do you also, in addition to the application settings, want to reset the SA-MP settings?
cantFindGTA=Can't find GTA installation
ping=Ping:
downloadSettingTitle=Downloads
sureYouWantToRestoreLegacySettings=Are you sure, that you want to reset the SA-MP settings?
map=Map:
website=Website:
noFavouriteServers=You don't have any favourites.
usePreReleases=Use pre-releases
tr=Turkish
showChatlogColors=Show colors
showChatlogColorsAsText=Show colors as text
copyIpAddressAndPort=Copy IP address and port
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
restoreLegacySettingsToDefault=Restore SA-MP settings to default
removeUsernamePlural=Remove usernames
updatesSettingTitle=Updates
connectingToServer=Connecting to server
retrieving=Retrieving ...
connectToServer=Connect to server
servers=Servers: {0}
removeFromFavourites=Remove from favourites
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
serverOffline=Server is offline.
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Russian
```
errorFetchingServers=Couldn't fetch servers
favourites=Favourites
connectToServerUsingPassword=Connect using password
enterFilterValue=Enter filter value
lagcomp=lagcomp:
showTimestamps=Show times if available
locateGTAManually=Click here to enter your GTA path manually.
lastVisit=Last visit
visitWebsite=Visit website
addToFavourites=Add to favourites
ba=Bosnian
sureYouWantToRestoreLegacySettingsAswell=Do you also, in addition to the application settings, want to reset the SA-MP settings?
cantFindGTA=Can't find GTA installation
ping=Ping:
sureYouWantToRestoreLegacySettings=Are you sure, that you want to reset the SA-MP settings?
noFavouriteServers=You don't have any favourites.
usePreReleases=Use pre-releases
tr=Turkish
showChatlogColors=Show colors
showChatlogColorsAsText=Show colors as text
copyIpAddressAndPort=Copy IP address and port
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
restoreLegacySettingsToDefault=Restore SA-MP settings to default
removeUsernamePlural=Remove usernames
connectingToServer=Connecting to server
retrieving=Retrieving ...
connectToServer=Connect to server
removeFromFavourites=Remove from favourites
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
openDonationPageTooltip=Opens the 'Donate' section of the GitHub project page
```

#### Polish
Done

#### Romanian
```
connectToServerUsingPassword=Connect using password
lagcomp=lagcomp:
lastVisit=Last visit
visitWebsite=Visit website
addToFavourites=Add to favourites
ba=Bosnian
generalSettingsTitle=General
sureYouWantToRestoreLegacySettingsAswell=Do you also, in addition to the application settings, want to reset the SA-MP settings?
ping=Ping:
sureYouWantToRestoreLegacySettings=Are you sure, that you want to reset the SA-MP settings?
usePreReleases=Use pre-releases
showChatlogColorsAsText=Show colors as text
copyIpAddressAndPort=Copy IP address and port
restoreLegacySettingsToDefault=Restore SA-MP settings to default
connectToServer=Connect to server
removeFromFavourites=Remove from favourites
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
```

#### Spanish
```
favourites=Favourites
connectToServerUsingPassword=Connect using password
enterFilterValue=Enter filter value
showTimestamps=Show times if available
lastVisit=Last visit
visitWebsite=Visit website
addToFavourites=Add to favourites
ba=Bosnian
generalSettingsTitle=General
sureYouWantToRestoreLegacySettingsAswell=Do you also, in addition to the application settings, want to reset the SA-MP settings?
sureYouWantToRestoreLegacySettings=Are you sure, that you want to reset the SA-MP settings?
noFavouriteServers=You don't have any favourites.
usePreReleases=Use pre-releases
tr=Turkish
showChatlogColors=Show colors
showChatlogColorsAsText=Show colors as text
copyIpAddressAndPort=Copy IP address and port
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
restoreLegacySettingsToDefault=Restore SA-MP settings to default
removeUsernamePlural=Remove usernames
no=No
connectingToServer=Connecting to server
connectToServer=Connect to server
removeFromFavourites=Remove from favourites
checkingForUpdates=Checking for updates
serverMightBeOfflineConnectAnyways=The server might not be online, do you want to try connecting to it anyways?
noServerHistory=You haven't joined any servers recently.
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
```

#### Turkish
```
favourites=Favourites
pageSize=PageSize
connectToServerUsingPassword=Connect using password
enterFilterValue=Enter filter value
showTimestamps=Show times if available
gamemodeTableHeader=Gamemode
lastVisit=Last visit
visitWebsite=Visit website
addToFavourites=Add to favourites
sureYouWantToRestoreLegacySettingsAswell=Do you also, in addition to the application settings, want to reset the SA-MP settings?
ping=Ping:
sureYouWantToRestoreLegacySettings=Are you sure, that you want to reset the SA-MP settings?
fpsLimit=FPS Limit
website=Website:
usePreReleases=Use pre-releases
showChatlogColors=Show colors
showChatlogColorsAsText=Show colors as text
copyIpAddressAndPort=Copy IP address and port
donate=Donate
applyUsername=Apply username
all=All
removeUsernameSingular=Remove username
downloadingUpdate=Downloading update
history=History
restoreLegacySettingsToDefault=Restore SA-MP settings to default
removeUsernamePlural=Remove usernames
retrieving=Retrieving ...
connectToServer=Connect to server
removeFromFavourites=Remove from favourites
checkingForUpdates=Checking for updates
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
```

#### Bosnian
```
pageSize=PageSize
connectToServerUsingPassword=Connect using password
lagcomp=lagcomp:
gamemodeTableHeader=Gamemode
ping=Ping:
fpsLimit=FPS Limit
usePreReleases=Use pre-releases
enterServerPasswordMessage=Enter the servers password (Leave empty if you think there is none).
```

## Build

This project is managed using [gradle](https://gradle.org).

### Building with Gradle

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

Assemble and test the build outputs. You will find the results in the __build__ folder of __client__.

``` shell
$ ./gradlew build
```

In order to build a runnable `.jar` file, run:
``` shell
$ ./gradlew shadowJar
```

### Syncing gradle with Eclipse

Typically, when you import a gradle project into eclipse, it takes care of creating the eclipse project files via the plugin [buildship](https://github.com/eclipse/buildship).

However, if you want to be extra sure, don´t want to use the plugin or need to fix some synchronization issue between eclipse and gradle; It is useful to know how to manually do it:

From the parent project run:

``` shell
$ ./gradlew eclipseClean eclipse
```

Eclipse will instantly reload the fresh project settings files.

## You need help?
[Send me an E-Mail](mailto:marceloschr@gmail.com)
