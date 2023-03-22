package cn.lumos.wordreview.service;

import cn.lumos.wordreview.dto.Result;

import java.util.List;

public interface IChatGPTService {
    Result ask(String question);

    Result getStory(List<String> words);
}
