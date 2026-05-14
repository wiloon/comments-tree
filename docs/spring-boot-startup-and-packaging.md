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

The plugin auto-detects the class with `public static void main(String[] args)` at build time and writes it into `MANIFEST.MF`. When `java -jar` is run, the JVM reads this file to find the entry point.

> `<start-class>` in `pom.xml` is optional. If omitted, the plugin scans the compiled classes and picks the one annotated with `@SpringBootApplication` that contains a `main` method.

---

## 5. Purpose of the META-INF Directory

`META-INF` is the metadata directory of a jar file. It solves a fundamental problem: **how does the JVM or a framework know what this jar is and how to use it?**

| File                                                                      | Purpose                                                                            |
| ------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| `MANIFEST.MF`                                                             | Declares the startup class, Class-Path, and other basic metadata                   |
| `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | Lists auto-configuration classes; this is how Spring Boot's zero-config works      |
| `services/`                                                               | Java SPI mechanism; JDBC drivers, logging implementations, etc. self-register here |

`META-INF` turns a jar from a "dumb archive" into a **self-describing module**.

### MANIFEST.MF Fields

| Field              | Description                                              |
| ------------------ | -------------------------------------------------------- |
| `Manifest-Version` | Manifest format version, typically `1.0`                 |
| `Main-Class`       | Entry class executed by the JVM when running `java -jar` |
| `Class-Path`       | External jar paths required at runtime                   |
| `Start-Class`      | Spring Boot specific — the actual application main class |

Format rules:
- Each line is `Key: Value` (one space after the colon)
- Maximum **72 bytes** per line; longer values must be continued on the next line with a leading space
- The file must end with a **blank line**, or the last attribute will be silently ignored

### META-INF Contents of This Project's Fat Jar

```
META-INF/
├── MANIFEST.MF
├── maven/
│   └── com.wiloon.demo/comments-tree-api/
│       ├── pom.xml           <- original pom copied in at build time
│       └── pom.properties    <- groupId / artifactId / version for quick lookup
└── services/
    └── java.nio.file.spi.FileSystemProvider   <- Java SPI registration
```

**`maven/…/pom.xml` and `pom.properties`**: embedded by Maven at package time. Used by tools like `mvn dependency:tree` to identify artifacts on the classpath, and by code that reads project metadata at runtime.

**`services/`**: Java SPI registration directory (see section below).

### Spring Boot Specific META-INF Files (inside dependency jars)

| File                                                                      | Purpose                                                             |
| ------------------------------------------------------------------------- | ------------------------------------------------------------------- |
| `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | Lists all auto-configuration classes; the foundation of zero-config |
| `spring.factories` (legacy)                                               | Used in Spring Boot 2.x; migrated to the file above in 3.x          |
| `spring-configuration-metadata.json`                                      | Metadata for IDE auto-completion of `application.properties`        |

---

## 6. Java SPI (Service Provider Interface)

SPI is Java's built-in plugin mechanism. It allows third parties to provide interface implementations without modifying the caller's code.

### Core Idea

```
Caller depends only on the interface → implementations are discovered at runtime by ServiceLoader
```

### Three Components

| Component             | Description                                                                    |
| --------------------- | ------------------------------------------------------------------------------ |
| **Interface**         | Defined by the framework or standard library (e.g., `java.sql.Driver`)         |
| **Implementation**    | Provided by a third-party library (e.g., `com.mysql.cj.jdbc.Driver`)           |
| **Registration file** | `META-INF/services/<interface-FQCN>`, content is the implementation class FQCN |

### How It Works

```
① Caller: ServiceLoader.load(Driver.class)
      ↓
② JVM scans all jars on the classpath for
   META-INF/services/java.sql.Driver
      ↓
③ Reads implementation class names from the file, instantiates via reflection
      ↓
④ Returns an iterator over all implementations
```

### Registration File Example

File path: `META-INF/services/java.sql.Driver`
```
com.mysql.cj.jdbc.Driver
```

### Common Use Cases

| Interface                              | Implementation (third-party jar) |
| -------------------------------------- | -------------------------------- |
| `java.sql.Driver`                      | MySQL, PostgreSQL drivers        |
| `org.slf4j.spi.SLF4JServiceProvider`   | Logback, Log4j2                  |
| `java.nio.file.spi.FileSystemProvider` | zip, nested jar file systems     |
| `javax.crypto.JceSecurity`             | BouncyCastle crypto provider     |

Since Java 6, JDBC drivers self-register via SPI — `Class.forName("com.mysql.Driver")` is no longer needed.

### `NestedFileSystemProvider` in This Project

The SPI file `META-INF/services/java.nio.file.spi.FileSystemProvider` in this project's fat jar contains:

```
org.springframework.boot.loader.nio.file.NestedFileSystemProvider
```

The standard JVM cannot represent a path inside a jar that is itself inside another jar. `NestedFileSystemProvider` registers a new `nested:` URI scheme with the JVM, enabling paths like:

```
nested:/path/to/app.jar/!BOOT-INF/lib/spring-core-6.x.jar
```

This lets `JarLauncher` open each nested jar in `BOOT-INF/lib/` as a proper file system and load classes from it — the technical foundation that allows the fat jar to run without being extracted.

---

## 7. History of MANIFEST.MF

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

## 8. Fat Jar

A fat jar (also called uber jar) packages all dependencies into a single jar file, allowing the application to run with a single command.

### Plain Jar vs Fat Jar

**Plain jar** (default `mvn package` output):
```
app.jar
└── com/wiloon/comments/CommentsTree.class
    (no dependencies included)
```
Running requires a manual classpath:
```bash
java -cp app.jar:lib/spring-boot-3.x.jar:lib/... com.wiloon.comments.CommentsTree
```

**Fat jar** (Spring Boot Maven Plugin output):
```
app.jar
├── META-INF/MANIFEST.MF
├── BOOT-INF/
│   ├── classes/     <- application .class files
│   └── lib/         <- all dependency jars, kept intact
└── org/springframework/boot/loader/
    └── JarLauncher.class
```
Running requires only:
```bash
java -jar app.jar
```

### Why Not Unpack All Dependencies Into One Layer

An alternative approach (used by Maven Shade Plugin) is to extract all dependency class files and merge them flat:

```
app.jar
├── com/wiloon/...        <- own classes
├── org/springframework/  <- Spring classes (unpacked)
└── com/fasterxml/...     <- Jackson classes (unpacked)
```

This approach has significant drawbacks:

| Problem                    | Description                                                                                                |
| -------------------------- | ---------------------------------------------------------------------------------------------------------- |
| **File conflicts**         | Multiple jars contain identically named files (`LICENSE`, `META-INF/spring.factories`) — only one survives |
| **SPI broken**             | Same-named files under `META-INF/services/` overwrite each other, losing implementations                   |
| **Signatures invalidated** | Unpacking a signed jar breaks its signature                                                                |
| **Poor traceability**      | Cannot determine which dependency version a class came from                                                |

Spring Boot keeps dependency jars intact inside `BOOT-INF/lib/` and uses `NestedFileSystemProvider` plus a custom `ClassLoader` to read them, avoiding all of the above.

### Pros and Cons

| Pros                                               | Cons                                                     |
| -------------------------------------------------- | -------------------------------------------------------- |
| Single-file deployment, no dependency installation | Large file size (typically 30–50 MB)                     |
| One-line startup with `java -jar`                  | Small changes require re-uploading the entire jar        |
| Self-contained, consistent across environments     | Not ideal when multiple apps share the same dependencies |

---

## 9. JDK-Level Alternatives to Fat Jar

### `jlink` — JDK 9 (2017)

The Java Platform Module System introduced `jlink`, which bundles the application and only the required JDK modules into a **self-contained runtime image**:

```bash
jlink --module-path mods \
      --add-modules com.wiloon.comments \
      --output dist/
```

The output includes a trimmed JVM — the target machine does not need Java installed. In practice, most libraries (including Spring Boot) are not fully modularized, making `jlink` difficult to use on real projects.

### `jpackage` — JDK 14 (2020)

Wraps `jlink` output into a native installer:

| Platform | Output          |
| -------- | --------------- |
| macOS    | `.dmg` / `.pkg` |
| Windows  | `.msi` / `.exe` |
| Linux    | `.deb` / `.rpm` |

Suitable for desktop applications (JavaFX). Not suitable for server-side services.

### GraalVM Native Image

Compiles Java bytecode to a **native binary** ahead of time:

```bash
native-image -jar app.jar -o app
./app   # runs without a JVM, starts in milliseconds
```

| Feature      | Description                                                                     |
| ------------ | ------------------------------------------------------------------------------- |
| Startup time | Milliseconds (fat jar typically takes 3–10 seconds)                             |
| Memory usage | Significantly reduced                                                           |
| Trade-offs   | Reflection and dynamic proxies require extra configuration; compilation is slow |

Spring Boot 3.x officially supports Native Image. This is enabled by **Spring AOT** (see next section).

### Current State

| Approach     | Status                                                             |
| ------------ | ------------------------------------------------------------------ |
| Fat jar      | Most mature, production mainstream; requires JRE on target machine |
| `jlink`      | Ecosystem not ready; rarely used with Spring Boot                  |
| `jpackage`   | Desktop application use case                                       |
| Native Image | Cloud-native trend; Spring Boot 3.x actively investing here        |

---

## 10. Spring AOT and GraalVM Native Image

They are related but distinct — they work together:

```
GraalVM Native Image (compiler, provided by Oracle/GraalVM)
        ↑
        │ requires all classes/reflection/proxy info at compile time
        │
Spring AOT (provided by Spring Framework 6 / Boot 3)
        │ pre-processes the Spring container at mvn package time
        │ generates static code, eliminating runtime reflection
        ↓
Output: native binary
```

**GraalVM Native Image** is a compiler tool that statically compiles Java bytecode into a native binary. All class loading is resolved at compile time; no JIT, no dynamic class loading at runtime. The problem: Spring relies heavily on reflection, dynamic proxies, and runtime classpath scanning — Native Image cannot see these by default.

**Spring AOT** (Ahead-Of-Time Processing) is a pre-processing phase added in Spring Boot 3. During `mvn package` it expands Bean definitions, conditional evaluations, and proxy generation into plain Java code, and generates hint files (`reflect-config.json`, `proxy-config.json`) that tell Native Image about all classes Spring uses.

|                          | What it is                                              | Provided by      |
| ------------------------ | ------------------------------------------------------- | ---------------- |
| **GraalVM Native Image** | Compiler: bytecode → native binary                      | Oracle / GraalVM |
| **Spring AOT**           | Pre-processor: makes Spring transparent to Native Image | Spring team      |

Spring AOT exists to make GraalVM Native Image capable of compiling Spring applications. Attempting to compile an unprocessed Spring application with Native Image directly will almost certainly fail at startup.

### History: "Spring Native" → Spring Boot 3

| Phase               | Name                                                    | Period           |
| ------------------- | ------------------------------------------------------- | ---------------- |
| Experimental        | **Spring Native** (standalone project)                  | 2021–2022        |
| Generally Available | **Spring Boot 3.x built-in AOT + Native Image support** | Nov 2022 onwards |

"Spring Native" as a separate project name is now largely obsolete. The functionality is built into Spring Boot 3 and is simply referred to as **Native Image support** or **Spring AOT**.

### Build Command

```bash
mvn -Pnative native:compile
```

This triggers Spring AOT processing followed by GraalVM `native-image` compilation. The output is a standalone Linux ELF binary that runs without a JVM.

### Runtime Structure Comparison

|                           | Fat Jar (JVM)                          | Native Image                             |
| ------------------------- | -------------------------------------- | ---------------------------------------- |
| **Bytecode interpreter**  | ✅ HotSpot interprets `.class` files    | ❌ All code is pre-compiled machine code  |
| **JIT compiler**          | ✅ C1/C2 optimizes hot paths at runtime | ❌ None — AOT compiled at build time      |
| **Dynamic class loading** | ✅                                      | ❌ All classes determined at compile time |
| **GC**                    | HotSpot GC (G1, ZGC, etc.)             | Substrate VM GC (Serial GC or G1)        |
| **Startup time**          | 3–10 seconds                           | 50–200 milliseconds                      |
| **Memory**                | 200–500 MB                             | 50–100 MB                                |
| **Executable**            | `java -jar app.jar`                    | `./app`                                  |

```
Fat Jar runtime:
┌──────────────────────────────────────┐
│  Spring app + Spring Framework       │  ← Java bytecode
│  JDK standard library                │
├──────────────────────────────────────┤
│  JVM (HotSpot): GC, JIT, class load  │  ← libjvm.so
├──────────────────────────────────────┤
│  OS                                  │
└──────────────────────────────────────┘

Native Image runtime:
┌──────────────────────────────────────┐
│  Spring app + Spring Framework       │  ← machine code (AOT compiled)
│  JDK standard library (used parts)  │
├──────────────────────────────────────┤
│  Substrate VM: GC + thread mgmt only │  ← no bytecode interpreter, no JIT
├──────────────────────────────────────┤
│  OS                                  │
└──────────────────────────────────────┘
```

**Substrate VM** is the minimal runtime bundled inside the native binary by GraalVM. It handles GC and thread management but is not a bytecode execution engine. It is analogous to Go's runtime: Go programs also embed a runtime (for GC and goroutine scheduling), but no one calls Go a "virtual machine" language.

---

## 11. Full Startup Flow of a Spring Boot Fat Jar

The JVM does **not** extract the jar to disk first. It reads directly from the ZIP structure using random access.

### Step 1: OS starts the JVM process

```
java -jar app.jar
  → OS forks a new process
  → loads libjvm.so into memory
  → JVM initializes heap, stack, method area
```

`libjvm.so` is the true body of the JVM — see [Section 14](#14-libjvmso--the-jvm-core-body) for details. The `java` executable itself is only a tiny launcher whose job is to find and `dlopen` this file.

The jar file has not been read yet at this point — it is just a path argument.

### Step 2: JVM opens the jar and locates MANIFEST.MF via ZIP random access

A jar is a ZIP file. ZIP stores its Central Directory at the **end** of the file, so the JVM does not need to read from the beginning:

```
① seek to end of file → read End of Central Directory (EOCD, fixed format)
      ↓
② read Central Directory offset from EOCD → seek to it
      ↓
③ find "META-INF/MANIFEST.MF" entry in the directory → get its byte offset
      ↓
④ seek to that offset → decompress (Deflate) → read MANIFEST.MF content
```

Only **3 seeks** are needed. No other files are touched.

### Step 3: Parse MANIFEST.MF, load JarLauncher

```
Main-Class: org.springframework.boot.loader.JarLauncher
```

`JarLauncher.class` lives at the top level of the fat jar (not inside `BOOT-INF/`), so the standard JVM ClassLoader can find and load it directly. Its `main()` is then invoked.

### Step 4: JarLauncher mounts nested jars

`JarLauncher` uses `NestedFileSystemProvider` (registered via SPI) to mount every jar inside `BOOT-INF/lib/` as a virtual file system, and sets up a custom `ClassLoader` to load classes from them.

### Step 5: Load Start-Class and start Spring

```
Start-Class: com.wiloon.comments.CommentsTree
```

`CommentsTree.main()` is invoked via reflection → `SpringApplication.run()` creates and refreshes an `ApplicationContext` → embedded Jetty begins listening.

The Spring startup inside `run()` has three distinct phases:

| Phase | What happens | When |
|-------|-------------|------|
| **Class loading** | JarLauncher builds the ClassLoader; `.class` files are lazy-loaded into JVM | Before `refresh()` |
| **Bean definition scanning** | Spring uses ASM to read bytecode metadata; `@Component` etc. are registered as `BeanDefinition` (no instantiation yet) | `refresh()` early — `invokeBeanFactoryPostProcessors()` |
| **Bean instantiation** | Constructors are called in dependency order; dependencies are injected | `refresh()` late — `finishBeanFactoryInitialization()` |

Scanning uses ASM to analyze bytecode **without loading classes into the JVM**, so classes that are not needed never trigger static initialization.

### Full sequence

```
java -jar app.jar
  ↓ OS: fork + execve(java)             — shell's child process becomes the java launcher
  ↓ java launcher: dlopen(libjvm.so)   — JVM main body enters memory
  ↓ JVM: heap / GC / JIT initialized
  ↓ JVM: 3 seeks into ZIP → MANIFEST.MF (no extraction)
  ↓ JVM: load JarLauncher (top-level class, standard ClassLoader)
  ↓ JarLauncher: mount BOOT-INF/lib/ nested jars via NestedFileSystemProvider
  ↓ JarLauncher: create custom ClassLoader
  ↓ JarLauncher: invoke CommentsTree.main() via reflection
  ↓ SpringApplication.run()
      → create ApplicationContext
      → context.refresh()
          → invokeBeanFactoryPostProcessors()  ← scan .class, register BeanDefinitions
          → registerBeanPostProcessors()       ← AOP, annotation processors
          → finishBeanFactoryInitialization()  ← instantiate all singleton Beans
          → finishRefresh()                    ← publish ContextRefreshedEvent, start Jetty
```

`.class` files are loaded **lazily** — only when a class is first needed. The jar on disk remains compressed throughout the entire runtime.

### ZIP Internal Structure

Each file inside a ZIP is **independently compressed** — it has its own compressed block and its own entry in the Central Directory:

```
┌─────────────────────────────────┐  ← file start
│  Local Header + compressed data │  entry 1: MANIFEST.MF  (Deflate, 312 bytes)
├─────────────────────────────────┤
│  Local Header + compressed data │  entry 2: JarLauncher.class
├─────────────────────────────────┤
│  Local Header + compressed data │  entry 3: CommentsTree.class
├─────────────────────────────────┤
│  ...                            │
├─────────────────────────────────┤
│  Central Directory              │  index: name + byte offset for every entry
├─────────────────────────────────┤
│  End of Central Directory       │  ← file end, points to Central Directory
└─────────────────────────────────┘
```

The Central Directory entry for a single file looks like:
```
filename    = "META-INF/MANIFEST.MF"
offset      = 0x00001a40    <- byte position of this entry in the file
compressed  = 312 bytes
uncompressed= 580 bytes
method      = Deflate
```

Reading a single file: `seek(0x00001a40)` → read 312 bytes → Deflate decompress → get 580 bytes. The rest of the archive is never touched.

---

## 13. Archive Format Internals and Compression

### ZIP vs tar.gz vs 7z/RAR

| Format     | Compression style                                          | Random access                     | Compression ratio              |
| ---------- | ---------------------------------------------------------- | --------------------------------- | ------------------------------ |
| **ZIP**    | Each file **independently** compressed                     | ✅ Yes — seek to offset            | Average                        |
| **tar.gz** | All files merged into one stream, then compressed          | ❌ No — must decompress from start | Good                           |
| **7z**     | **Solid** compression by default — all files in one stream | ❌ No in Solid mode                | High (30–50% smaller than ZIP) |
| **RAR**    | Solid by default, can be disabled                          | ❌ No in Solid mode                | High                           |

**Solid compression**: files are concatenated before compression, so the compressor finds repeated patterns across files (e.g., many similar `.class` files). Better ratio, but to extract file #100 you must decompress files #1–#99 first.

**Why JAR must use ZIP**: the JVM lazy-loads `.class` files one at a time. Solid compression would require decompressing the entire archive for every class load — unusable in practice.

### Why ZIP Is Still Dominant After 35 Years

1. **Open spec since 1991** — no patent barriers; all platforms implement it natively
2. **Windows Explorer and macOS Finder open it as a folder** — zero user friction
3. **ZIP in disguise is everywhere**: `.jar`, `.apk`, `.docx`, `.xlsx`, `.epub`, `.war` are all ZIP files with different extensions
4. **Random access** satisfies the most common use case (open one file from an archive) well enough

No single replacement simultaneously matches ZIP on universality, random access, and ecosystem compatibility. Alternatives dominate in specific niches but cannot displace ZIP overall.

### ZIP Specification Evolution

The ZIP spec is maintained by PKWARE as **APPNOTE.TXT**, latest version **6.3.10 (2022)**. It is freely available and has been since 1991.

Recent additions (around 2020):

| Method ID | Algorithm            | Added |
| --------- | -------------------- | ----- |
| 93        | **Zstandard (zstd)** | ~2020 |
| 95        | XZ                   | ~2020 |
| 98        | Brotli               | ~2020 |

This means a ZIP file can in principle contain zstd-compressed entries, gaining higher compression while keeping per-file random access. In practice, **Windows Explorer, macOS Finder, and most unzip tools do not support these methods yet** — the spec moves faster than the ecosystem.

This is a pattern common to all dominant standards: backward compatibility with a massive installed base means new features take a decade to actually propagate.

### zstd and Random Access

Standard zstd is a **streaming compression algorithm**, not an archive format. `.tar.zst` = tar (collects files) + zstd (compresses the whole stream) — same sequential limitation as tar.gz.

zstd has an official extension called **Zstandard Seekable Format**, which splits data into independent Frames with a Seek Table at the end:

```
[Frame 0] [Frame 1] [Frame 2] ... [Seek Table]
  ↑ each Frame is independently compressed
```

This enables random access by seeking to the relevant Frame. However, this format has almost no mainstream tool support — it remains a niche extension.

### The Convergence

ZIP and zstd are borrowing from each other:

```
ZIP  ← adopts zstd compression (better ratio, keeps random access)
zstd ← adds Seekable extension (gains random access, like ZIP)
```

Both are converging toward the same goal: **high compression + random access**. The tension between these two properties is fundamental — higher ratio requires larger compression contexts (Solid), random access requires smaller independent blocks. The only way to satisfy both is to pick a block size in between.

Formats designed from scratch for this trade-off:

| Format                   | Block size                | Use case                                   |
| ------------------------ | ------------------------- | ------------------------------------------ |
| **EROFS** (Linux kernel) | 4 KB–1 MB, zstd per block | Android system images, container layers    |
| **SquashFS + zstd**      | Configurable blocks       | Embedded Linux, container images           |
| **ZIP + zstd (new)**     | Per file                  | General archive (not yet widely supported) |

These formats occupy different niches and are unlikely to replace ZIP in general file exchange — but they represent the direction for systems where startup time and memory matter (containers, embedded devices, cloud-native).

---

## 12. Build Commands

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

---

## 14. libjvm.so — The JVM Core Body

`libjvm.so` is the true body of the JVM. The `java` executable (`/usr/bin/java`) is a tiny launcher — its only job is to parse arguments, locate the JRE installation path, and call `dlopen("libjvm.so")`.

```
java -jar app.jar
  ↓
java executable  (tiny launcher, a few KB)
  → finds $JAVA_HOME/lib/server/libjvm.so
  → dlopen(libjvm.so)   ← JVM enters memory here
  ↓
JVM initializes: heap, stack, method area, GC, JIT compiler
  ↓
reads MANIFEST.MF → loads JarLauncher → ...
```

`libjvm.so` lives at `$JAVA_HOME/lib/server/libjvm.so` and is typically the largest single file in the JDK installation (tens of MB).

### What libjvm.so Contains

| Component            | Description                                   |
| -------------------- | --------------------------------------------- |
| Bytecode interpreter | Executes `.class` bytecode                    |
| JIT compilers        | C1 (client compiler), C2 (server compiler)    |
| GC implementations   | Serial, Parallel, G1, ZGC, Shenandoah         |
| Class loader         | C++ implementation of Java's `ClassLoader`    |
| Runtime              | Thread management, locking, memory management |

---

## 15. JVM Implementation Language

HotSpot (the JVM shipped with OpenJDK and Oracle JDK) is written primarily in **C++**, with some C and platform-specific assembly.

| Component                                     | Language             |
| --------------------------------------------- | -------------------- |
| Interpreter, GC, class loading                | C++                  |
| OS-level primitives (threads, memory mapping) | C                    |
| JIT-generated machine code templates (Stubs)  | Assembly (x86 / ARM) |

This is a natural choice: managing raw memory, manipulating CPU registers, and making OS system calls are exactly what C/C++ is designed for. Java cannot implement its own VM because running any Java program already requires a pre-existing JVM — a chicken-and-egg problem.

The JDK standard library (`java.lang.*`, `java.util.*`, etc.) is written in Java, but any operation that touches the OS (file I/O, networking, threads) ultimately calls a `native` method implemented in C++ inside `libjvm.so` or a companion `.so`.

### Other JVM Implementations

| Implementation                 | Language                                               |
| ------------------------------ | ------------------------------------------------------ |
| HotSpot (OpenJDK / Oracle)     | C++                                                    |
| GraalVM (JIT compiler "Graal") | Java (the compiler itself) — VM substrate is still C++ |
| Android ART                    | C++                                                    |
| JikesRVM (research)            | Java (self-hosted, but requires a C bootstrap)         |

---

## 16. Self-Bootstrapping: Java vs Go vs Rust

"Self-bootstrapping" (self-hosting) means a language's own compiler is written in that language and can compile itself.

### Java — Partial

| Layer              | Implementation language | Self-bootstrapping            |
| ------------------ | ----------------------- | ----------------------------- |
| `javac` (compiler) | Java                    | ✅ `javac` can compile `javac` |
| JVM (runtime)      | C++                     | ❌ requires a C++ toolchain    |

`javac` is self-hosting. The JVM is not and cannot be — running `javac` already requires a pre-existing JVM. The root cause is not that Java is "interpreted", but that no Java program can run without a JVM, and the JVM itself is C++.

### Go — Fully Self-Hosted (since Go 1.5, 2015)

- Go 1.4 and earlier: compiler written in C
- **Go 1.5**: compiler fully rewritten in Go; completely self-hosting
- Bootstrap chain: Go 1.4 (C-compiled) compiles Go 1.5; each subsequent version compiles the next

Go compiles to native machine code with no separate VM, so it can fully escape the C dependency.

### Rust — Fully Self-Hosted

- Early `rustc` was written in OCaml
- Now `rustc` is written in Rust; the compiler compiles itself
- Bootstrap entry point: a pre-built `rustc` binary (stage0), then stage1 → stage2 are fully self-hosted

### Summary

```
Java:   .java → javac(Java) → .class → JVM(C++) executes  ← always depends on C++
Go:     .go   → gc(Go)      → machine code                ← fully independent of C
Rust:   .rs   → rustc(Rust) → machine code                ← fully independent of C
```

The key enabler for Go and Rust: they compile directly to **native machine code**, so no C-written runtime is needed to execute the compiler output.

---

## 17. Native Binary Output: Java vs Go

By default, Go and Java produce fundamentally different outputs:

|                 | Go                | Java (default)             | Java (GraalVM)    |
| --------------- | ----------------- | -------------------------- | ----------------- |
| Output          | Native ELF binary | `.class` / `.jar` bytecode | Native ELF binary |
| Linux execution | `./app`           | `java -jar app.jar`        | `./app`           |
| Requires JVM    | No                | Yes                        | No                |

**Go** always compiles to native machine code — this is its default and only mode.

**Java** defaults to bytecode for the "write once, run anywhere" guarantee. The JVM handles platform differences at runtime.

**Java + GraalVM Native Image** can produce a native binary just like Go:

```bash
mvn -Pnative native:compile
./target/comments-tree-api   # runs without java installed
```

The trade-off: GraalVM compilation takes minutes (vs seconds for `mvn package`), and reflection / dynamic class loading must be declared explicitly. But the result runs with Go-like startup times and memory footprint.

---

## 18. ApplicationContext

`ApplicationContext` is Spring's core container interface. It manages the entire Bean lifecycle and application configuration.

### What It Is

```
ApplicationContext
  = BeanFactory  (Bean creation, dependency injection)
  + event publishing
  + internationalization (i18n)
  + resource loading
  + environment / configuration abstraction
```

`BeanFactory` is the minimal container. `ApplicationContext` is its superset and is always used in real projects.

### Common Implementations

| Implementation | Use case |
|---------------|----------|
| `AnnotationConfigApplicationContext` | Annotation-based, non-web |
| `AnnotationConfigServletWebServerApplicationContext` | Spring Boot Web (Servlet stack — used by this project) |
| `AnnotationConfigReactiveWebServerApplicationContext` | Spring Boot WebFlux (reactive stack) |

Spring Boot selects the implementation automatically based on what is on the classpath (`spring-webmvc` vs `spring-webflux`). No manual selection is needed.

### BeanDefinition vs Bean Instance

The container holds two distinct things:

```
BeanDefinition (metadata)              →  Bean instance (object)
"there is a singleton named            →  new CommentService(commentDao)
 commentService; its constructor          stored in the singleton pool
 requires a commentDao"
                                          (singletonObjects Map)
```

Scanning produces only `BeanDefinition` records. Instantiation happens later. This allows Spring to analyze all dependency relationships first, determine instantiation order, and detect circular dependencies before creating any object.

### Why `refresh()` Is Called That

`refresh()` is defined on `ConfigurableApplicationContext`:

```java
public interface ConfigurableApplicationContext {
    void refresh();  // discard all Beans, re-scan config, rebuild the entire container
    void close();    // shut down the container
}
```

The name reflects the interface contract: **throw away the current state and reload everything from scratch**. It is not a partial update — it is a full rebuild.

For a normal Spring Boot application, `refresh()` is called exactly once at startup. It can be called again in specific scenarios:

| Scenario | Description |
|----------|-------------|
| **Spring Cloud** | Config-center (Nacos/Consul) pushes new configuration → `ContextRefresher.refresh()` rebuilds the context |
| **Spring Boot DevTools** | Detects `.class` changes at dev time → `close()` + new context + `refresh()` (two ClassLoaders alternating) |
| **`@RefreshScope`** | Beans annotated with `@RefreshScope` are destroyed and recreated on refresh; other Beans are unaffected |
| **Integration tests** | Multiple test cases may each create their own context |

`refresh()` vs `close()` vs restart:

```
refresh()  = on the same context object: destroy all Beans, re-scan, re-instantiate
close()    = destroy the context and release all resources
restart    = close() + new context + refresh()
```

### In This Project

```java
@SpringBootApplication   // = @ComponentScan + @EnableAutoConfiguration + @Configuration
public class CommentsTree {
    public static void main(String[] args) {
        SpringApplication.run(CommentsTree.class, args);
        //                    ↑ component scan root + configuration source
        //                      internally creates and refresh()es an ApplicationContext
    }
}
```

`run()` returns the fully refreshed `ApplicationContext`. At that point Jetty is listening and all Beans are ready.

---

## 19. Why Java Needs Tomcat/Jetty for Web Services

### Can Java Serve HTTP Without Tomcat?

Yes. The JDK includes a basic HTTP server:

```java
// Raw TCP socket — parse HTTP manually
ServerSocket ss = new ServerSocket(8080);

// JDK built-in (since JDK 6) — not production grade
HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
server.createContext("/", exchange -> { ... });
server.start();
```

The JDK's built-in `HttpServer` is intended for tools and testing. It lacks connection pooling, HTTP/2, proper TLS, and a tunable thread model. It is not used in production.

### Why Tomcat/Jetty Emerged

Java EE (late 1990s) was designed for portability: **the same WAR file should deploy to any server** (Tomcat, JBoss, WebLogic, WebSphere).

```
Application layer (WAR)
  ↕  Servlet API (javax.servlet.*)   ← standard interface, defined by Sun/Oracle
Servlet container (Tomcat / Jetty / JBoss)
  ↕  TCP / HTTP
OS
```

The Servlet API is the **specification**; Tomcat/Jetty are **implementations**. Applications depend only on the spec and are theoretically container-agnostic. This is the opposite of Go's philosophy, where `net/http` is a production-grade standard library with no concept of a "deployment target".

### Spring Boot Changed the Experience

Spring Boot embeds Tomcat/Jetty inside the fat jar. From a developer's perspective it now resembles Go:

```bash
# Go
go build && ./app

# Spring Boot
mvn package && java -jar app.jar
```

No separate Tomcat installation, no WAR deployment. Jetty sits in `BOOT-INF/lib/` and Spring starts it inside `finishRefresh()`.

### What Tomcat/Jetty Actually Solve

Tomcat and Jetty are not just "adapters" — they carry 20+ years of production-hardened implementation for:

- HTTP/1.1, HTTP/2, HTTP/3
- WebSocket
- TLS (via JDK SSLEngine or native OpenSSL)
- Connection pooling and keep-alive
- Thread model tuning
- Defense against slow-connection attacks

If Tomcat did not exist, another component would have to solve exactly these same problems. The complexity does not disappear — it is just absorbed by a well-tested library.

### Comparison with Go

| | Go | Spring Boot (Servlet) |
|-|----|-----------------------|
| HTTP implementation | Standard library, production grade | Embedded Tomcat/Jetty |
| Deployment | Single binary | Single fat jar |
| Developer experience | Simple, direct | Close to Go since Spring Boot |
| Design goal | Simple and efficient | Enterprise-grade swappable deployment (origin) |

---

## 20. Tomcat/Jetty vs Netty: IO Model and Virtual Threads

### Why Netty Was Needed

Traditional Tomcat uses blocking IO: one thread per request, blocked while waiting for DB or network.

```
Blocking IO (Tomcat):
  1 request → 1 thread blocked waiting → thread unavailable for other work
  1000 concurrent → 1000 threads → high memory and scheduling overhead

Netty async non-blocking:
  1 request → register callback, thread immediately released
  1000 concurrent → tens of threads → high throughput
```

The cost: async programming (callbacks / Reactor pattern) is harder to write, read, and debug.

### Virtual Threads Close the Gap

Java 21 virtual threads make blocking IO nearly free:

```
Virtual thread blocks on IO:
  → JVM automatically unmounts the virtual thread from its carrier (platform) thread
  → carrier thread picks up another virtual thread
  → when IO completes, virtual thread is remounted and continues

Result: write synchronous blocking code, get near-async throughput
```

| | Netty / WebFlux | Tomcat + Virtual Threads |
|-|----------------|--------------------------|
| Programming model | Async, complex | Sync blocking, simple |
| Throughput | High | Close to Netty |
| CPU-bound tasks | No advantage | No advantage |
| Debug difficulty | High | Low |
| Learning curve | High | Low |

### When Netty Still Wins

| Scenario | Why Netty |
|----------|-----------|
| **Backpressure control** | Reactive streams let consumers tell producers to slow down; virtual threads have no equivalent |
| **Per-connection memory** | EventLoop model has lower overhead than virtual threads (each has a growable stack, ~1 KB initial) |
| **Streaming responses** | `Flux<T>` sends data chunk by chunk natively; Servlet is still request-response |

Spring team's position (current):

> "For most applications, Spring MVC + virtual threads is sufficient. WebFlux suits scenarios requiring backpressure and extreme resource efficiency."

### Decision Tree

```
Need a web service?
    ↓
Most business applications
    → Spring MVC + Tomcat/Jetty + virtual threads
    → Simple code, easy to maintain

Extreme throughput / streaming / backpressure
    → Spring WebFlux + Netty
    → Significantly higher code complexity

Millisecond startup / minimal memory (Serverless / FaaS)
    → GraalVM Native Image + Netty or Tomcat
    → Long compile times, reflection needs explicit config
```

---

## 21. Java Network Layer Landscape

All mainstream Java web stacks reduce to two lineages:

```
Servlet container family:   Tomcat / Jetty / Undertow
Netty family:               Netty (or Vert.x, which wraps Netty)
```

### Undertow

Red Hat / JBoss product. Spring Boot supports it as a first-class option alongside Tomcat and Jetty:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

Supports HTTP/1.1, HTTP/2, WebSocket, TLS. Non-blocking IO model. Lighter than Tomcat. Quarkus uses it by default.

### Other Frameworks and Their Network Layers

| Framework | Network layer | Notes |
|-----------|--------------|-------|
| **Quarkus** | Vert.x → Netty | Red Hat, cloud-native focus |
| **Micronaut** | Netty | Compile-time DI, fast startup |
| **Helidon SE** | Netty | Oracle |
| **Vert.x** | Netty | Multi-language, event-driven |
| **gRPC-Java** | Netty | gRPC protocol |

### What Netty Provides (Same as Tomcat)

Both Tomcat and Netty solve the same protocol-level problems. The difference is the IO model only:

| | Tomcat/Jetty/Undertow | Netty |
|-|----------------------|-------|
| HTTP/1.1 | ✅ | ✅ |
| HTTP/2 | ✅ | ✅ |
| WebSocket | ✅ | ✅ |
| TLS | ✅ | ✅ |
| IO model | Blocking (+ virtual threads) | Non-blocking async |

WebFlux replaces Tomcat with Netty. The protocol complexity does not disappear — it moves from Tomcat to Netty. The JDK's built-in `HttpServer` is not production grade and is almost never used.
