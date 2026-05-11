# GitHub Copilot Instructions

## Language

- All code you write (Java, TypeScript, Vue) must use **English** for identifiers, comments, Javadoc, and log messages.
- `Taskfile.yml` task descriptions and comments must be in **English**.
- Chinese is only acceptable in end-user UI display strings already present in the frontend templates.

## Java Style

- **Constructor injection** only — never `@Autowired` on fields.
- `private final` for all injected dependencies.
- Use `@RestController`, not `@Controller` + per-method `@ResponseBody`.
- Log with SLF4J `logger.error("msg", e)` — never `e.printStackTrace()`.
- Pass commands as `String[]` to `Runtime.exec()`, never as a concatenated string.

## Testing

- **Unit tests** → suffix `*Test`, use `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`. No Spring context.
- **Integration tests** → suffix `*IT`, use `@SpringBootTest` + `@AutoConfigureMockMvc`.
- `mvn test` runs only unit tests; `mvn verify` runs both.

## Spring Security

- Use `SecurityFilterChain` bean (not `WebSecurityConfigurerAdapter`).
- `PasswordEncoder` is provided by `PasswordEncoderConfig` — do not define it in `SecurityConfig`.

## Key Commands

```bash
mvn test -pl comments-tree-api -am              # unit tests
mvn verify -pl comments-tree-api -am -DskipFrontend=true  # all tests
task api    # start backend
task ui     # start frontend
```
