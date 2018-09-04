package nju.trust.web;

import nju.trust.exception.BadRequestException;
import nju.trust.exception.InternalException;
import nju.trust.util.PathUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Author: J.D. Liao
 * Date: 2018/9/3
 * Description:
 */
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @RequestMapping("/image")
    public String uploadImage(MultipartFile image) {
        String path = PathUtils.getImagePath();
        String filename = StringUtils.cleanPath(image.getOriginalFilename());
        if (image.isEmpty())
            throw new BadRequestException("Image is empty");
        if (filename.contains("..")) {
            // This is a security check
            throw new BadRequestException(
                    "Cannot store file with relative path outside current directory " + filename);
        }

        try (InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, Paths.get(path).resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            return "static/images/upload/" + filename;
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalException("Upload failed");
        }
    }
}