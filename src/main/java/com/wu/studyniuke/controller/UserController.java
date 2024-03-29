package com.wu.studyniuke.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.wu.studyniuke.annotation.LoginRequired;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.FollowService;
import com.wu.studyniuke.service.LikeService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author me
 * @create 2021-05-21-16:11
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${aliyun.key.access}")
    private String accessKey;

    @Value("${aliyun.key.secret}")
    private String secretKey;

    @Value("${aliyun.bucket.header.name}")
    private String headerBucketName;

    @Value("${aliyun.bucket.header.url}")
    private String headerBucketUrl;

    @Value("${aliyun.endpoint.url}")
    private String endpoint;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
//        //上传文件名称
//        String fileName = CommunityUtil.generateUUID();
//
//
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
//
//        PutObjectRequest putObjectRequest = new PutObjectRequest("examplebucket",
//                "data/"+fileName+".png",
//                new File("D:\\localpath\\examplefile.txt"));
//
//        ossClient.putObject(putObjectRequest);
//        ossClient.shutdown();
        return "site/setting";
    }


    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    public String updateHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "no image");
            return "/site/setting";
        }
        String localFilename = headerImage.getOriginalFilename();
        String substring = localFilename.substring(localFilename.lastIndexOf("."));
        if (StringUtils.isBlank(substring)) {
            model.addAttribute("error", "false fileName");
            return "site/setting";
        }

        //阿里云实现上传文件
        String uploadFileName = CommunityUtil.generateUUID() + substring;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);

//        System.out.println(localFilename);
//        System.out.println(uploadFileName);

        InputStream is = null;
        try {
            is = headerImage.getInputStream();
        } catch (IOException e) {
            LOGGER.error("转换失败");
        }


        ossClient.putObject(headerBucketName, uploadFileName, is);
        ossClient.shutdown();


        String headUrl = headerBucketUrl + "/" + uploadFileName;
        userService.updateHeadUrl(hostHolder.getUser().getId(), headUrl);

        return "redirect:/index";
    }

    //废弃
    @Deprecated
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "no image");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String substring = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(substring)) {
            model.addAttribute("error", "false fileName");
            return "site/setting";
        }
        filename = CommunityUtil.generateUUID() + substring;
        File file = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            LOGGER.error("fail to upload file" + e.getMessage());
            throw new RuntimeException("fail to upload file" + " server exception", e);
        }
        String headUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeadUrl(hostHolder.getUser().getId(), headUrl);

        return "redirect:/index";

    }

    //废弃
    @Deprecated
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        filename = uploadPath + "/" + filename;
        String suffix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(filename);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            LOGGER.error("fail to get image" + e.getMessage());
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model,
                                 @RequestParam(name = "infoMode", defaultValue = "0") int infoMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("infoMode", infoMode);

        return "/site/profile";
    }
}
