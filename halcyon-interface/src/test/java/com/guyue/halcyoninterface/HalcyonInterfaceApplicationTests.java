package com.guyue.halcyoninterface;

import com.halcyon.halcyonclientsdk.client.OpenApiClient;
import com.halcyon.halcyonclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class HalcyonInterfaceApplicationTests {
    @Resource
    private OpenApiClient openApiClient;
    @Test
    void contextLoads() {
        String res = openApiClient.getNameByGet("帅哥");
        User user = new User();
        user.setUsername("faker");
        String res2 = openApiClient.getUserNameByPost(user);
        System.out.println(res);
        System.out.println(res2);
    }
}
