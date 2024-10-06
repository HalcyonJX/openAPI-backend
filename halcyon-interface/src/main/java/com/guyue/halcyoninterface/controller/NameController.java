package com.guyue.halcyoninterface.controller;


import com.halcyon.halcyonclientsdk.model.User;
import com.halcyon.halcyonclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("name")
public class NameController {
    @GetMapping("/getName")
    public String getNameByGet(String name){
        return "GET 你的名字是"+name;
    }
    @PostMapping("/postName")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/postUser")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
        //从请求头中获取参数
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");

        //todo 实际情况应该是去数据库中查是否已分配给用户
        if(!accessKey.equals("guyue")){
            throw new RuntimeException("无权限");
        }
        //直接校验如果随机数大于1万，就认为签名错误
        if (Long.parseLong(nonce)>10000){
            throw new RuntimeException("签名错误");
        }
        //校验时间戳如果时间差距大于 3 分钟报错
        if (!isWithinThreeMinutes(timestamp)){
            throw new RuntimeException("时间戳过期");
        }
        //判断签名 -> 这里简化使用工具类里面定义好了的 secretKey
        //实际从数据库里获取
        if (!sign.equals(SignUtils.getSign(body, SignUtils.TEST_SECRET_KEY))){
            throw new RuntimeException("签名错误");
        }
        //如果权限校验通过，返回用户名字
        return "POST 用户名字是" + user.getUsername();
    }
    private boolean isWithinThreeMinutes(String timeStamp) {

        try {
            long receivedTimeMillis = Long.parseLong(timeStamp);

            //注意 客户端传入的时间戳是按照 毫秒 -> 秒存储的，此处需要同步 / 1000
            long currentTimeMillis = System.currentTimeMillis() / 1000; //除以 1000 获取秒数

            //计算时间差
            // 计算时间差
            Duration duration = Duration.between(
                    Instant.ofEpochMilli(receivedTimeMillis),
                    Instant.ofEpochMilli(currentTimeMillis)
            );
            return duration.toMinutes() <= 3;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
