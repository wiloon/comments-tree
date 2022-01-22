//package com.wiloon.comments.security;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.wiloon.comments.user.CommentsTreeUserDetails;
//import com.wiloon.comments.user.User;
//import com.wiloon.comments.user.UserController;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Map;
//
//public class LoginFilter extends UsernamePasswordAuthenticationFilter {
//    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);
//
//    {
//        logger.info("login filter: {}", this);
//    }
//
//    @Autowired
//    @Override
//    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
//        super.setAuthenticationManager(authenticationManager);
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//        logger.info("login filter attempt auth");
//        if (!request.getMethod().equals("POST")) {
//            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
//        }
//        if (request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE) || request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
//            String nameOrEmail = null;
//            String password = null;
//            Boolean rememberMe = null;
//            try {
//                Map<String, String> map = new ObjectMapper().readValue(request.getInputStream(), Map.class);
//                nameOrEmail = map.get("nameOrEmail").trim();
//                password = map.get("password").trim();
//                rememberMe = (Boolean) ((Object) map.get("rememberMe"));
//                logger.debug("login params: {}", map);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            // request.getInputStream()流读取一次就清空了
//            // 为了防止之后会频繁的使用表单中的参数，一次性全部将表单内容写入到attribute中去
//            // 即使多次使用参数我们直接getAttribute就可以拿到参数不用每次都使用流（也获取不到流了，会报流已关闭异常）
//            request.setAttribute("nameOrEmail", nameOrEmail);
//            request.setAttribute("password", password);
//            request.setAttribute("rememberMe", rememberMe);
//
//            User user = new User(nameOrEmail);
//            user.setPassword(password);
//
//            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( nameOrEmail, password);
//            // Allow subclasses to set the "details" property
//            setDetails(request, authRequest);
//            return this.getAuthenticationManager().authenticate(authRequest);
//        }
//        return super.attemptAuthentication(request, response);
//    }
//}
