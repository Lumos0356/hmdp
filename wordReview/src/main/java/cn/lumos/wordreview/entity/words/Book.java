package cn.lumos.wordreview.entity.words;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("BK_TB")
public class Book {
    private String id;
    @TableField("original_id")
    private Integer oId;
    private String name;
    @TableField("voc_count")
    private Integer wordCount;
}
