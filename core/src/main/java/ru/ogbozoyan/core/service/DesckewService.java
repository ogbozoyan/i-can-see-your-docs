package ru.ogbozoyan.core.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ogbozoyan.core.configuration.exception.DescewException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class DesckewService {

    @Autowired
    @Qualifier("descewWebClient")
    private WebClient webClient;


    public ConcurrentHashMap<String, ByteArrayResource> uploadAndGetFiles(String preSignedUrl) {
        log.info("Sending file to Flask URL: {}", preSignedUrl);

        byte[] zipBytes = webClient.post()
            .uri("/upload")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("url", preSignedUrl))
            .retrieve()
            .bodyToMono(byte[].class)
            .block();

        if (zipBytes == null) {
            throw new DescewException("Received null body from Flask");
        }

        return extractZipToMap(zipBytes);
    }

    @SneakyThrows
    private ConcurrentHashMap<String, ByteArrayResource> extractZipToMap(byte[] zipBytes) {
        ConcurrentHashMap<String, ByteArrayResource> fileMap = new ConcurrentHashMap<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }

                    byte[] fileContent = baos.toByteArray();
                    ZipEntry finalEntry = entry;
                    ByteArrayResource resource = new ByteArrayResource(fileContent) {
                        @Override
                        public String getFilename() {
                            return finalEntry.getName();
                        }
                    };

                    fileMap.put(entry.getName(), resource);
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract zip", e);
            throw new DescewException("Failed to extract zip file", e);
        }

        log.info("Found {} files; {}", fileMap.size(), fileMap);
        return fileMap;
    }

}
