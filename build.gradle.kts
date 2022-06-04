plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

// gradlew publishToSonatype closeSonatypeStagingRepository for staging and manual release
// gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository for automatic release

val versionObject = Version(breaking = "1", minor = "0", nonbreaking = "0", revision = "0", date = "2206")
project.group = "com.riskrieg"
project.version = "$versionObject"

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.github.spotbugs:spotbugs-annotations:4.6.0")

    implementation("com.riskrieg:map:1.0.0-0.2206")
    implementation("com.riskrieg:palette:1.0.0-0.2206")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

fun getProjectProperty(name: String) = project.properties[name] as? String

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    failFast = true
}

tasks.jar {
    archiveBaseName.set(project.name)
    manifest.attributes(
        mapOf(
            "Implementation-Version" to project.version,
            "Automatic-Module-Name" to "com.riskrieg.codec"
        )
    )
    includeEmptyDirs = false
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

class Version(
    val breaking: String,
    val minor: String,
    val nonbreaking: String,
    val revision: String,
    val date: String,
    val classifier: String? = null
) {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Version) return false
        return breaking == other.breaking
                && minor == other.minor
                && nonbreaking == other.nonbreaking
                && revision == other.revision
                && date == other.date
                && classifier == other.classifier
    }

    override fun hashCode(): Int {
        var result = breaking.hashCode()
        result = 31 * result + minor.hashCode()
        result = 31 * result + nonbreaking.hashCode()
        result = 31 * result + revision.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + (classifier?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "$breaking.$minor.$nonbreaking-$revision.$date" + if (classifier != null) "-$classifier" else ""
    }
}

/* Maven Central Publishing and Signing */

fun generatePom(pom: MavenPom) {
    pom.packaging = "jar"
    pom.name.set(project.name)
    pom.description.set("Java codecs for the various Riskrieg file formats: https://riskrieg.com")
    pom.url.set("https://github.com/Riskrieg/codec")
    pom.scm {
        url.set("https://github.com/Riskrieg/codec")
        connection.set("scm:git:git://github.com/Riskrieg/codec.git")
        developerConnection.set("scm:git:ssh:git@github.com:Riskrieg/codec.git")
    }
    pom.licenses {
        license {
            name.set("MIT License")
            url.set("https://github.com/Riskrieg/codec/blob/main/LICENSE")
            distribution.set("repo")
        }
    }
    pom.developers {
        developer {
            id.set("aaronjyoder")
            name.set("Aaron J Yoder")
            email.set("aaronjyoder@gmail.com")
        }
    }
}

// Publish

// Skip fat jar publication (https://github.com/johnrengelman/shadow/issues/586)
components.java.withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) { skip() }
val SoftwareComponentContainer.java
    get() = components.getByName("java") as AdhocComponentWithVariants

publishing {
    publications {
        register("Release", MavenPublication::class) {
            from(components["java"])

            artifactId = project.name
            groupId = project.group as String
            version = project.version as String

            generatePom(pom)
        }
    }
}

val canSign = getProjectProperty("signing.keyId") != null
if (canSign) {
    signing {
        sign(publishing.publications.getByName("Release"))
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}