package ru.netology.cloud.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud.model.FileInfo;
import ru.netology.cloud.model.User;
import ru.netology.cloud.repository.FileRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;

    public FileService(FileRepository fileRepository, FileStorageService fileStorageService) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
    }

    public void saveFile(MultipartFile file, String filename, User user) throws IOException {
        fileStorageService.storeFile(file, filename, user);
        FileInfo fileInfo = new FileInfo(filename, file.getSize(), user);
        fileRepository.save(fileInfo);
    }

    public Path getFilePath(String filename, User user) throws IOException {
        return fileStorageService.loadFile(filename, user);
    }

    public void deleteFile(String filename, User user) throws IOException {
        fileStorageService.deleteFile(filename, user);
        fileRepository.deleteByUserAndFilename(user, filename);
    }

    public void renameFile(String oldFilename, String newFilename, User user) throws IOException {
        Path oldPath = fileStorageService.loadFile(oldFilename, user);
        Path newPath = oldPath.resolveSibling(newFilename);
        Files.move(oldPath, newPath);

        FileInfo fileInfo = fileRepository.findByUserAndFilename(user, oldFilename)
                .orElseThrow(() -> new IOException("File not found"));
        fileInfo.setFilename(newFilename);
        fileRepository.save(fileInfo);
    }

    public List<FileInfo> getUserFiles(User user) {
        return fileRepository.findByUserOrderByFilename(user);
    }
}
