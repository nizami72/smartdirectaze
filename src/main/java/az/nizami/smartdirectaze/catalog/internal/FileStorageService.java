package az.nizami.smartdirectaze.catalog.internal;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Log4j2
public class FileStorageService {

    private final Path rootPath;

    public FileStorageService(@Value("${app.product.folder.photo}") String photoFolder) {
        this.rootPath = Paths.get(photoFolder);
    }

    public String storeProductPhoto(Long shopId, Long productId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path targetDir = rootPath.resolve(String.valueOf(shopId)).resolve(String.valueOf(productId));
            Files.createDirectories(targetDir);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String randomName = RandomStringUtils.randomAlphanumeric(10) + extension;
            Path targetFile = targetDir.resolve(randomName);

            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored photo for product [{}] in shop [{}] at [{}]", productId, shopId, targetFile);

            return randomName;
        } catch (IOException e) {
            log.error("Failed to store photo for product [{}] in shop [{}]", productId, shopId, e);
            throw new RuntimeException("Could not store photo", e);
        }
    }

    public byte[] loadProductPhoto(Long shopId, Long productId, String filename) {
        try {
            Path targetFile = rootPath.resolve(String.valueOf(shopId)).resolve(String.valueOf(productId)).resolve(filename);
            if (Files.exists(targetFile)) {
                return Files.readAllBytes(targetFile);
            }
            log.warn("Photo for product [{}] in shop [{}] at [{}] not found", productId, shopId, targetFile);
            return null;
        } catch (IOException e) {
            log.error("Failed to load photo for product [{}] in shop [{}] filename [{}]", productId, shopId, filename, e);
            throw new RuntimeException("Could not load photo", e);
        }
    }
}
