package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest (classes = ServletsApplicationTests.Config.class)
public class ServletsApplicationTests {

 @Configuration
 public static class Config {}

 @Test
 public void contextLoads() {
 }

}
