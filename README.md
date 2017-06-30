# ServerBrowser

[![Build Status](https://travis-ci.org/Bios-Marcel/ServerBrowser.svg?branch=master)](https://travis-ci.org/Bios-Marcel/ServerBrowser)

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
$ ../gradlew run
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

__ServerBrowser__ utilizes advanced build techniques in order to assemble and optimize the output. The goal is to build the smallest possible 'self-contained' executable for Windows for the client. __shared__ is simply a library project and as such produces a JAR and the server get assembled to a distribution ZIP with executable scripts for all platforms. 

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

### Reflection and Resources with Proguard

We use [Proguard](https://www.guardsquare.com/en/proguard/manual/introduction) to optimized the output JAR. This means that reflection and resource loading need to be handled with care.

#### Changing Proguard config and looking at optimized stacktraces

You can change the file __proguard.pro__ in your favorite editor directly, or use the ProguardGUI from the Proguard project.

```shell
$ ./gradlew client:runProguardGui
```

If you need to de-obfuscate a stacktrace from the optimized version of the client, you can use the __ReTrace__ tab of the ProguardGui.

TODO(bugabinga): Add task to start ReTrace directly.

#### Reflection

First of all; __don´t use reflection!__ It´s performance is horrible and the runtime behavior hard to predict.

If you absolutely have to have reflection, you need to mark classes that get accessed at runtime via reflection in the __proguarg.pro__ config file.

To learn how to do this read the intro about reflections.
* [in the official documentation](https://www.guardsquare.com/en/proguard/manual/introduction) 
* [the keep options documentation](https://www.guardsquare.com/en/proguard/manual/usage#keepoptions)

Pay special attention to the "Keep option modifiers" in order to at least __allowshrinking__ and __allowoptimization__ if possible.

#### Resources

If resources like images need to be loaded in the code, make sure to never hard-code the paths which contain package names because those get rewritten during optimization.

**Example bad:**

```
TODO
```

**Example good:**

```
TODO
```

#### Running optimized JAR

In order to test the optimized client, simply run:

``` shell
$ ./gradlew client:runOptimized
```

This will optimize the output JAR and run it. This makes it easy to test new __proguard.pro__ configs.

## Troubleshooting

If your Client isn't able to connect to the server anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

The second thing you might want to check, is your firewall. Make sure you haven't blocked port 1099 or the application itself.

## You need help?
[Send me an E-Mail](mailto:marceloschr@gmail.com)
