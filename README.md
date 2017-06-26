# ServerBrowser

## Description
This program enables you to access a list of over 1000 SA-MP Servers, join them, save them as your favorites or search for specific servers.
In addition to that it includes a Version Changer (0.3a - 0.3.7) and a list of past user names.

## You want to help?
You can help by [reporting bugs](https://github.com/Bios-Marcel/ServerBrowser/issues), [recommending new features](https://github.com/Bios-Marcel/ServerBrowser/issues) or [creating pull requests](https://github.com/Bios-Marcel/ServerBrowser/pulls).

## Build

This project is managed using [gradle](https://gradle.org).

### Structure

It is a multi-project consisting of 3 subprojects

```
ServerBrowser			The parent project
|__ client				The Windows GUI client with which humans interact
|__ server				The Unix daemon with which the client interacts via a sockets API
|__ shared				A library with shared functionality for the client and the server
```

### Building with gradle

To see which tasks are available, run:

__ on Unix:__

``` shell
$ ./gradlew task
```

__ on Window:__

``` shell
$ ./gradlew.bat task
```

The first time you execute __gradlew__ (gradle wrapper) it will download a local copy of gradle into the project folder __.gradle__. This will not be committed to version control.

You are of course free to use your own or systems copy of gradle, but this approach has the advantage that we all share the same version of gradle.

### Running tasks on subprojects

To run tasks on the subprojects, you can either __cd__ into the subproject and run the task, e.g.:

``` shell
$ cd client
$ ./gradlew run
```

Or run it from the parent project by prefixing the task with the subprojects name and a ":" (colon).

``` shell
$ ./gradlew client:run
```

### Syncing gradle with Eclipse

Typically, when you import a gradle project into eclipse, it takes care of creating the eclipse project files via the plugin [buildship](https://github.com/eclipse/buildship).

However, if you want to be extra sure, don´t want to use the plugin or need to fix some synchronization issue between eclipse and gradle; It is useful to know how to manually do it:

From the parent project run:

``` shell
$ ./gradlew eclipseClean eclipse
```

Eclipse will instantly reload the fresh project settings files.

## Troubleshooting

If your Client isn't able to connect to the server anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

The second thing you might want to check, is your firewall. Make sure you haven't blocked port 1099 or the application itself.

## You need help?
[Send me an E-Mail](mailto:marceloschr@gmail.com)
