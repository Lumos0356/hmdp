package cn.lumos.wordreview.service.impl;

import cn.lumos.wordreview.dto.Result;
import cn.lumos.wordreview.entity.words.Word;
import cn.lumos.wordreview.mapper.BookMapper;
import cn.lumos.wordreview.service.IBookService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BookServiceImpl implements IBookService {
    @Resource
    BookMapper bookMapper;

    @Override
    public Result getWordsForBook(Long bookId) {
        List<Word> words = bookMapper.getWordsForBook(13106822L);
        return Result.ok(words);
    }


}
