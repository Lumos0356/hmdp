package cn.lumos.wordreview;

import cn.lumos.wordreview.dto.Result;
import cn.lumos.wordreview.entity.ai.AIAnswer;
import cn.lumos.wordreview.entity.ai.Choices;
import cn.lumos.wordreview.entity.words.Book;
import cn.lumos.wordreview.entity.words.Word;
import cn.lumos.wordreview.mapper.BookMapper;
import cn.lumos.wordreview.service.IBookService;
import cn.lumos.wordreview.service.IChatGPTService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@SpringBootTest
class WordReviewApplicationTests {
    @Resource
    private IChatGPTService service;
    @Resource
    private IBookService bookService;

    @Resource
    BookMapper bookMapper;
    
    @Value("${chatgpt.api.url}") // 从配置文件中读取API URL
    private String apiUrl;
    @Value("${chatgpt.api.apiKey}") // 从配置文件中读取API apiKey
    private String apiKey;
    @Test
    void testChatApi() {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //构造请求
        HttpPost post = new HttpPost(apiUrl);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authorization", "Bearer " + apiKey);
        String paramJson = "{\"model\": \"text-davinci-003\", \"prompt\": \"" + "帮我写一个java冒泡排序" + "\", \"temperature\": 0, \"max_tokens\": 1024}";

        //发送请求
        StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/json", "UTF-8"));
        post.setEntity(stringEntity);

        try {
            //处理结果
            CloseableHttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String jsonStr = EntityUtils.toString(response.getEntity());
                AIAnswer aiAnswer = JSON.parseObject(jsonStr, AIAnswer.class);
                StringBuilder answers = new StringBuilder();
                List<Choices> choices = aiAnswer.getChoices();
                for (Choices choice : choices) {
                    answers.append(choice.getText());
                }
                System.out.println(answers.toString());
            } else {
                System.out.println("api.openai.com Err Code is " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetStory() {
//        List<String> words = new ArrayList<>();
//        words.add("apple");
//        words.add("human");
//        words.add("revolutionary");
//        words.add("intermediate");
//        words.add("unwilling");
        String model = "\n" +
                "现在你来扮演我的英语老师，接下来的对话我会给你发几个英语单词，你要用我给出的单词编一个有趣的英文小故事。我发的英语单词会用括号括住，只有括号里面的单词才是你需要编成故事的，单词之间我会用逗号隔开。这个故事你需要按如下模版回答，注意，该模板一共包括三部分，你必须将三部分都写出来。\n" +
                "第一部分（英文原文）：John was a data scientist who received a set of (instruction) to improve the accuracy of the (models) he had (submitted) for a project. He diligently followed the (requests) and spent days working on the code to make the necessary improvements. In the end, his hard work paid off and the accuracy of the models significantly (improved).\n" +
                "第二部分（汉语对照）: 约翰是一位数据科学家，他收到了一组（instruction）来改进他为一个项目（submitted）的（model）的准确性。他勤奋地遵循了（requests），并花费了几天的时间修改代码以进行必要的改进。最终，他的辛勤工作得到了回报，模型的准确性显著（improved）了。\n" +
                "第三部分（词汇学习）：\n" +
                "instruction (n. 指示，说明): a statement that describes how to do something or how something operates\n" +
                "requests (n. 请求): an act of asking politely or formally for something\n" +
                "submitted (v. 提交): past tense of submit, which means to present for consideration or judgment\n" +
                "models (n. 模型): a simplified representation of a complex system or process\n" +
                "improve (v. 改进): to make something better or more satisfactory\n" +
                "\n" +
                "单词如下：" + "[apple, human, revolutionary, intermediate, unwilling]";

        Result ask = service.ask(model);
        System.out.println(ask.getData());
    }
    
//    @Test
//    void getBookById() {
//        List<Word> words = bookMapper.getWordsForBook(13106822L);
//        for (Word word : words) {
//            System.out.println(word.getWord() + " (" + word.getPronunciation() + ")");
//            System.out.println("  " + word.getName());
//            System.out.println("  Phrase:");
//            for (String phrase : word.getPhrases()) {
//                System.out.println("    - " + phrase);
//            }
//        }
////        bookService.getBookById(id);
//
//    }
}
