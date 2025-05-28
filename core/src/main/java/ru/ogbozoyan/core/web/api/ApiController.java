package ru.ogbozoyan.core.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;
import ru.ogbozoyan.core.service.AiService;
import ru.ogbozoyan.core.service.DesckewService;
import ru.ogbozoyan.core.service.DocumentService;
import ru.ogbozoyan.core.service.S3Service;

import java.util.List;
import java.util.UUID;

@Tag(name = "API controller")
@CrossOrigin
@RestController
@RequestMapping("/api/v0/")
@RequiredArgsConstructor
public class ApiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private DesckewService desckewService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private DocumentService documentService;

    @PostMapping(value = "/document",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "1 шаг. Сохранить докмуент и получить uuid + url сохраненного документа")
    @Tag(name = "1 шаг")
    public DocumentEntity documentUploadAndSaveToDB(@RequestParam("file") MultipartFile multipartFile) {
        return documentService.uploadDocument(multipartFile);
    }

    @GetMapping(value = "/document")
    @Operation(description = "Получить список документов")
    public List<DocumentEntity> getAll() {
        return documentService.findAll();
    }

    @GetMapping(value = "/document/{uuid}")
    @Operation(description = "Получить документ по uuid")
    public DocumentEntity getByUuid(@PathVariable UUID uuid) {
        return documentService.findByUuid(uuid);
    }

    @GetMapping(value = "/document/split", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "2 шаг. Сюда отправляется uuid документа после 1 шага, полученные name + url нужно по очереди отправить для получения ответа от нейронки")
    @Tag(name = "2 шаг")
    public ResponseEntity<List<DocumentService.UploadedFileResponse>> splitDocumentAndUploadToS3(@RequestParam("uuid") UUID uuid) {
        return ResponseEntity.ok(documentService.splitDocumentToPartsAndUploadToS3(uuid));
    }

    @GetMapping("/presigned-url/{uuid}/{filename}")
    @Operation(description = "Получить пресignedUrl для скачивания файла по uuid и имени файла")
    public ResponseEntity<String> getPresignedUrlByUuidAndFilename(@PathVariable UUID uuid,
                                                                   @PathVariable String filename) {
        return ResponseEntity.ok(s3Service.getPresignedUrl(uuid, filename));
    }

    @GetMapping("/presigned-url/")
    @Operation(description = "Получить пресignedUrl для скачивания файла по пути полному до файла key+/+name")
    public ResponseEntity<String> getPresignedUrlByUuidAndFilename(@RequestParam("filepath") String filepath) {
        return ResponseEntity.ok(s3Service.getPresignedUrl(filepath));
    }

    @GetMapping(value = "/document/process")
    @Operation(description = "3 шаг. Передача документа на обработку нейронке")
    @Tag(name = "3 шаг")
    public DocumentService.AiProcessingResult documentProcessToAi(@RequestParam("uuid") UUID uuid) {
        return documentService.processDocumentToAi(uuid);
    }

}
