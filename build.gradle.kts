import com.github.spotbugs.SpotBugsTask
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath("org.eclipse.jgit:org.eclipse.jgit:5.1.2.201810061102-r")
  }
}

plugins {
  checkstyle
  java
  `maven-publish`
  id("com.github.ben-manes.versions") version "0.20.0"
  id("com.github.spotbugs") version "1.6.5"
}

repositories {
  jcenter()
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
  testImplementation("org.junit.vintage:junit-vintage-engine:${Versions.junit}")
}

version = generateVersion()
group = "org.openstreetmap.josm"

object Versions {
  val junit = "5.3.1"
  val spotbugs = "3.1.8"
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
      project.afterEvaluate {
        groupId = project.group.toString()
        artifactId = "jmapviewer"
        version = project.version.toString()
      }
      from(components.getByName("java"))
      artifact(sourcesJar)
      artifact(javadocJar)
    }
  }
}

checkstyle {
  toolVersion = "8.13"
  this.config = resources.text.fromFile(project.file("tools/checkstyle/jmapviewer_checks.xml"))
}

spotbugs {
  toolVersion = Versions.spotbugs
  isIgnoreFailures = true
  sourceSets = setOf(project.sourceSets.getByName("main"))
  effort = "max"
}
tasks.withType(SpotBugsTask::class) {
  reports {
    xml.isEnabled = false
    html.isEnabled = true
  }
}

fun generateVersion(): String {
  val git = Git(FileRepositoryBuilder().setWorkTree(projectDir).readEnvironment().findGitDir().build())
  val describe = git.describe().call()
  return describe ?:
    git.repository.newObjectReader().abbreviate(
      git.log().setMaxCount(1).call().first().toObjectId()
    ).name()
}