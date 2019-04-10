package com.aloys.blog.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.io.IOException;
import java.util.UUID;

public class FileUpload {

    private static final String DIR_UPLOAD ="/upload";

    //文件上传
    public static String upload(MultipartFile file){
        String filePath = "";

        String name = file.getOriginalFilename();
        String suffix = name.substring(name.lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;
        File src = new File("");

        try {
            String path = src.getCanonicalPath() + DIR_UPLOAD;
            file.transferTo(new File(path + "/" + fileName).toPath());

            filePath = path + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }
}
