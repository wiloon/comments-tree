# Java web+数据库题目2：无限层级留言

## 背景

这是 2022 年面试某远程职位时对方给出的带回家面试题，限时一周完成。题目要求实现一个前后端分离的树形留言评论系统。当时我只有 Java 经验，没有在生产项目中使用过 Spring Boot，代码是一边查资料一边开发、在一周内完成的。

2026 年为准备面试一个 Java Spring Boot 职位，重新整理了项目，进行了以下升级：

- **Java 8 → Java 17**，**Spring Boot 2.6.3 → 3.5.14**（Jakarta EE、Spring Security 6.x Lambda DSL、JUnit 5）
- **Vue CLI（webpack 4）→ Vite 4**（解决 Node 18 + OpenSSL 3 兼容性问题）
- 修复安全漏洞：fastjson 1.2.83、sqlite-jdbc 3.47.1.0（CVE-2023-32697）
- 补充 e2e 测试（Playwright）、完善 Taskfile

完整升级记录见 [docs/specs/upgrade.md](docs/specs/upgrade.md)。

## 技术栈

### 后端

| 组件                       | 版本                 |
| -------------------------- | -------------------- |
| Java                       | 17                   |
| Spring Boot                | 3.5.14               |
| Spring Security            | 6.5.x                |
| SQLite（生产）/ H2（测试） | sqlite-jdbc 3.47.1.0 |
| Maven                      | 3.9+                 |

### 前端

| 组件       | 版本               |
| ---------- | ------------------ |
| Vue.js     | 2.6.x              |
| Vuetify    | 2.x                |
| TypeScript | ~4.1.5             |
| Vite       | ^4.5.9             |
| Node.js    | 18.20.8（via fnm） |

## 本地开发启动

### 依赖

- JDK 17+
- Maven 3.9+
- Node.js 18（通过 [fnm](https://github.com/Schniz/fnm) 管理）
- [Task](https://taskfile.dev) (`go-task`)

### 启动后端

```bash
task api
```

编译 Spring Boot 后端（跳过测试和前端构建）并启动，监听 `http://localhost:8081`。

### 启动前端开发服务器

```bash
task ui
```

在 `comments-tree-web/` 目录下安装依赖并启动 Vite 开发服务器，访问 `http://localhost:5173`。

> 开发模式下前端通过 `/api` 代理转发请求到后端 `localhost:8081`。

### 运行测试

```bash
task test        # 单元测试
task test-it     # 集成测试
task e2e         # e2e 测试（需前后端已启动）
task test-all    # 以上全部（需前后端已启动）
```

### 查看所有可用命令

```bash
task
```

---

## 难度

普通

## 思路：

做一个简单网站实现某种简单但略有难度的功能；用到数据库；有注册登录功能；前后端分离，Java提供restful api，可使用流行框架；前端使用任一前端框架。

## 题目内容：

使用Java+数据库，做一个简单的“树形留言”网站。难点可能在“无限层级”，由于可以无限嵌套，在数据库设计、ORM/SQL查询、以及页面展示上都需要一定经验。同时也需要考察候选人基本的用户注册/登录功能，主要是对用户密码的处理、字段的验证、浏览器session/cookie的应用等技能。由于现在前后端分离已经是常态，要求Java后端提供Restful api，前端页面可以使用任一种前端框架（包含但不限于jquery/react/vue/angular等）

## 功能需求：

- 用户可以在网站上注册
  - 需要填写 username, password, email。
  - username需要检查：不可为空，只能使用字母和数字，长度在5~20之间，不能与已有用户名重复
  - password需要检查：不可为空，长度在8~20之间，至少包含一个大写、一个小写、一个数字、一个特殊符号
  - email需要检查：不可为空，格式要正确，不能与已有email重复。为简单起见，不需要发送邮件确认

- 用户可以在网站上登录
  - 使用username+password，或者email+password 登录
  - 提供”remember me”功能，登录后一个月内不需要重新登录
  - 如果未勾选”remember me”，则关闭浏览器后再次访问会提示注册或登录
  - 用户登录后，需要在页面上方显示用户名和Email

- 用户登录后，可以发表留言。
  - 留言长度在3~200字之间，可以为中文
  - 输入时会动态提示还可以再输入多少字
  - 会记录留言发表时间

- 可以针对某个留言进行再次评论
  - 评论输入的要求与留言相同
  - 可以针对某个评论再次评论，不限层级

- 用户可以查看留言
  - 只需要一个页面显示全部留言及树形嵌套的评论即可（一次性加载，不要懒加载）
  - 留言以时间倒序从上向下排列，最上面是最新的
  - 某个留言旁边可以看到发布者用户名和发表时间
  - 查看留言时不需要登录

## 技术需求：

- 提供一条命令进行网站的初始化、启动等功能，最终可以在浏览器中自动打开网站首页
- 可以使用Java的任意框架
- 使用数据库（关系数据库或NoSQL），自行建表，使用SQL/NO-SQL/ORM等。为了Review方便，推荐使用较简单的文件式数据库（如sqlite等），不需要安装
- 用户注册时，密码保存到数据库里不能使用明文，需要某种形式的不可逆加密。
- 可以使用ORM或者原生SQL等方式使用数据库查询
- 后端提供的Restful API，需要考虑到权限检查，以及正确的http method和http code
- 对于较大层数的嵌套留言（超过50层），不会出现明显的性能问题
- 有恰当的单元测试
- 作业以PR形式提交必要的代码和文件, 注意不要提交压缩包
