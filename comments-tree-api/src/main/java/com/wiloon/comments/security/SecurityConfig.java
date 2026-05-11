package com.wiloon.comments.security;

import com.wiloon.comments.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

/**
 * spring security config
 * @author wiloon
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserService userService;
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService, DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests()
                // vue 编译后的静态文件路径
                .antMatchers("/index.html").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/css/*").permitAll()
                .antMatchers("/fonts/*").permitAll()
                .antMatchers("/js/*").permitAll()
                // 用户登录
                .antMatchers(HttpMethod.POST, "/session").permitAll()
                // session 检查
                .antMatchers(HttpMethod.GET, "/session").permitAll()
                // 查询 comments 列表
                .antMatchers("/comments").permitAll()
                // 用户注册
                .antMatchers("/user").permitAll()
                // 跨域的 OPTIONS 请求
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated()
                .and().logout()
                .logoutUrl("/logout")
                // 登出成功handler
                .logoutSuccessHandler(logoutSuccessHandler())
                .deleteCookies("JSESSIONID")
                // 关闭 csrf
                .and().csrf().disable()
                .sessionManagement()
                // 使用session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                // 自定义权限拒绝类
                .and().exceptionHandling()
                .accessDeniedHandler(restfulAccessDeniedHandler())
                .authenticationEntryPoint(restAuthenticationEntryPoint())
                // remember me
                .and().rememberMe()
                .rememberMeParameter("rememberMe")
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(userService)
                // form login
                .and().formLogin()
                .loginProcessingUrl("/session")
                .usernameParameter("nameOrEmail")
                // form login auth success handler
                .successHandler(loginAuthenticationSuccessHandler())
                // form login auth failed handler
                .failureHandler(formLoginFailedHandler());

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationFailureHandler formLoginFailedHandler() {
        return new FormLoginAuthFailureHandler();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new DefaultLogoutSuccessHandler();
    }

    @Bean
    public AuthenticationSuccessHandler loginAuthenticationSuccessHandler() {
        return new DefaultAuthenticationSuccessHandler();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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
