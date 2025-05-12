package com.sky.controller.admin;

import com.sky.config.OssConfiguration;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-12
 * Time: 14:34
 */
@Slf4j
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){//注意：这里的参数必须与前端请求参数同名
        log.info("开始进行文件上传{}",file);
        try {

            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //使用UUID对上传图片名称重命名，避免重名情况
            String uuid = UUID.randomUUID().toString();
            //拼接新的文件名称
            String newFileName = uuid + "." + extension;

            //将获得的file文件转化为二进制数据
            byte[] fileBytes = file.getBytes();
            //调用文件上传工具类上传文件
            String FilePath = aliOssUtil.upload(fileBytes, newFileName);

            return Result.success(FilePath);
        } catch (IOException e) {
            log.error("文件上传失败{}",e);
        }
        return null;
    }
}
