package com.mqq.agent.controller;

import com.mqq.agent.app.LoveApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private LoveApp loveApp;

    @GetMapping(value = "/loveAppStream",  produces = "text/plain;charset=utf-8")
    public Flux<String> loveAppStream() {
        return loveApp.doStream("你好，我是程序员喵柒", UUID.randomUUID().toString());
    }
}
