package ru.ogbozoyan.core.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Bean
    public ChatClient openAiChatClient() {
        return chatClientBuilder.clone()
            .defaultSystem(text -> text.text("""
                
                Выведи значения таблицы в заданном формате:
                
                Номер ячейки.  и значение, если ты не уверен или их несколько выведи [значение:уверенность в значении]
                Если значения нету - ТО выводи null
                Все значения целочисленые
                
                """))
            .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
            .build();
    }

}
