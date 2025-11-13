package ru.netology.cloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.storage.path}") String storagePath) {
        this.fileStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    public void storeFile(MultipartFile file, String filename, User user) throws IOException {
        Path userDir = this.fileStorageLocation.resolve(user.getLogin());
        Files.createDirectories(userDir);

        Path targetLocation = userDir.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation);
    }

    public Path loadFile(String filename, User user) {
        Path userDir = this.fileStorageLocation.resolve(user.getLogin());
        return userDir.resolve(filename).normalize();
    }

    public void deleteFile(String filename, User user) throws IOException {
        Path filePath = loadFile(filename, user);
        Files.deleteIfExists(filePath);
    }

    public boolean fileExists(String filename, User user) {
        Path filePath = loadFile(filename, user);
        return Files.exists(filePath);
    }
}
