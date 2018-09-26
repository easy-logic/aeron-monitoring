plugins {
    java
}

val jar: Jar by tasks
jar.baseName = "aeron-monitoring-core"

val aeronVersion = "1.11.1"
val junitVersion = "5.3.1"
val lombokVersion = "1.18.2"


dependencies {
    compile("io.aeron:aeron-client:$aeronVersion")
    compile("io.aeron:aeron-driver:$aeronVersion")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}