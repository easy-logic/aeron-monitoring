buildscript {
	val springBootVersion = "2.0.5.RELEASE"
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
	}
}


plugins {
    java
}
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

val jar: Jar by tasks
jar.baseName = "aeron-monitoring-http"


dependencies {
    compile(project(":core"))
	compile("org.springframework.boot:spring-boot-starter-webflux")
	testCompile("org.springframework.boot:spring-boot-starter-test")
	testCompile("io.projectreactor:reactor-test")
}
