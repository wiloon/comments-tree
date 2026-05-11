package com.wiloon.comments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SecurityFilterChain 集成测试
 * 验证：SecurityConfig 改用 SecurityFilterChain Bean 后权限配置是否正确
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIT {

    @Autowired
    private MockMvc mockMvc;

    // --- 白名单接口无需登录 ---

    @Test
    public void getComments_noAuth_allowed() throws Exception {
        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk());
    }

    @Test
    public void getSession_noAuth_allowed() throws Exception {
        mockMvc.perform(get("/session"))
                .andExpect(status().isOk());
    }

    // --- 受保护接口未登录返回 401，响应体为 JSON ---

    @Test
    public void postComment_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\",\"parentId\":0}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // --- 已登录用户可以访问受保护接口（不会被 entryPoint 拦截）---

    @Test
    @WithMockUser(username = "testuser")
    public void postComment_withAuth_notUnauthorized() throws Exception {
        // 已认证时不应该返回 401（业务上可能 400/500，但不是未授权）
        mockMvc.perform(post("/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\",\"parentId\":0}"))
                .andExpect(status().is(not(401)));
    }

    // --- 未认证访问 /session 返回的 JSON 中 code 是 401 ---

    @Test
    public void sessionCheck_noAuth_returnsUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/session"))
                .andExpect(status().isOk())  // spring security 放行了 GET /session，由 controller 判断
                .andExpect(jsonPath("$.code").value(401));
    }

    // --- OPTIONS 请求无论接口是否受保护都应放行（CORS preflight）---

    @Test
    public void options_noAuth_allowed() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .options("/comment"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }
}
