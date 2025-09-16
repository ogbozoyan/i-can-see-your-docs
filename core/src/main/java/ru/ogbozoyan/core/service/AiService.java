package ru.ogbozoyan.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import ru.ogbozoyan.core.dao.entity.TableBig;
import ru.ogbozoyan.core.dao.entity.TableNamesEnum;
import ru.ogbozoyan.core.dao.entity.TableSmall;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private S3Service s3Service;

    private final String TABLE_PROCESSING_FORMAT_PROMPT = """
        Используя таблицу {%s}, извлеки значения согласно формату ниже:
        
        * Для каждой ячейки укажи её соответствующее целочисленное значение.
        * Если в ячейке несколько возможных значений или ты не уверен в точности, выведи все варианты с указанием уверенности каждого.
        * Если ячейка пустая или значения нет, выводи null.
        
        Формат вывода менять нельзя, он должен быть строго таким, как указано!
        Верни строго в JSON формате без дополнительного текста.
        """;

    public BigDecimal processEmployeeAi(String url) {

        byte[] imageBytes = s3Service.downloadFile(url);

        Resource imageResource = new ByteArrayResource(imageBytes);

        try {
            return chatClient.prompt()
                    .user(userSpec -> userSpec
                            .text("""
                                        Тебе необходимо определить номер сотрудника на фотографиию.
                                        Все значения целочисленые
                                        Ни в коем случае не выводи значения в текстовом формате.
                                        Если значения нету - ТО выводи null.
                                    """)
                            .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
                    )
                    .call()
                    .entity(BigDecimal.class);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public TableBig processTableAiBigTable(String url) {

        String prompt = TABLE_PROCESSING_FORMAT_PROMPT.formatted(TableNamesEnum.TABLE_1.getName());
        byte[] imageBytes = s3Service.downloadFile(url);

        assert imageBytes != null;
        Resource imageResource = new ByteArrayResource(imageBytes);

        try {
            return  chatClient.prompt()
                    .user(userSpec -> userSpec
                            .text(prompt)
                            .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
                    )
                    .call()
                    .entity(TableBig.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public TableSmall processTableAiSmall(String url, TableNamesEnum tableName) {
        if ((tableName == TableNamesEnum.TABLE_1) || (tableName == TableNamesEnum.LAST_NUMBER_TABLE)) {
            throw new IllegalArgumentException("Неправильно название МАЛЕНЬКИХ таблиц");
        }
        String prompt = TABLE_PROCESSING_FORMAT_PROMPT.formatted(tableName.getName());
        byte[] imageBytes = s3Service.downloadFile(url);

        assert imageBytes != null;
        Resource imageResource = new ByteArrayResource(imageBytes);
        try {
            return chatClient.prompt()
                    .user(userSpec -> userSpec
                            .text(prompt)
                            .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
                    )
                    .call()
                    .entity(TableSmall.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
