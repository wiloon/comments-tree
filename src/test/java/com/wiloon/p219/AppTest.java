package com.wiloon.p219;


import com.wiloon.p219.user.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Unit test for simple App.
 */
public class AppTest   {
    @Test
    public void testPasswordHash(){
        String password = "password0";
        BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash =bcryptPasswordEncoder.encode(password);
        System.out.println(hash);
        Assert.assertTrue(bcryptPasswordEncoder.matches(password, hash));
    }
}
