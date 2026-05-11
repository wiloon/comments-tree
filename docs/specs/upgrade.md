# 升级记录（2026-05）

## 概述

2026 年 5 月，对项目进行了两项主要升级：

1. **Java 后端升级**：Java 8 + Spring Boot 2.6.3 → Java 17 + Spring Boot 3.5.14
2. **前端构建工具迁移**：Vue CLI（webpack 4）→ Vite 4

---

## 一、Java 后端升级

### 升级目标

| 组件                  | 升级前   | 升级后   |
| --------------------- | -------- | -------- |
| Java                  | 1.8      | 17       |
| Spring Boot           | 2.6.3    | 3.5.14   |
| Spring Security       | 5.6.x    | 6.5.x    |
| maven-compiler-plugin | 3.9.0    | 3.13.0   |
| fastjson              | 1.2.79   | 1.2.83   |
| sqlite-jdbc           | 3.36.0.3 | 3.47.1.0 |

### 升级步骤

**Step 1 — 确认环境**

系统已安装 JDK 17（`/usr/lib/jvm/java-17-openjdk`），Maven 3.9.15，无需额外安装。

**Step 2 — 建立基线**

用 JDK 17 跑原有代码，记录基线测试通过数作为验收标准。

**Step 3 — Spring Boot 2.6.3 → 2.7.18（中间版本）**

先升到 Spring Boot 2.7.x（2.x 最终版），利用其废弃警告定位后续 3.x 的 breaking changes：

- root `pom.xml`：`spring-boot-starter-parent` 版本 2.6.3 → 2.7.18

**Step 4 — Spring Boot 2.7.18 → 3.5.14 + Jakarta EE + Java 17**

Spring Boot 3.x 的主要 breaking changes 一次性处理：

- root `pom.xml`：Spring Boot 3.5.14，`maven-compiler-plugin` 3.13.0，`<source>/<target>` 改为 17
- `comments-tree-api/pom.xml`：
  - 移除 `spring-boot-starter-test` / `spring-boot-test` / `spring-boot-test-autoconfigure` 的显式版本（交由 BOM 管理）
  - 移除 `h2` 显式版本（交由 BOM 管理）
  - 移除 `junit:junit` 依赖
  - `fastjson` 1.2.79 → 1.2.83（修复 CVE）
  - `sqlite-jdbc` 3.36.0.3 → 3.47.1.0（修复 CVE-2023-32697）
- 全部 `javax.servlet.*` → `jakarta.servlet.*`（6 个文件）
- 全部 `javax.validation.*` → `jakarta.validation.*`（3 个文件）
- `SecurityConfig`：从 Spring Security 5.x 链式 DSL（`antMatchers` / `authorizeRequests`）重写为 6.x Lambda DSL（`requestMatchers` / `authorizeHttpRequests`）
- 测试文件迁移到 JUnit 5：
  - `@RunWith(MockitoJUnitRunner.class)` → `@ExtendWith(MockitoExtension.class)`
  - `@RunWith(SpringRunner.class)` 移除（`@SpringBootTest` 已包含）
  - `org.junit.*` → `org.junit.jupiter.api.*`

**Step 5 — 最终验证**

```
mvn verify -pl comments-tree-api -am -DskipFrontend=true
```

结果：**18/18 测试全部通过**（11 单元测试 + 7 集成测试）。

### 关键注意事项

- `javax.sql.DataSource` 是 Java SE 标准库，**不需要**改成 jakarta
- `PasswordEncoderConfig` 必须保持独立的 `@Configuration` 类，避免 `UserService` 与 `SecurityConfig` 之间的循环依赖
- Spring Security 6.x 中 `antMatchers` / `authorizeRequests` 已被彻底移除，必须使用 Lambda DSL

---

## 二、前端构建工具迁移：Vue CLI → Vite

### 迁移原因

Node.js 18 + OpenSSL 3 与 webpack 4 不兼容，启动时报 `ERR_OSSL_EVP_UNSUPPORTED`。选择迁移到 Vite 而非使用 `--openssl-legacy-provider` 临时绕过。

### 主要变更

| 文件                     | 变更内容                                                                                                            |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------- |
| `package.json`           | 移除所有 `@vue/cli-*` 依赖，新增 `vite ^4.5.9`、`vite-plugin-vue2 ^2.0.3`                                           |
| `vite.config.ts`（新建） | Vite 配置：`vite-plugin-vue2`、`@` alias、`/api` 代理到 `:8081`、port 固定 5173                                     |
| `index.html`（新建）     | Vite 根 HTML 入口，`<script type="module" src="/src/main.ts">`                                                      |
| `vue.config.js`          | 删除（由 `vite.config.ts` 替代）                                                                                    |
| `src/main.ts`            | `process.env.NODE_ENV` → `import.meta.env.MODE`                                                                     |
| `src/plugins/vuetify.ts` | import 路径改为 `vuetify`（完整包），配合 Vue alias 解决多实例问题                                                  |
| `src/views/Login.vue`    | 移除多余的 `<div id="app"><v-app>` 外层包裹（在 `<router-view>` 内嵌套 `<v-app>` 导致 Vuetify dialog 无法正常挂载） |
| `src/views/Register.vue` | 同上                                                                                                                |
| `tsconfig.json`          | `"webpack-env"` → `"vite/client"`                                                                                   |
| `babel.config.js`        | 移除 `@vue/cli-plugin-babel/preset`（Vite 不需要 Babel）                                                            |
| `Taskfile.yml`           | 移除 `NODE_OPTIONS: --openssl-legacy-provider`，前端 URL 从 `:8080` 变为 `:5173`                                    |
| `playwright.config.ts`   | `baseURL` 从 `http://localhost:8080` 改为 `http://localhost:5173`                                                   |

### Vuetify 多实例问题

Vite 打包时 `vuetify` UMD 全包内嵌了自己的 Vue，与项目 Vue 实例冲突，导致 `<v-app>` 渲染崩溃。  
**修复方案**：在 `vite.config.ts` 中通过 `resolve.alias` 将 `vue` 指向 `node_modules/vue/dist/vue.esm.js`，强制所有依赖共用同一 Vue 实例：

```ts
resolve: {
  dedupe: ['vue'],
  alias: {
    'vue': path.resolve(__dirname, 'node_modules/vue/dist/vue.esm.js'),
    '@': path.resolve(__dirname, './src')
  }
}
```

### 最终验证

```
npx playwright test
```

结果：**5/5 e2e 测试全部通过**。

---

## 三、测试结果汇总

| 测试类型                     | 数量 | 结果       |
| ---------------------------- | ---- | ---------- |
| 单元测试（JUnit 5）          | 11   | ✅ 全部通过 |
| 集成测试（Spring Boot Test） | 7    | ✅ 全部通过 |
| e2e 测试（Playwright）       | 5    | ✅ 全部通过 |
