package com.mqq.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WeatherTools {

    @Tool(description = "获取指定城市的当前天气情况")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        return "上海今天晴朗，气温25℃";
    }
}
