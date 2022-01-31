package com.wiloon.comments;

import com.wiloon.comments.user.CommentsTreeUserDetails;
import com.wiloon.comments.user.User;
import com.wiloon.comments.user.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
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
        Assert.assertNotNull(user);
        userService.deleteUser(id);
        Boolean exist =  userService.isUserRegistered(userName,email);
        Assert.assertFalse(exist);
    }

    @Test public void testUserDetail(){
        String userName="user0";
        String email="foo@bar.com";
        String id = userService.userRegister(userName, email, "password0");
        User user = userService.getUserById(id);
        CommentsTreeUserDetails commentsTreeUserDetails = new CommentsTreeUserDetails(user);
        Assert.assertEquals(userName,commentsTreeUserDetails.getUsername());
        Assert.assertNotNull(commentsTreeUserDetails.getUserId());
        userService.deleteUser(id);

    }
}
