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
$ ./gradlew run
```

Or run it from the parent project by prefixing the task with the subprojects name and a ":" (colon).

``` shell
$ ./gradlew client:run
```

Assemble and test the build outputs. You will find the results in the __build__ folders of __client__ and __server__.

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

__ServerBrowser__ utilizes advanced build techniques in order to assemble and optimize the output. The goal is to build the smallest possible 'self-contained' executable for Windows for the client and respectively a DEB file for the server.

__self-contained__ in this context means the JVM is bundled with the output.

In order to achieve this the following 3 step process is used:

1. Put all build outputs into a single JAR (fat JAR), including (transitive) dependencies.
2. Optimize, shrink and obfuscate the fat JAR.
3. Bundle the optimized JAR with a JVM and build a native container around them (exe/deb).

These steps map to these tools:

1. [Gradle Shadow Plugin](http://imperceptiblethoughts.com/shadow/#introduction)
2. [Proguard](https://www.guardsquare.com/en/proguard/manual/gradle)
3. [javapackager](https://github.com/FibreFoX/javafx-gradle-plugin) + [JavaFX-Gradle-Plugin](https://github.com/FibreFoX/javafx-gradle-plugin)

Our build scripts are largely glue around those tools.

### TODO Debug Build

### TODO Release Build

## Troubleshooting

If your Client isn't able to connect to the server anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

The second thing you might want to check, is your firewall. Make sure you haven't blocked port 1099 or the application itself.

### TODO Reflection and Resources

### Proguard Errors

```
Warning: there were 1 kept classes and class members that were remapped anyway.
         You should adapt your configuration or edit the mapping file.
         If you are sure this remapping won't hurt, you could try your luck
         using the '-ignorewarnings' option.
         (http://proguard.sourceforge.net/manual/troubleshooting.html#mappingconflict1)
```

This most likely means, that new classes were added to the __keep__ list of proguard, but they are still referenced as __obfuscated__ in __proguard.map__.

Run __clean__ to reset the file and build again.

``` shell
$ ./gradlew clean
```


## You need help?
[Send me an E-Mail](mailto:marceloschr@gmail.com)
