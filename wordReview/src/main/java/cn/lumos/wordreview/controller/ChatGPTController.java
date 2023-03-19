package cn.lumos.wordreview.controller;

import cn.lumos.wordreview.dto.Result;
import cn.lumos.wordreview.entity.AIQuestion;
import cn.lumos.wordreview.service.IChatGPTService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/chat")
public class ChatGPTController {
    @Resource
    private IChatGPTService service;

    @PostMapping("/ask")
    public Result ask(@RequestBody AIQuestion question) {
        return service.ask(question);
    }
}
