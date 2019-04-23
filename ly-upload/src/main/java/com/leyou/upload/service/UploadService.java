package com.leyou.upload.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class UploadService {

    private static final List<String> allowTpyes = Arrays.asList("image/jpeg", "image/png", "image/bmp", "image/jpg");

    public String uploadImage(MultipartFile file) {
        try {
            // 校验文件
            String type = file.getContentType();
            if (!allowTpyes.contains(type)) {
                throw new LyException(ExceptionEnums.INVAILD_FILE_TYPE);
            }
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnums.INVAILD_FILE_TYPE);
            }
            // 准备目标路径
            File dest = new File("D:\\image", file.getOriginalFilename());
            // 保存文件到本地
            file.transferTo(dest);
            // 返回路径
            return "//localhost/image/" + file.getOriginalFilename();
        } catch (IOException e) {
            log.error("上传文件失败!", e);
            throw new LyException(ExceptionEnums.UPLOAD_FILE_ERROR);
        }
    }
}
