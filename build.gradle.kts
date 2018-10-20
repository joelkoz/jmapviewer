plugins {
  java
  `maven-publish`
}

repositories {
  jcenter()
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
  testImplementation("org.junit.vintage:junit-vintage-engine:${Versions.junit}")
}

object Versions {
  val junit = "5.3.1"
}
val demoClass = "org.openstreetmap.gui.jmapviewer.Demo"
val demoClassFilePattern = demoClass.replace('.', '/').plus(".java")
val imagesFilePattern = "org/openstreetmap/gui/jmapviewer/images/**"

sourceSets {
  getByName("main") {
    java {
      setSrcDirs(setOf("src"))
      exclude(demoClassFilePattern)
      exclude(imagesFilePattern)
    }
    resources {
      setSrcDirs(setOf("src"))
      exclude(demoClassFilePattern)
      include(imagesFilePattern)
    }
  }
  getByName("test") {
    java {
      setSrcDirs(setOf("test"))
    }
  }
  create("demo") {
    java {
      setSrcDirs(setOf("src"))
      include(demoClassFilePattern)
      compileClasspath = sourceSets.getByName("main").output
    }
  }
}

tasks.withType(JavaCompile::class) {
  options.compilerArgs = options.compilerArgs.plus(arrayOf("-Xlint:all", "-Xlint:-serial"))
}

tasks.create("runDemo", JavaExec::class) {
  project.afterEvaluate {
    classpath = classpath
        .plus(sourceSets.getByName("demo").output)
        .plus(sourceSets.getByName("main").output)
  }
  setMain(demoClass)
}

val sourcesJar = tasks.create("sourcesJar", Jar::class) {
  classifier = "src"
  from(sourceSets.getByName("main").allSource)
}

val javadocJar = tasks.create("javadocJar", Jar::class) {
  classifier = "javadoc"
  from(tasks.withType(Javadoc::class).getByName("javadoc").destinationDir)
}

artifacts {
  add(configurations.archives.name, sourcesJar)
  add(configurations.archives.name, javadocJar)
}

publishing {
  repositories {
    maven("$buildDir/maven") {
      name = "buildDir"
    }
    maven("https://josm.openstreetmap.de/nexus/content/repositories/releases/") {
      name = "Nexus"
    }
  }
  publications {
    create("maven", MavenPublication::class) {
      groupId = "org.openstreetmap.josm"
      artifactId = "jmapviewer"
      version = "1.0.0"
      from(components.getByName("java"))
      artifact(sourcesJar)
      artifact(javadocJar)
    }
  }
}