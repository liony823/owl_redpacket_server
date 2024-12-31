package com.td.client.controller;

import com.td.common.annotations.Anonymous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-17 10:36
 */
@RestController
public class UpdateController {

    @Anonymous
    @GetMapping("/download/{fileName:.+}")
    public void downloadImage(@PathVariable String fileName, HttpServletResponse response, HttpServletRequest request) {
        try {
            // 获取当前项目目录
            String currentDir = System.getProperty("user.dir");

            //读取当前目录下fileNames文件
            Path path = Paths.get(currentDir + File.separator + fileName);

            File file = path.toFile();
            if (file.exists()) {
                response.setContentType("application/force-download"); // 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                //设置文件大小
                response.setContentLength((int) file.length());
                byte[] buffer = new byte[1024];
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                java.io.BufferedInputStream bis = new java.io.BufferedInputStream(fis);
                java.io.OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                bis.close();
                fis.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
