package ru.ogbozoyan.core.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;
import ru.ogbozoyan.core.service.AiService;
import ru.ogbozoyan.core.service.DesckewService;
import ru.ogbozoyan.core.service.DocumentService;
import ru.ogbozoyan.core.service.S3Service;
import ru.ogbozoyan.core.web.dto.S3CustomResponse;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    @PostMapping(value = "/descew")
    @ResponseStatus(HttpStatus.OK)
    @Operation(hidden = true)
    @Deprecated
    public ResponseEntity<ConcurrentHashMap<String, ByteArrayResource>> testDesckew(@RequestParam("presignedUrl") String preSignedUrl) {
        return ResponseEntity.ok(desckewService.uploadAndGetFiles(preSignedUrl));
    }

    @PostMapping(value = "/document",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "1 шаг. Сохранить докмуент и получить uuid + url сохраненного документа")
    public DocumentEntity documentUploadAndSaveToDB(@RequestParam("file") MultipartFile multipartFile) {
        return documentService.uploadDocument(multipartFile);
    }

    @GetMapping(value = "/document/split/streaming",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(description = "2 шаг. Сюда отправляется uuid документа после 1 шага", deprecated = true)
    public Flux<DocumentService.UploadedFileResponse> splitDocumentAndUploadToS3Streaming(@RequestParam("uuid") UUID uuid) {
        return documentService.splitDocumentToPartsAndUploadToS3Stream(uuid);
    }

    @GetMapping(value = "/document/split", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "2 шаг. Сюда отправляется uuid документа после 1 шага, полученные name + url нужно по очереди отправить для получения ответа от нейронки")
    public ResponseEntity<List<DocumentService.UploadedFileResponse>> splitDocumentAndUploadToS3(@RequestParam("uuid") UUID uuid) {
        return ResponseEntity.ok(documentService.splitDocumentToPartsAndUploadToS3(uuid));
    }

    @PostMapping(value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Deprecated
    @Operation(hidden = true)
    public ResponseEntity<S3CustomResponse> upload(@RequestParam("file") MultipartFile file) {
        UUID uuid = UUID.randomUUID();
        return ResponseEntity.ok(s3Service.uploadFile(uuid, file));
    }


    @GetMapping("/download/{uuid}/{filename}")
    @Deprecated
    @Operation(hidden = true)
    public ResponseEntity<byte[]> downloadByUuidAndFilename(@PathVariable UUID uuid,
                                                            @PathVariable String filename) {
        byte[] file = s3Service.downloadFile(uuid, filename);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
    }

    @GetMapping("/presigned-url/{uuid}")
    @Deprecated
    @Operation(hidden = true)
    public ResponseEntity<String> getPresignedUrlByUuid(@PathVariable UUID uuid) {
        String filename = s3Service.resolveFilenameFromS3(uuid);
        return ResponseEntity.ok(s3Service.getPresignedUrl(uuid, filename));
    }

    @GetMapping("/presigned-url/{uuid}/{filename}")
    @Operation(description = "Получить пресignedUrl для скачивания файла по uuid и имени файла")
    public ResponseEntity<String> getPresignedUrlByUuidAndFilename(@PathVariable UUID uuid,
                                                                   @PathVariable String filename) {
        return ResponseEntity.ok(s3Service.getPresignedUrl(uuid, filename));
    }

    @GetMapping("/presigned-url/{filepath}")
    @Operation(description = "Получить пресignedUrl для скачивания файла по пути полному до файла key+/+name")
    public ResponseEntity<String> getPresignedUrlByUuidAndFilename(@PathVariable String filepath) {
        return ResponseEntity.ok(s3Service.getPresignedUrl(filepath));
    }

}
