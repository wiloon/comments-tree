# Spring Boot 代码改进记录

## 背景

2026 年整理项目时，针对 2022 年编写的 Spring Boot 后端代码，识别并修复了以下不符合最佳实践的问题。

---

## 问题清单与改进方案

### 1. 过时的 Spring Security 配置 (`SecurityConfig.java`)

**问题**：继承 `WebSecurityConfigurerAdapter`，该类在 Spring Security 5.7 中已标注为 `@Deprecated`，在 6.0 中已移除。

**改进**：改用 `SecurityFilterChain` Bean 方式配置，不再继承任何基类。

```java
// ❌ 旧方式
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) { ... }
}

// ✅ 新方式
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.build();
    }
}
```

---

### 2. 字段注入应改为构造器注入

**问题**：`@Autowired` 直接注入字段，导致：
- 依赖关系不透明
- 无法声明字段为 `final`（不可变）
- 单元测试需要反射注入，难以 mock

**涉及文件**：`CommentController`、`UserController`、`CommentService`、`UserService`、`CommentsDao`、`SecurityConfig`

**改进**：改为构造器注入，字段加 `final`。

```java
// ❌ 旧方式
@Autowired
UserService userService;

// ✅ 新方式
private final UserService userService;

public CommentController(UserService userService) {
    this.userService = userService;
}
```

---

### 3. `@Controller` + `@ResponseBody` 应合并为 `@RestController`

**问题**：`CommentController` 和 `UserController` 使用 `@Controller` + `@ResponseBody`，这是 Spring 4.0 之前的写法，现在有更简洁的替代。

**改进**：将 `@Controller` + `@ResponseBody` 替换为 `@RestController`。

---

### 4. `BCryptPasswordEncoder` 静态初始化 (`UserService.java`)

**问题**：在 Service 类里静态初始化 `BCryptPasswordEncoder`，绕过了 Spring IoC 容器，无法被统一管理或替换。

```java
// ❌ 旧方式
private static final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
```

**改进**：在 `SecurityConfig` 中已有 `@Bean PasswordEncoder`，直接注入使用。

---

### 5. 异常处理：`e.printStackTrace()` 替换为 SLF4J 日志

**问题**：`BrowserCommandRunner`、`MacOsBrowser` 中使用 `e.printStackTrace()` 输出到标准错误，在生产环境下无法被日志系统收集。

**改进**：统一使用 `logger.error("message", e)` 记录异常。

---

### 6. 命令执行安全：字符串拼接改为数组参数 (`Browser.java`)

**问题**：`Runtime.getRuntime().exec(command + " " + url)` 用字符串拼接构造命令，如果 `url` 含有特殊字符或空格，会导致命令解析错误。

**改进**：使用数组形式传参，OS 不会对参数做 shell 解析。

```java
// ❌ 旧方式
Runtime.getRuntime().exec(command + " " + url);

// ✅ 新方式
Runtime.getRuntime().exec(new String[]{command, url});
```

---

### 7. `queryForObject` 空结果处理 (`UserService.java`)

**问题**：`jdbcTemplate.queryForObject()` 在查询结果为空时抛出 `EmptyResultDataAccessException`，而代码中没有捕获，会导致 500 错误。

**改进**：捕获异常并返回 `null`，或改用 `query()` 方法取列表后判断是否为空。

---

### 8. 使用 Jetty 内部 JSON 库 (`RestAuthenticationEntryPoint`, `RestfulAccessDeniedHandler`)

**问题**：使用了 `org.eclipse.jetty.util.ajax.JSON`，这是 Jetty 容器的内部工具类，不属于项目依赖的公开 API，与容器实现耦合。项目其他地方已使用 FastJSON，应保持统一。

**改进**：改用 FastJSON 的 `JSON.toJSONString()`。

---

### 9. `String.indexOf() >= 0` 改为 `String.contains()`

**问题**：`LinuxEnvironmentCondition`、`MacOsEnvironmentCondition`、`WindowsEnvironmentCondition` 中用 `indexOf() >= 0` 判断字符串包含关系，表意不清晰。

**改进**：使用 Java 1.5+ 提供的 `String.contains()` 方法。

---

### 10. `Init.java` 实现 `ServletContextAware` 过时

**问题**：`ServletContextAware` 是 Servlet 容器时代的接口，在 Spring Boot 中更推荐使用 `ApplicationRunner` 或监听 `ApplicationReadyEvent`。

**改进**：改为实现 `ApplicationRunner` 接口，在 `run()` 方法中执行初始化逻辑。

---

### 11. `BrowserConfig.java` 中三个 Bean 同名

**问题**：三个 `@Bean` 方法都命名为 `"browser"`，Spring 只会注册满足条件的那一个，但重名写法容易引起混淆，且 macOS 的条件错误地使用了 `WindowsEnvironmentCondition`。

**改进**：去掉显式 Bean 名称（让 Spring 使用方法名），并修正 macOS 的条件类。

---

## 修改文件汇总

| 文件                                | 修改内容                                                                              |
| ----------------------------------- | ------------------------------------------------------------------------------------- |
| `SecurityConfig.java`               | 移除 `WebSecurityConfigurerAdapter` 继承，改用 `SecurityFilterChain` Bean；构造器注入 |
| `CommentController.java`            | `@Controller` → `@RestController`；构造器注入；`e.printStackTrace()` → `logger.error` |
| `UserController.java`               | `@Controller` → `@RestController`；构造器注入                                         |
| `CommentService.java`               | 构造器注入                                                                            |
| `CommentsDao.java`                  | 构造器注入                                                                            |
| `UserService.java`                  | 构造器注入；移除静态 `BCryptPasswordEncoder`，改为注入 `PasswordEncoder`              |
| `RestAuthenticationEntryPoint.java` | Jetty JSON → FastJSON                                                                 |
| `RestfulAccessDeniedHandler.java`   | Jetty JSON → FastJSON                                                                 |
| `Browser.java`                      | 字符串拼接命令 → 数组参数                                                             |
| `MacOsBrowser.java`                 | `e.printStackTrace()` → `logger.error`                                                |
| `Init.java`                         | `ServletContextAware` → `ApplicationRunner`                                           |
| `LinuxEnvironmentCondition.java`    | `indexOf() >= 0` → `contains()`                                                       |
| `MacOsEnvironmentCondition.java`    | `indexOf() >= 0` → `contains()`                                                       |
| `WindowsEnvironmentCondition.java`  | `indexOf() >= 0` → `contains()`                                                       |
| `BrowserConfig.java`                | 去掉重复 Bean 名称；修正 macOS 条件类                                                 |

---

## 第二阶段：测试覆盖与分层

### 12. 新建 `PasswordEncoderConfig.java` 拆解循环依赖

**问题**：`UserService` 需要 `PasswordEncoder`，`SecurityConfig` 需要 `UserService`，同时 `SecurityConfig` 提供 `PasswordEncoder` Bean — 三者构成循环依赖，导致 Spring 在构造器注入模式下无法启动。

**改进**：将 `PasswordEncoder` Bean 提取到独立的 `@Configuration` 类 `PasswordEncoderConfig`，让 `UserService` 和 `SecurityConfig` 分别注入，打破循环。

---

### 13. 新增 `GlobalExceptionHandler.java`

**问题**：Controller 层的未捕获异常会以 `500 + HTML` 响应返回，前端无法正常解析。

**改进**：新增 `@RestControllerAdvice` 的全局异常处理器，统一返回 `CommonResult.failed("服务器内部错误")` + HTTP 500 JSON。

---

### 14. 单元测试：`UserServiceUnitTest`（7 个测试）

新建纯 Mockito 单元测试，覆盖 `UserService` 核心逻辑，无需启动 Spring 容器：

| 测试方法                                      | 验证点                                     |
| --------------------------------------------- | ------------------------------------------ |
| `getUserByName_notFound_returnsNull`          | DB 无结果时返回 `null`                     |
| `getUserByEmail_notFound_returnsNull`         | 同上（按邮箱查）                           |
| `getUserById_notFound_returnsNull`            | 同上（按 ID 查）                           |
| `hashPassword_delegatesToInjectedEncoder`     | 密码哈希委托给注入的 `PasswordEncoder`     |
| `isUserRegistered_returnsTrue`                | 用户已注册时返回 `true`                    |
| `isUserRegistered_returnsFalse`               | 用户未注册时返回 `false`                   |
| `loadUserByUsername_notFound_throwsException` | 用户不存在时抛 `UsernameNotFoundException` |

---

### 15. 集成测试：`SecurityIT`（6 个测试）

新建全上下文集成测试，覆盖 Spring Security 过滤器链行为：

| 测试方法                                      | 验证点                                 |
| --------------------------------------------- | -------------------------------------- |
| `getComments_noAuth_allowed`                  | GET /comments 无需认证                 |
| `getSession_noAuth_allowed`                   | GET /session 无需认证                  |
| `postComment_noAuth_returns401`               | POST /comment 未认证返回 401           |
| `postComment_withAuth_notUnauthorized`        | POST /comment 已认证不返回 401         |
| `sessionCheck_noAuth_returnsUnauthorizedJson` | 未认证 GET /session 返回 JSON code=401 |
| `options_noAuth_allowed`                      | CORS preflight OPTIONS 允许通过        |

---

### 16. 集成测试：`CommentsFlowIT`（完整业务流）

覆盖从注册到评论的完整链路：

1. 注册新用户 → 期望成功
2. 重复注册 → 期望失败（code=500）
3. 登录（form login POST /session）→ 获取 session
4. 携带 session 查询当前用户信息
5. 未登录查询评论列表（白名单接口）
6. 已登录发表评论
7. 查询评论列表，验证刚发的评论出现
8. 未登录发表评论 → 期望 401

---

### 17. 重构 `SortedCommentTest` 为纯单元测试

**问题**：`SortedCommentTest` 使用 `@SpringBootTest` + `@MockBean`，启动完整 Spring 上下文，但测试内容只涉及纯业务逻辑（评论树排序），完全不需要 Spring。

**改进**：改为 `@RunWith(MockitoJUnitRunner.class)` + `@Mock` + `@InjectMocks`，运行速度提升约 10 倍，并将 `catch (ParseException e) { e.printStackTrace() }` 改为 `throws ParseException`。

---

### 18. 测试分层：surefire（单元）vs failsafe（集成）

**问题**：原有测试全部由 `maven-surefire-plugin` 跑，无法区分单元测试和集成测试，`mvn test` 会触发需要完整 Spring 上下文的慢速测试。

**改进**：
- 添加 `maven-failsafe-plugin 3.0.0-M7`，绑定 `integration-test` + `verify` goal
- 命名约定：`*Test.java` → surefire（`mvn test`），`*IT.java` → failsafe（`mvn verify`）
- 将 `SecurityIntegrationTest.java` 重命名为 `SecurityIT.java`

```
mvn test          # 仅单元测试（毫秒级，无 Spring 上下文）
mvn verify        # 单元测试 + 集成测试
```

---

### 19. 修复 `CommentController.newComment()` 空指针隐患

**问题**：`getUserByNameOrEmail()` 在用户不存在时返回 `null`，controller 直接调用 `user.getId()` 没有 null 检查，在 `@WithMockUser` 场景（mock 用户不在 DB 里）下触发 NPE。

**改进**：在使用 `user` 之前加 null 检查，返回 `CommonResult.failed("Authenticated user not found")`。

---

### 20. 新增 Taskfile 测试任务

| 命令            | 说明                                    |
| --------------- | --------------------------------------- |
| `task test`     | 运行所有单元测试（`*Test`）             |
| `task test-it`  | 仅运行集成测试（`*IT`，跳过重跑单元）   |
| `task test-all` | 运行单元测试 + 集成测试（`mvn verify`） |

---

### 21. 新增 AI 编码规范文档

- `AGENTS.md`：项目根目录 AI agent 综合指南，覆盖语言约束、Java/Spring Boot 规范、测试分层约定
- `.github/copilot-instructions.md`：GitHub Copilot 专用指令（Copilot 自动读取），精简版规则速查

---

## 修改文件汇总（第二阶段）

| 文件                                        | 修改内容                                                     |
| ------------------------------------------- | ------------------------------------------------------------ |
| `PasswordEncoderConfig.java`                | 新建，拆解循环依赖                                           |
| `GlobalExceptionHandler.java`               | 新建，全局异常处理                                           |
| `CommentController.java`                    | 修复 `user == null` 时的 NPE                                 |
| `UserServiceUnitTest.java`                  | 新建，7 个纯 Mockito 单元测试                                |
| `SecurityIT.java`                           | 新建（原 `SecurityIntegrationTest`），6 个安全集成测试       |
| `CommentsFlowIT.java`                       | 新建，完整业务流集成测试                                     |
| `SortedCommentTest.java`                    | 重构为纯 Mockito 单元测试                                    |
| `comments-tree-api/pom.xml`                 | 添加 `maven-failsafe-plugin`                                 |
| `src/test/resources/application.properties` | 新建，H2 测试数据源配置                                      |
| `src/test/resources/jdbc/schema.sql`        | 新建，H2 兼容的测试 schema                                   |
| `Taskfile.yml`                              | 新增 `test` / `test-it` / `test-all` 任务；task 描述改为英文 |
| `AGENTS.md`                                 | 新建，AI 编码规范                                            |
| `.github/copilot-instructions.md`           | 新建，Copilot 指令                                           |

---

## 第三阶段：前端 E2E 测试框架迁移（Cypress → Playwright）

### 22. 迁移 E2E 测试框架：Cypress → Playwright

**原因**：
- 原 Cypress 版本（`@vue/cli-plugin-e2e-cypress ~4.5.0`）只有 1 个测试用例，无断言（assertions），测试依赖数据库中预存的 `user0` 账号，不自包含
- Playwright 自 2024 年起周下载量约为 Cypress 的 2 倍，Chromium / Firefox / WebKit 全支持，原生 async/await，Node >= 18 下无 engine 警告

**改进内容**：

- 安装 `@playwright/test`，安装 Chromium 浏览器
- 创建 `playwright.config.ts`：`baseURL=http://localhost:8080`，测试目录指向 `tests/e2e/playwright/`
- 新建 `tests/e2e/playwright/comment-flow.spec.ts`，包含 5 个自包含测试：

| 测试名                                          | 覆盖场景                                    |
| ----------------------------------------------- | ------------------------------------------- |
| `unauthenticated user sees the comment list`    | 未登录时登录/注册按钮可见，"新留言"按钮隐藏 |
| `user can log in and the logout button appears` | 登录后"退出"按钮出现                        |
| `logged-in user can post a root comment`        | 发根评论后评论内容出现在列表                |
| `logged-in user can reply to a comment`         | 回复评论后回复内容出现在列表                |
| `user can log out`                              | 退出后"登录"按钮重新出现                    |

- `beforeAll` 钩子用时间戳生成唯一用户，通过 **直接调用后端 API**（`request.newContext()` → `POST http://localhost:8081/user`）完成注册，绕过 UI 交互，避免注册结果不确定的竞态问题
- 登录成功断言改为**正向断言**（等待 `[data-cy=logout-dialog]` 出现），替换原来的负向断言（等待 `[data-cy=login-dialog]` 消失），避免 Vue 响应式更新延迟导致的误判
- 测试完全自包含，无需预设数据库数据
- Node 版本从 16.13.2 升级到 18.20.8（Playwright 1.x 要求 Node >= 18）
- `comments-tree-web/.node-version` 更新为 `18.20.8`
- `ui` task 使用 Node 18 启动 Vue dev server

---

### 23. tsconfig.json 废弃项与类型修复

| 选项               | 旧值                  | 新值                             | 原因                                                             |
| ------------------ | --------------------- | -------------------------------- | ---------------------------------------------------------------- |
| `moduleResolution` | `"node"` (= `node10`) | `"bundler"`                      | `node10` 在 TS 7.0 将停止支持；`"bundler"` 适配 webpack/vite     |
| `baseUrl`          | `"."`                 | 删除                             | `moduleResolution: bundler` 下不再需要；`paths` 改为以 `./` 开头 |
| `paths["@/*"]`     | `["src/*"]`           | `["./src/*"]`                    | 无 `baseUrl` 时路径必须是相对路径                                |
| `types`            | `["webpack-env", "jest"]` | 追加 `"node"`              | `playwright.config.ts` 中使用 `process.env`，需要 `@types/node`  |
| `include`          | 未包含根目录 `*.ts`   | 追加 `"playwright.config.ts"`   | 文件不在 `include` 范围内时 TS 无法对其进行类型检查              |

---

### 24. 修复 `Home.vue` v-treeview `open-all` 在 items 更新后失效

**问题**：Vuetify 2.x 的 `v-treeview` 的 `open-all` 属性只在初次挂载时展开所有节点。当评论保存后通过 `loadCommentsTree()` 异步更新 `:items` 时，treeview 不会自动重新展开，导致新回复隐藏在折叠节点中。同时原代码还绑定了 `:open="nodeOpen"`（初始为空数组），这会主动将所有节点折叠，加剧了问题。

**改进**：
- 移除 `:open="nodeOpen"` 绑定及 `nodeOpen` 字段
- 新增 `treeKey` 计数器，绑定到 `:key="treeKey"`
- 每次 `loadCommentsTree()` 成功后执行 `treeKey++`，强制 Vue 销毁并重建 treeview，使 `open-all` 重新生效

此 bug 由 Playwright 回复测试用例发现。

---

## 修改文件汇总（第三阶段）

| 文件                                        | 修改内容                                                                              |
| ------------------------------------------- | ------------------------------------------------------------------------------------- |
| `comments-tree-web/package.json`            | 新增 `test:e2e:pw` 脚本；`@playwright/test` 添加到 devDependencies                   |
| `comments-tree-web/playwright.config.ts`    | 新建，Playwright 配置                                                                 |
| `tests/e2e/playwright/comment-flow.spec.ts` | 新建，5 个自包含 Playwright 测试；`beforeAll` 用 API 直接注册；正向登录断言           |
| `comments-tree-web/.node-version`           | 16.13.2 → 18.20.8                                                                     |
| `Taskfile.yml`                              | `ui` task 使用 Node 18；新增 `e2e` task                                               |
| `comments-tree-web/tsconfig.json`           | 修复 `moduleResolution`、移除 `baseUrl`、修复 `paths`；追加 `types: node`；追加 `include: playwright.config.ts` |
| `comments-tree-web/src/views/Home.vue`      | 移除 `:open="nodeOpen"`；添加 `:key="treeKey"` + `treeKey++`，修复 `open-all` 失效 |
