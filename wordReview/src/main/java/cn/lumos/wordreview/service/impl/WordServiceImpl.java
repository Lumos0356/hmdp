package cn.lumos.wordreview.service.impl;

import cn.lumos.wordreview.mapper.WordMapper;
import cn.lumos.wordreview.service.IWordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class WordServiceImpl implements IWordService {
    @Resource
    private WordMapper wordMapper;

}
