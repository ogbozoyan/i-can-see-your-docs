package ru.ogbozoyan.core.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.ogbozoyan.core.configuration.exception.DescewException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class DesckewService {

    @Value("${app.descew.api-url}")
    private String descewApiUrl;

    @Autowired
    private RestTemplate restTemplate;


    @SneakyThrows
    public ConcurrentHashMap<String, ByteArrayResource> uploadAndGetFiles(Resource fileResource) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(
            descewApiUrl + "/upload",
            requestEntity,
            byte[].class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return extractZipToMap(response.getBody());
        } else {
            throw new DescewException("Upload failed: " + response.getStatusCode());
        }

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
