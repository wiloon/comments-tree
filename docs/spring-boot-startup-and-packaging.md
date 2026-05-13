# Spring Boot Startup and Packaging

## 1. Application Entry Point

`CommentsTree.main()` is the entry point of the entire Spring Boot application.

```java
@SpringBootApplication
public class CommentsTree {
    public static void main(String[] args) {
        SpringApplication.run(CommentsTree.class, args);
    }
}
```

The `main` method can be placed in any class in the project. Spring does not care which file or package it lives in.

---

## 2. What `SpringApplication.run(CommentsTree.class)` Means

Passing `CommentsTree.class` tells Spring Boot two things:

1. **Component scan root** — Spring scans recursively from the package where `CommentsTree` lives (`com.wiloon.comments`), registering all `@Component`, `@Service`, `@Repository`, `@Controller`, etc.
2. **Annotation configuration source** — `@SpringBootApplication` is on this class, so Spring Boot reads auto-configuration and `@Bean` methods from it.

> Convention: place the entry class at the top-level package, annotate it with `@SpringBootApplication`, and pass it to `run()` — all three in one place.

---

## 3. `@EnableAutoConfiguration`

This project does not use `@EnableAutoConfiguration` directly. It is already included inside `@SpringBootApplication` and does not need to be declared separately.

---

## 4. How `java -jar` Locates the Entry Class

The JVM reads `META-INF/MANIFEST.MF` inside the jar to find the entry point. The location of the source file is irrelevant.

The Spring Boot Maven Plugin writes the following into the manifest at build time:

```
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: com.wiloon.comments.CommentsTree
```

The entry class is declared explicitly in `pom.xml` via `<start-class>`:

```xml
<properties>
    <start-class>com.wiloon.comments.CommentsTree</start-class>
</properties>
```

The plugin reads this value and writes it into `MANIFEST.MF`. When `java -jar` is run, the JVM looks here first.

---

## 5. Purpose of the META-INF Directory

`META-INF` is the metadata directory of a jar file. It solves a fundamental problem: **how does the JVM or a framework know what this jar is and how to use it?**

| File                                                                      | Purpose                                                                            |
| ------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| `MANIFEST.MF`                                                             | Declares the startup class, Class-Path, and other basic metadata                   |
| `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | Lists auto-configuration classes; this is how Spring Boot's zero-config works      |
| `services/`                                                               | Java SPI mechanism; JDBC drivers, logging implementations, etc. self-register here |

`META-INF` turns a jar from a "dumb archive" into a **self-describing module**.

---

## 6. History of MANIFEST.MF

- **Specification**: Java JAR File Specification, part of the Java SE standard maintained by Oracle
- **Introduced**: 1997, with **JDK 1.1**
- The jar format is based on ZIP; `META-INF/MANIFEST.MF` was mandated by Sun in the original jar specification

| Year  | Event                                                                              |
| ----- | ---------------------------------------------------------------------------------- |
| 1996  | JDK 1.0 — no jar format yet                                                        |
| 1997  | JDK 1.1 — jar and `MANIFEST.MF` introduced                                         |
| 1999  | JDK 1.2 — extended with signing, sealed packages, etc.                             |
| Today | Core structure unchanged; frameworks like Spring Boot add custom attributes on top |

---

## 7. Full Startup Flow of a Spring Boot Fat Jar

The JVM does **not** extract the jar to disk first. It reads directly from the ZIP structure using random access.

### ZIP File Structure

```
[Entry 1 data]        <- compressed content of MANIFEST.MF
[Entry 2 data]        <- JarLauncher.class
[Entry 3 data]        <- application .class files
...
[Central Directory]         <- name + byte offset for every entry
[End of Central Directory]  <- points to Central Directory; always at the end
```

ZIP stores its directory at the **end of the file**, enabling random access without decompressing the entire archive. This is the key advantage over tar.gz, which is a sequential stream and requires reading from the beginning.

### Startup Steps

```
① Seek to end of file, read End of Central Directory
      ↓
② Jump to Central Directory offset, read the index
      ↓
③ Look up META-INF/MANIFEST.MF, get its byte offset
      ↓
④ Jump to that offset, decompress and read MANIFEST.MF
      ↓
⑤ Extract Main-Class = JarLauncher, load and execute it
      ↓
⑥ JarLauncher scans BOOT-INF/lib/ for nested jars,
   sets up a custom ClassLoader for them
      ↓
⑦ Load Start-Class = CommentsTree, invoke main()
      ↓
⑧ Spring container starts: scans beans, starts embedded Jetty
```

`.class` files are loaded **lazily** — only when a class is first needed. The entire process happens in memory; the jar on disk remains compressed throughout.

---

## 8. Build Commands

The project supports two build modes, configured in `Taskfile.yml`:

| Command           | Description                                | Use case                    |
| ----------------- | ------------------------------------------ | --------------------------- |
| `task build`      | Backend jar only, skips frontend and tests | Development, fast iteration |
| `task build-full` | Frontend and backend bundled into one jar  | Quick deployment, demos     |

### Production Deployment Strategies

**Small projects / internal tools**: bundle frontend and backend into a single jar — simple to deploy, one artifact.

**Medium to large projects (mainstream)**: deploy separately
- Frontend: Nginx container serving static files and proxying API requests
- Backend: Spring Boot jar in its own container
- Benefits: independent release cycles, CDN for frontend, horizontal scaling for backend
