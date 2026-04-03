package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String upload;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());

        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式错误");
            return "/site/setting";
        }

        //生成随机=文件名
        fileName = CommunityUtil.generateUUID() + suffix;

        File dest = new File(upload + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            model.addAttribute("error", "上传文件失败");
            throw new RuntimeException("上传文件失败", e);
        }

        // 更新当前用户的头像路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";

    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = upload + "/" + fileName;
        // 文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        response.setContentType("image/" + suffix);

        try (FileInputStream fis = new FileInputStream(fileName);
             OutputStream out = response.getOutputStream();){

            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                out.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像文件失败: " + e.getMessage());
        }

    }

    //修改密码
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword,
                                 String newPassword,
                                 String confirmPassword,
                                 Model model) {
        //新密码俩次不一样
        if (!StringUtils.equals(newPassword, confirmPassword)) {
            model.addAttribute("confirmPasswordMsg", "两次输入的密码不一致");
            return "/site/setting";
        }

        Map<String, Object> map = userService.updatePassword(hostHolder.getUser().getId(), oldPassword, newPassword);
        if (map.isEmpty()) {
            model.addAttribute("msg", "密码更新成功");
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {

        //logger.debug("个人主页 userId: {}", userId);

        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("用户不存在");
        }

        //用户
        model.addAttribute("user", user);

        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = followService.findFollowerCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followerCount", followerCount);

        // 关注状态
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
        }

        model.addAttribute("hasFollowed", hasFollowed);


        return "/site/profile";
    }
}
