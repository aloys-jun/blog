package com.aloys.blog.controller;

import com.aloys.blog.lucene.LuceneUtil;
import com.aloys.blog.util.FileUpload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;


@RestController
public class BlogController {

    @GetMapping("/index")
    public String index(){
        return "Hello blog";
    }

    @PostMapping("/upload")
    public String upload(MultipartFile file,String title){

        String filePath = FileUpload.upload(file);

        LuceneUtil.createIndex(new File(filePath),title);

        return "ok";

    }

    @PostMapping("/search")
    public Object doSearch(@RequestBody Map<String,String> query){

        return LuceneUtil.doSearch(query.get("query"));
    }
}
