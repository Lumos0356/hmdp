package cn.lumos.wordreview.controller;

import cn.lumos.wordreview.service.IWordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/words")
public class WordController {
    @Resource
    private IWordService wordService;



}
