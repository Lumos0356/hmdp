package cn.lumos.wordreview.mapper;

import cn.lumos.wordreview.entity.words.Book;
import cn.lumos.wordreview.entity.words.Word;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    List<Word> getWordsForBook(Long bookId);
}
