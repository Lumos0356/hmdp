package cn.lumos.wordreview.service;

import cn.lumos.wordreview.dto.Result;

public interface IBookService {
    Result getWordsForBook(Long bookId);
}
