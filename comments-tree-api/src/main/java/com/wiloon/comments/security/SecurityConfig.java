package com.wiloon.comments.security;

import com.alibaba.fastjson.JSON;
import com.wiloon.comments.common.CommonResult;
import com.wiloon.comments.user.CommentsTreeUserDetails;
import com.wiloon.comments.user.User;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

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
        registry.antMatchers(HttpMethod.POST, "/session").permitAll(); // 用户登录
        registry.antMatchers(HttpMethod.GET, "/session").permitAll();
        registry.antMatchers("/ping").permitAll();
        registry.antMatchers("/comments").permitAll();
        registry.antMatchers("/user").permitAll();

        // 跨域的 OPTIONS 请求
        registry.antMatchers(HttpMethod.OPTIONS).permitAll();

        // logout
        registry.and().logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("http://localhost:8080/")
                .logoutSuccessHandler(new LogoutSuccessHandler(){
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        String accessControlAllowOrigin = request.getHeader("Access-Control-Allow-Origin");
                        response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
                        response.addHeader("Access-Control-Allow-Credentials", "true");
                        response.setContentType("text/json;charset=UTF-8");
                        response.getWriter().println(JSON.toJSONString(CommonResult.success("退出成功")));
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.flushBuffer();
                    }
                })
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
                .userDetailsService(userDetailsService()).authenticationSuccessHandler(new AuthenticationSuccessHandler(){

                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        CommentsTreeUserDetails user = (CommentsTreeUserDetails) userService.loadUserByUsername(authentication.getName());
                        request.getSession().setAttribute(User.SESSION_USER_ID_KEY, user.getUserId());

                    }
                })
                // form login
                .and().formLogin()
                .loginProcessingUrl("/session")
                .usernameParameter("nameOrEmail")
                .successHandler(loginAuthenticationSuccessHandler())
                .failureHandler(new AuthenticationFailureHandler(){
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                        String accessControlAllowOrigin = request.getHeader("Access-Control-Allow-Origin");
                        response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
                        response.addHeader("Access-Control-Allow-Credentials", "true");
                        response.setContentType("text/json;charset=UTF-8");
                        response.getWriter().println(JSON.toJSONString(CommonResult.failed("登录失败")));
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.flushBuffer();
                    }
                })
                .and()
                // .addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class)
                // .addFilterAt(rememberMeAuthenticationFilter(), RememberMeAuthenticationFilter.class)
        ;
        // httpSecurity.addFilterAt(UsernamePasswordAuthenticationFilter.class);
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

//    @Bean
//    public LoginFilter loginFilter() {
//
//        LoginFilter loginFilter = new LoginFilter();
//        loginFilter.setAuthenticationSuccessHandler(new DefaultAuthenticationSuccessHandler());
//        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
//            String accessControlAllowOrigin = request.getHeader("Access-Control-Allow-Origin");
//
//            response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
//            response.addHeader("Access-Control-Allow-Credentials", "true");
//
//            response.setContentType("text/json;charset=UTF-8");
//            response.getWriter().println(JSON.toJSONString(CommonResult.failed("登录失败")));
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.flushBuffer();
//        });
//        loginFilter.setRememberMeServices(rememberMeServices());
//        return loginFilter;
//    }

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
