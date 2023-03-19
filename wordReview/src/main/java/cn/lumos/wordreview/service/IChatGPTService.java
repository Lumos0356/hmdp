package cn.lumos.wordreview.service;

import cn.lumos.wordreview.dto.Result;
import cn.lumos.wordreview.entity.AIQuestion;

public interface IChatGPTService {
    Result ask(AIQuestion question);
}
