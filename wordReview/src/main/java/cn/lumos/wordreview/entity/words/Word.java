package cn.lumos.wordreview.entity.words;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Transient;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("VOC_TB")
public class Word {
    @TableField("vc_id")
    private String id;
    @TableField("original_id")
    private Integer oId;
    @TableField("vc_vocabulary")
    private String word;
    @TableField("vc_interpretation")
    private String name;
    @TableField("vc_phonetic_uk")
    private String pronunciation;
    @Transient
    private List<String> phrases;
    private Integer weight = -1;
}
