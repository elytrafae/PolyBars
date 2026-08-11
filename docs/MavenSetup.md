# Adding PolyBars to Your Gradle Project

PolyBars is published on [Modrinth's Maven Repository](https://support.modrinth.com/en/articles/8801191-modrinth-maven), allowing you to easily add it as a dependency in your Fabric mod project.

---

## 1. Add Modrinth's Maven Repository

Add the Modrinth Maven repository to your `repositories` block (if you haven't already)

### Groovy DSL (`build.gradle`)

```groovy
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
}
```

### Kotlin DSL (`build.gradle.kts`)

```kotlin
repositories {
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}
```

---

## 2. Add PolyBars as a Dependency

Add PolyBars to your `dependencies` block using `modImplementation`.

### Groovy DSL (`build.gradle`)

```groovy
dependencies {
    // Replace with your desired version (e.g. 1.0.0+26.2)
    implementation "maven.modrinth:polybars:1.0.0+26.2"
}
```

### Kotlin DSL (`build.gradle.kts`)

```kotlin
dependencies {
    // Replace with your desired version (e.g. 1.0.0+26.2)
    implementation("maven.modrinth:polybars:1.0.0+26.2")
}
```
