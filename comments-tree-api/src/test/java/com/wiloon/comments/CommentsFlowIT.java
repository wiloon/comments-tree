package com.wiloon.comments;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 完整业务流集成测试：注册 → 登录 → 发评论 → 查评论
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CommentsFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void fullFlow_register_login_comment_query() throws Exception {
        String username = "testuser_" + System.currentTimeMillis();
        String email = username + "@test.com";
        String password = "Test@1234";

        // 1. 注册
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(new JSONObject()
                                .fluentPut("name", username)
                                .fluentPut("email", email)
                                .fluentPut("password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 重复注册应失败
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(new JSONObject()
                                .fluentPut("name", username)
                                .fluentPut("email", email)
                                .fluentPut("password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));

        // 3. 登录（form login）
        MvcResult loginResult = mockMvc.perform(post("/session")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nameOrEmail", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
        assertNotNull(session);

        // 4. session 检查（已登录）
        mockMvc.perform(get("/session").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value(username));

        // 5. 未登录查评论列表（白名单接口）
        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. 已登录发表根评论
        MvcResult commentResult = mockMvc.perform(post("/comment")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(new JSONObject()
                                .fluentPut("content", "这是一条测试评论")
                                .fluentPut("parentId", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        // 7. 查询评论列表，刚发的评论应该出现
        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThan(0)));

        // 8. 未登录发评论应被拦截返回 401
        mockMvc.perform(post("/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(new JSONObject()
                                .fluentPut("content", "匿名评论")
                                .fluentPut("parentId", 0))))
                .andExpect(status().isUnauthorized());
    }
}
