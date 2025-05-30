package ru.ogbozoyan.core.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import ru.ogbozoyan.core.configuration.exception.DescewException;
import ru.ogbozoyan.core.util.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;

@Service
@Slf4j
public class DesckewService {

    private final S3Service s3Service;

    public DesckewService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public ConcurrentHashMap<String, ByteArrayResource> split(String filePath) {
        log.info("Splitting file: {}", filePath);

        try (InputStream inputStream = new ByteArrayInputStream(s3Service.downloadFile(filePath))) {

            BufferedImage original = ImageIO.read(inputStream);

            BufferedImage enhanced = ImageUtils.adjustContrast(original, 1f, 0f);

            byte[] zipBytes = ImageUtils.parseAndZip(enhanced);

            return extractZipToMap(zipBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
