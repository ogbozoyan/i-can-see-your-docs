package ru.ogbozoyan.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Autowired
    private ChatClient openAiChatClient;

    @Autowired
    private RestTemplate restTemplate;

    public DocumentEntity.TableBig processDocumentToAi(String presignedUrl){
        //TODO: change to S3 Get implementation

        String imageUrl = "http://176.124.214.22:9001/api/v1/download-shared-object/aHR0cDovLzQ4NjY0MTktY3MxNzAyNDo5MDAwL29jci9pbWcucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9QUtCV0FUU01FTEVIOEVNNDlWTEElMkYyMDI1MDUyNSUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyNTA1MjVUMTA1OTIyWiZYLUFtei1FeHBpcmVzPTQzMjAwJlgtQW16LVNlY3VyaXR5LVRva2VuPWV5SmhiR2NpT2lKSVV6VXhNaUlzSW5SNWNDSTZJa3BYVkNKOS5leUpoWTJObGMzTkxaWGtpT2lKQlMwSlhRVlJUVFVWTVJVZzRSVTAwT1ZaTVFTSXNJbVY0Y0NJNk1UYzBPREl4TWpFMU1pd2ljR0Z5Wlc1MElqb2liV2x1YVc5aFpHMXBiaUo5LmQ5R1NTbmVZLUlVOWp1Z2M2Zl85TVp1Vmh5Y1ZBUnA4UVNOQ0ZIbU5nU0pRN0VTOGtFb2tqTEtDTGR5NnVsMjJaNHBTZ1l2SDYxeXBGX2ZXSldvSG1RJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZ2ZXJzaW9uSWQ9bnVsbCZYLUFtei1TaWduYXR1cmU9OGI4ZGQwODJlNzE1MjM5ODI3MmQ4YTBiMDQ0NTJjNzhhMGY1ZTdmNWQ3Y2Y4YjI1YTM2MGMzM2Y2ZGU5NmVkZg";
        byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);

        Resource imageResource = new ByteArrayResource(imageBytes);

        var outputConverter = new BeanOutputConverter<>(DocumentEntity.TableBig.class);

        String jsonSchema = outputConverter.getJsonSchema();
        return openAiChatClient.prompt()
            .user(userSpec -> userSpec
                .text("""
                
                Выведи значения таблицы в заданном формате:
                
                Номер ячейки.  и значение, если ты не уверен или их несколько выведи [значение:уверенность в значении]
                Если значения нету - ТО выводи null
                Все значения целочисленые
                
                """)
                .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
            )
            .options(OpenAiChatOptions.builder()
                .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                .build())
            .call()
            .entity(DocumentEntity.TableBig.class);
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
