package cn.lumos.wordreview.mapper;

import cn.lumos.wordreview.entity.words.Word;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WordMapper extends BaseMapper<Word> {

}
