package com.wiloon.comments;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Unit test for simple CommentsTree.
 */
public class CommentsTreeTest {
    @Test
    public void testPasswordHash(){
        String password = "0oVHFEqB";
        BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
        String hash =bcryptPasswordEncoder.encode(password);
        System.out.println(hash);
        Assert.assertTrue(bcryptPasswordEncoder.matches(password, hash));
    }
}
