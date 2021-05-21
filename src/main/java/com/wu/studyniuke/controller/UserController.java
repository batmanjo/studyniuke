package com.wu.studyniuke.controller;

import com.wu.studyniuke.annotation.LoginRequired;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * @author me
 * @create 2021-05-21-16:11
 */
@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

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
}
