package ru.ogbozoyan.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Autowired
    private ChatClient openAiChatClient;

    @Autowired
    private RestTemplate restTemplate;


    public Flux<String> processEmployeeAi(String url) {
        byte[] imageBytes = restTemplate.getForObject(url, byte[].class);

        Resource imageResource = new ByteArrayResource(imageBytes);

        return openAiChatClient.prompt()
            .user(userSpec -> userSpec
                .text("""
                        Тебе необходимо определить номер сотрудника на фотографиию.
                        Все значения целочисленые
                        Ни в коем случае не выводи значения в текстовом формате.
                        Если значения нету - ТО выводи null.
                    """)
                .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
            )
            .options(OpenAiChatOptions.builder()
                .build())
            .stream()
            .content();
    }

    public Flux<String> processTableAi(String url, boolean isBig) {
        String prompt;
        if (isBig) {
            prompt = """
                Выведи значения таблицы в заданном формате:
                
                Номер ячейки.  и значение, если ты не уверен или их несколько выведи [значение:уверенность в значении]
                Если значения нету - ТО выводи null
                Все значения целочисленые
                
                """;
        } else {
            prompt = """
                Выведи значения таблицы в заданном формате:
                
                Номер ячейки.  и значение, если ты не уверен или их несколько выведи [значение:уверенность в значении]
                Если значения нету - ТО выводи null
                Все значения целочисленые
                
                """;
        }
        byte[] imageBytes = restTemplate.getForObject(url, byte[].class);

        assert imageBytes != null;
        Resource imageResource = new ByteArrayResource(imageBytes);

        return openAiChatClient.prompt()
            .user(userSpec -> userSpec
                .text(prompt)
                .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
            )
            .options(OpenAiChatOptions.builder()
                .build())
            .stream()
            .content();
    }

    /*
    @PostMapping("/chat/json")
    ArtistInfoVariant chatJsonOutput(@RequestBody MusicQuestion question) {
        var outputConverter = new BeanOutputConverter<>(ArtistInfoVariant.class);
        var userPromptTemplate = new PromptTemplate("""
                Tell me the name of one musician famous for playing the {instrument} in a {genre} band.
                """);
        Map<String,Object> model = Map.of("instrument", question.instrument(), "genre", question.genre());
        var prompt = userPromptTemplate.create(model, OpenAiChatOptions.builder()
            .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
            .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, outputConverter.getJsonSchema()))
            .build());

        var chatResponse = chatModel.call(prompt);
        return outputConverter.convert(chatResponse.getResult().getOutput().getText());
    }
    */
}
