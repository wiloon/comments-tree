package com.wiloon.comments.security;

import com.wiloon.comments.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;

import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserService userService;
    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity.authorizeRequests();
        // white list
        // vue 编译后的静态文件路径
        registry.antMatchers("/index.html").permitAll();
        registry.antMatchers("/favicon.ico").permitAll();
        registry.antMatchers("/css/*").permitAll();
        registry.antMatchers("/fonts/*").permitAll();
        registry.antMatchers("/js/*").permitAll();

        registry.antMatchers(HttpMethod.POST, "/session").permitAll(); // 用户登录
        // registry.antMatchers(HttpMethod.GET, "/session").permitAll(); // session 检查
        registry.antMatchers("/comments").permitAll();  // 查询 comments 列表
        registry.antMatchers("/user").permitAll(); // 用户 注册

        // 跨域的 OPTIONS 请求
        registry.antMatchers(HttpMethod.OPTIONS).permitAll();

        // logout
        registry.and().logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler()) // 登出成功handler
                .deleteCookies("JSESSIONID")
                .and().authorizeRequests()
                .anyRequest()
                .authenticated()
                // 关闭 csrf
                .and().csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // 使用session
                // 自定义权限拒绝类
                .and().exceptionHandling()
                .accessDeniedHandler(restfulAccessDeniedHandler())
                .authenticationEntryPoint(restAuthenticationEntryPoint())
                // remember me
                .and().rememberMe()
                .rememberMeParameter("rememberMe")
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(userDetailsService())
                .authenticationSuccessHandler(rememberSuccessHandler()) // remember me auth success handler
                // form login
                .and().formLogin()
                .loginProcessingUrl("/session")
                .usernameParameter("nameOrEmail")
                .successHandler(loginAuthenticationSuccessHandler()) // form login auth success handler
                .failureHandler(formLoginFailedHandler()) // form login auth failed handler
                .and();

    }

    @Bean
    public AuthenticationFailureHandler formLoginFailedHandler() {
        return new FormLoginAuthFailurHandler();
    }

    @Bean
    public AuthenticationSuccessHandler rememberSuccessHandler() {
        return new DefaultRememberMeSuccessHandler();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new DefaultLogoutSuccessHandler();
    }

    @Bean
    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter() throws Exception {
        //重用WebSecurityConfigurerAdapter配置的AuthenticationManager，不然要自己组装AuthenticationManager
        return new RememberMeAuthenticationFilter(authenticationManager(), rememberMeServices());
    }

    @Bean
    public AuthenticationSuccessHandler loginAuthenticationSuccessHandler() {
        return new DefaultAuthenticationSuccessHandler();
    }

    @Bean
    public UserService wjcUserDetailsService() {
        return new UserService();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        DefaultRememberMeService rememberMeServices = new DefaultRememberMeService("INTERNAL_SECRET_KEY", wjcUserDetailsService(), persistentTokenRepository());
        rememberMeServices.setParameter("rememberMe"); // 修改默认参数remember-me为rememberMe和前端请求中的key要一致
        rememberMeServices.setTokenValiditySeconds(30 * 24 * 60 * 60);
        return rememberMeServices;
    }
    @Override
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler() {
        return new RestfulAccessDeniedHandler();
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }
}
