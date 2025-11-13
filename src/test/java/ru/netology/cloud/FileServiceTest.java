package ru.netology.cloud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud.model.FileInfo;
import ru.netology.cloud.model.User;
import ru.netology.cloud.repository.FileRepository;
import ru.netology.cloud.service.FileService;
import ru.netology.cloud.service.FileStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FileServiceTest {

    private FileRepository fileRepository;
    private FileStorageService fileStorageService;
    private FileService fileService;

    @BeforeEach
    public void setup() {
        fileRepository = Mockito.mock(FileRepository.class);
        fileStorageService = Mockito.mock(FileStorageService.class);
        fileService = new FileService(fileRepository, fileStorageService);
    }

    @Test
    public void saveFile_savesToStorageAndRepository() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getSize()).thenReturn(3L);
        Mockito.when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));
        User user = new User("ivan","p");

        fileService.saveFile(file, "a.txt", user);

        Mockito.verify(fileStorageService, Mockito.times(1)).storeFile(file, "a.txt", user);
        Mockito.verify(fileRepository, Mockito.times(1)).save(Mockito.any(FileInfo.class));
    }

    @Test
    public void getUserFiles_returnsList() {
        User user = new User("ivan","p");
        Mockito.when(fileRepository.findByUserOrderByFilename(user)).thenReturn(List.of(new FileInfo("a.txt", 3L, user)));
        List<FileInfo> list = fileService.getUserFiles(user);
        assertEquals(1, list.size());
    }
}
