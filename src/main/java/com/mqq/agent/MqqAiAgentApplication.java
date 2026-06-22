package com.mqq.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mqq.agent.mapper")
public class MqqAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqqAiAgentApplication.class, args);
    }

}
