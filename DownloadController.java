package com.hikvision.hemh.download.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.*;
import java.util.Objects;

@RestController
@RequestMapping("/download")
public class DownloadController {

    private static String dirPath = "D:\\Downloads\\";

    @GetMapping
    public String download(@PathParam("filename") String filename, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (Objects.isNull(filename) || filename.length() == 0) {
            return "Are you kidding me?";
        }

        File file = new File(dirPath + filename);
        long fSize = file.length();
        response.reset();
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "application/x-download;charset=utf-8");
        response.setHeader("Content-Length", String.valueOf(fSize));
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);// 文件名可能为中文，需要处理

        // 定义偏移量
        long offset = 0;
        if (null != request.getHeader("Range")) {
            // 断点续传
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            try {
                offset = Long.parseLong(request.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", ""));
            } catch (NumberFormatException e) {
                offset = 0;
            }
        }
        response.setHeader("Content-Range", "bytes " + offset + "-" + (fSize - 1) + "/" + fSize);

        // 移到偏移位置处
        FileInputStream input = new FileInputStream(file);
        input.skip(offset);


        byte[] buffer = new byte[1024 * 10];
        int length = 0;
        OutputStream output = response.getOutputStream();
        while ((length = input.read(buffer, 0, buffer.length)) != -1) {
            output.write(buffer, 0, length);
            // 用于调试，防止下载速度过快
            // Thread.sleep(100);
        }
        output.flush();
        return "Hello world!";
    }
}
