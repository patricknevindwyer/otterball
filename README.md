Work with JSON (or just JSON like things (ahem Python output)) of all shapes and sizes.

See [Tasks](https://github.com/patricknevindwyer/otterball/blob/master/TASKS.md) for the current state of OtterBall


# Packaging

## PACKR

The native builds for OtterBall rely on [packr](https://github.com/libgdx/packr), or [download packr](http://bit.ly/packrgdx).

The `packr` jar is expected to be in the project `bin` directory

## Getting a JDK Zip

```sh
pushd /Library/Java/JavaVirtualMachines
zip -r ~/Downloads/jdk.zip jdk1.8.0_25.jdk
popd
```

Move the JDK to the project `bin` directory

## Export Jar

Open jar-build.jardesc and Finish or:

1. Eclipse File -> Export...
2. Java/JAR File
3. Select OtterBall / Export Generated class files and resources / Compress contents of JAR file
4. JAR FILE EXPORT DESTINATION: otterball/assets/OtterBall.jar
4. Select errors and warnings
5. Use existing manifest
6. Finish

## Run packr

```sh
> ./package.sh
Cleaning output directory '/Users/patricknevindwyer/projects/otterball/release/OtterBall.app' ...
Copying executable ...
Copying classpath(s) ...
Unpacking JRE ...
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Removing foreign platform libs ...
Done!
```

## Output

The built Mac lib is in `release/OtterBall.zip`