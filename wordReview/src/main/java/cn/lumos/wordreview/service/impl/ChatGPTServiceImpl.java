package cn.lumos.wordreview.service.impl;

import cn.lumos.wordreview.dto.Result;
import cn.lumos.wordreview.entity.AIAnswer;
import cn.lumos.wordreview.entity.AIQuestion;
import cn.lumos.wordreview.entity.Choices;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.List;

@Service
public class ChatGPTServiceImpl implements IChatGPTService {
    private Logger logger = LoggerFactory.getLogger(ChatGPTServiceImpl.class);
    @Value("${chatgpt.api.url}") // 从配置文件中读取API URL
    private String apiUrl;
    @Value("${chatgpt.api.apiKey}") // 从配置文件中读取API apiKey
    private String apiKey;

    @Override
    public Result ask(AIQuestion question){
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //构造请求
        HttpPost post = new HttpPost(apiUrl);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authorization", "Bearer " + apiKey);
        String paramJson = "{\"model\": \"text-davinci-003\", \"prompt\": \"" + question.getMsg() + "\", \"temperature\": 0, \"max_tokens\": 1024}";

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
                return Result.ok(answers.toString());
            } else {
                return Result.fail("api.openai.com Err Code is " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
