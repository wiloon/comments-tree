package com.wiloon.comments;

import com.wiloon.comments.user.CommentsTreeUserDetails;
import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Assertions;

@SpringBootTest
public class UserTest {
    @Autowired
    UserService userService;

    @Test
    public void test() {
        String userName="user0";
        String email="foo@bar.com";
        String id = userService.userRegister(userName, email, "password0");
        User user = userService.getUserById(id);
        Assertions.assertNotNull(user);
        userService.deleteUser(id);
        Boolean exist =  userService.isUserRegistered(userName,email);
        Assertions.assertFalse(exist);
    }

    @Test public void testUserDetail(){
        String userName="user0";
        String email="foo@bar.com";
        String id = userService.userRegister(userName, email, "password0");
        User user = userService.getUserById(id);
        CommentsTreeUserDetails commentsTreeUserDetails = new CommentsTreeUserDetails(user);
        Assertions.assertEquals(userName,commentsTreeUserDetails.getUsername());
        Assertions.assertNotNull(commentsTreeUserDetails.getUserId());
        userService.deleteUser(id);

    }
}
