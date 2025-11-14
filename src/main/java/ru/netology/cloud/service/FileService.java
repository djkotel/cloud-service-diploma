package ru.netology.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud.model.FileInfo;
import ru.netology.cloud.model.User;
import ru.netology.cloud.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;

    public FileService(FileRepository fileRepository, FileStorageService fileStorageService) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
    }

    public void saveFile(MultipartFile file, String filename, User user) throws IOException {
        log.info("Saving file '{}' for user {}", filename, user.getLogin());
        fileStorageService.storeFile(file, filename, user);
        FileInfo fileInfo = new FileInfo(filename, file.getSize(), user);
        fileRepository.save(fileInfo);
    }

    public Path getFilePath(String filename, User user) throws IOException {
        log.debug("Getting path for file '{}' for user {}", filename, user.getLogin());
        return fileStorageService.loadFile(filename, user);
    }

    /**
     * Удаляем запись из БД в транзакции, затем физически удаляем файл.
     * Если удаление файла провалится, пытаемся восстановить запись в БД.
     */
    @Transactional
    public void deleteFile(String filename, User user) throws IOException {
        log.info("Deleting file '{}' for user {}", filename, user.getLogin());
        FileInfo fileInfo = fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new IOException("File not found"));

        // Сохраняем копию для потенциального восстановления
        FileInfo backup = new FileInfo(fileInfo.getFilename(), fileInfo.getSize(), fileInfo.getUser());

        // Удаляем запись из БД
        fileRepository.delete(fileInfo);

        try {
            // Пытаемся удалить с диска
            fileStorageService.deleteFile(filename, user);
            log.info("File '{}' deleted from storage for user {}", filename, user.getLogin());
        } catch (IOException e) {
            log.error("Failed to delete file '{}' from storage: {}. Restoring DB record.", filename, e.getMessage());
            // Пытаемся восстановить запись в БД
            try {
                fileRepository.save(backup);
                log.info("DB record for '{}' restored", filename);
            } catch (Exception ex) {
                log.error("Failed to restore DB record for '{}': {}", filename, ex.getMessage());
            }
            throw e;
        }
    }

    /**
     * Переименование: сначала обновляем запись в БД, затем переименовываем файл на диске.
     * В случае ошибки на диске — откатываем изменение в БД (вручную).
     */
    @Transactional
    public void renameFile(String oldFilename, String newFilename, User user) throws IOException {
        log.info("Renaming file '{}' -> '{}' for user {}", oldFilename, newFilename, user.getLogin());

        FileInfo fileInfo = fileRepository.findByUserAndFilename(user, oldFilename)
                .orElseThrow(() -> new IOException("File not found"));

        String prev = fileInfo.getFilename();
        fileInfo.setFilename(newFilename);
        fileRepository.save(fileInfo);

        try {
            Path oldPath = fileStorageService.loadFile(oldFilename, user);
            Path newPath = oldPath.resolveSibling(newFilename);
            Files.move(oldPath, newPath);
            log.info("File on disk renamed: {} -> {}", oldPath, newPath);
        } catch (IOException e) {
            log.error("Failed to rename file on disk: {}. Rolling back DB change.", e.getMessage());
            // rollback DB
            fileInfo.setFilename(prev);
            fileRepository.save(fileInfo);
            throw e;
        }
    }

    public List<FileInfo> getUserFiles(User user) {
        log.debug("Listing files for user {}", user.getLogin());
        return fileRepository.findByUserOrderByFilename(user);
    }
}
