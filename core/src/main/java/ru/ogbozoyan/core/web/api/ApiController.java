package ru.ogbozoyan.core.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.ogbozoyan.core.web.dto.S3CustomResponse;
import ru.ogbozoyan.core.service.S3Service;

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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation
    public ResponseEntity<DocumentEntity.TableBig> test() {
        return ResponseEntity.ok(aiService.processDocumentToAi(null));
    }


    @PostMapping(value = "/descew",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @Operation
    public ResponseEntity<String> testDesckew(@RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(desckewService.uploadAndGetFiles(multipartFile.getResource()).toString());
    }

    @PostMapping(value = "/document",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @Operation
    public ResponseEntity<Flux<?>> testDocument(@RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(documentService.uploadDocumentAndProcess(multipartFile));
    }

    @PostMapping(value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<S3CustomResponse> upload(@RequestParam("file") MultipartFile file) {
        UUID uuid = UUID.randomUUID();
        return ResponseEntity.ok(s3Service.uploadFile(uuid, file));
    }

    @GetMapping("/download/{uuid}")
    public ResponseEntity<byte[]> downloadByUuid(@PathVariable UUID uuid) {
        String filename = s3Service.resolveFilenameFromS3(uuid);
        byte[] file = s3Service.downloadFile(uuid, filename);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
    }

    @GetMapping("/download/{uuid}/{filename}")
    public ResponseEntity<byte[]> downloadByUuidAndFilename(@PathVariable UUID uuid,
                                                            @PathVariable String filename) {
        byte[] file = s3Service.downloadFile(uuid, filename);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
    }

    @GetMapping("/presigned-url/{uuid}")
    public ResponseEntity<String> getPresignedUrlByUuid(@PathVariable UUID uuid) {
        String filename = s3Service.resolveFilenameFromS3(uuid);
        return ResponseEntity.ok(s3Service.getPresignedUrl(uuid, filename));
    }

    @GetMapping("/presigned-url/{uuid}/{filename}")
    public ResponseEntity<String> getPresignedUrlByUuidAndFilename(@PathVariable UUID uuid,
                                                                   @PathVariable String filename) {
        return ResponseEntity.ok(s3Service.getPresignedUrl(uuid, filename));
    }

}
