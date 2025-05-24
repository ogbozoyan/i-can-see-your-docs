package ru.ogbozoyan.core.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.ogbozoyan.core.service.AiService;
import ru.ogbozoyan.core.service.DesckewService;

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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(aiService.imageURI());
    }

    @PostMapping(value = "/descew",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @Operation
    public ResponseEntity<String> testDesckew(@RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(desckewService.uploadAndGetFiles(multipartFile.getResource()).toString());
    }
}
