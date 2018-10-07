# aeron-monitoring

# Build

[Gradle](http://gradle.org/) is used as build system. Latest stable JDK 8 is also required to build
the project.

Clean build:

```shell
    $ git clone https://github.com/easy-logic/aeron-monitoring.git
    $ cd aeron-monitoring
    $ ./gradlew
```

# Run 

```shell
    $ java -jar aeron-monitor.jar [-Dloader.path="plugins-path"] [--port=9000]
```

# License 

Copyright 2018 Easy Logic

Licensed under the MIT License

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
