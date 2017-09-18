# ServerBrowser

[![Build Status](https://travis-ci.org/Bios-Marcel/ServerBrowser.svg?branch=master)](https://travis-ci.org/Bios-Marcel/ServerBrowser)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7e9eab6cb5644617a590ef4e81a2e466)](https://www.codacy.com/app/Bios-Marcel/ServerBrowser?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Bios-Marcel/ServerBrowser&amp;utm_campaign=Badge_Grade)

## Description
This program enables you to access a list of over 1000 SA-MP Servers, join them, save them as your favorites or search for specific servers.
In addition to that it includes a Version Changer (0.3a - 0.3.7) and a list of past user names.

## Documentation

Documentation is currently available under the following link:

[Documentation](https://sa-mpservers.com/doc/overview-summary.html)

## You want to help?
You can help by [reporting bugs](https://github.com/Bios-Marcel/ServerBrowser/issues), [recommending new features](https://github.com/Bios-Marcel/ServerBrowser/issues) or [creating pull requests](https://github.com/Bios-Marcel/ServerBrowser/pulls).

## Build

This project is managed using [gradle](https://gradle.org).

### Structure

It is a multi-project consisting of 3 subprojects

```
ServerBrowser			The parent project
|__ client				The Windows GUI client with which humans interact
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

## Troubleshooting

If your Client isn't able to connect to the server anymore, the first thing you should do, is to try and download the [latest version of the client](https://github.com/Bios-Marcel/ServerBrowser/releases/latest).

The second thing you might want to check, is your firewall. Make sure you haven't blocked port 1099 or the application itself.

## You need help?
[Send me an E-Mail](mailto:marceloschr@gmail.com)
