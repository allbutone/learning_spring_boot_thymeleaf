package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by ren_xt
 */
@Service
public class ImageService {
    public static String UPLOAD_ROOT = "upload-dir";//会在项目根目录下创建 upload-dir

    private ImageRepository imageRepository;
    private ResourceLoader resourceLoader;

    @Autowired
    public ImageService(ImageRepository imageRepository, ResourceLoader resourceLoader) {
        this.imageRepository = imageRepository;
        this.resourceLoader = resourceLoader;
    }

    Page<Image> findPage(Pageable pageable){
        return imageRepository.findAll(pageable);
    }

    public Resource findOneImage(String name){
        return resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + name);
    }

    public void createImage(MultipartFile file) throws IOException {
        if(!(new File(UPLOAD_ROOT).exists())){
            prepareImageDirectory();
        }

        if (!file.isEmpty()){
//            nio Files and Paths
            Files.copy(file.getInputStream(), Paths.get(UPLOAD_ROOT, file.getOriginalFilename()));
            imageRepository.save(new Image(file.getOriginalFilename()));
        }
    }

    public void deleteImage(String filename) throws IOException {
        final Image byName = imageRepository.findByName(filename);
        if (byName == null){
            throw new IllegalArgumentException("file " + filename + " does not exist");
        }
        imageRepository.delete(byName);
        Files.deleteIfExists(Paths.get(UPLOAD_ROOT, filename));
    }

    @Bean
    @Profile("dev")
    CommandLineRunner setUp(ImageRepository imageRepository) throws IOException {
        return args -> {
            prepareImageDirectory();

            FileCopyUtils.copy("Test file", new FileWriter(UPLOAD_ROOT + "/test"));//spring utils
            imageRepository.save(new Image("test"));

            FileCopyUtils.copy("Test file2", new FileWriter(UPLOAD_ROOT + "/test2"));
            imageRepository.save(new Image("test2"));

            FileCopyUtils.copy("Test file3", new FileWriter(UPLOAD_ROOT + "/test3"));
            imageRepository.save(new Image("test3"));
        };
    }

    private void prepareImageDirectory() throws IOException {
        FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));//spring utils
        Files.createDirectory(Paths.get(UPLOAD_ROOT));//nio
    }
}