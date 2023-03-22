package cn.lumos.wordreview.entity.words;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("PR_TB")
public class Phrase {
    private String id;
    private String wordId;
    private String phrase;

}
