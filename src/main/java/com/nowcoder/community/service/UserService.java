package com.nowcoder.community.service;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.mapper.LoginTicketMapper;
import com.nowcoder.community.util.*;
import org.apache.commons.lang3.StringUtils;
import com.nowcoder.community.dao.UserMapper;

import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
       // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "用户名不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //验证账号

        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该用户名已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        // 1. 构造邮件内容
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("username", user.getUsername());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);


        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ACTIVATION_FAILURE;
        }

        if (user.getStatus() == 1) {

            return ACTIVATION_REPEAT;
        }
        if (!user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_FAILURE;
        }

        userMapper.updateStatus(userId, 1);
        return ACTIVATION_SUCCESS;
    }

    // 登录 因为多种情况所以返回值为Map
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 1. 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        // 2. 验证密码不能为空
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
        }

        // 3. 验证账号是否存在
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号不存在");
            return map;
        }

        // 4. 验证密码是否正确
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误");
            return map;
        }

        // 5. 验证账号是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活");
            return map;
        }

        // 6. 登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        //loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    //退出
    public void logout(String ticket) {
        //loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        if (loginTicket != null) {
            loginTicket.setStatus(1);
            redisTemplate.opsForValue().set(redisKey, loginTicket);
        }
    }

    public LoginTicket findLoginTicket(String ticket) {
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl) {
        //return userMapper.updateHeader (userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        if (rows > 0) {
            clearCache(userId);
        }
        return rows;
    }

    //更新密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        User user = hostHolder.getUser();

        // 验证旧密码是否正确 加密后对比
        String oldPasswordMd5 = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPasswordMd5)) {
            map.put("oldPasswordMsg", "旧密码错误");
            return map;
        }

        // 验证新密码是否为空
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空");
            return map;
        }

        // 更新密码
        user.setPassword(CommunityUtil.md5(newPassword + user.getSalt()));
        userMapper.updatePassword(userId, user.getPassword());

        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 1、优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2 去不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user);
        return user;
    }

    // 3 数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
