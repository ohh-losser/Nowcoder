package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    private static final Logger log = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {
        try {
            // 1. 创建一个空的 MIME 邮件对象
            MimeMessage message = mailSender.createMimeMessage();

            // 2. 使用 Spring 提供的 Helper 类来辅助填充邮件内容
            MimeMessageHelper helper = new MimeMessageHelper(message);

            // 3. 设置邮件的各项属性
            helper.setFrom(from);       // 发件人
            helper.setTo(to);           // 收件人
            helper.setSubject(subject); // 邮件主题

            // 4. 设置邮件正文
            helper.setText(content, true);

            // 5. 执行发送
            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            // 6. 异常处理
            log.error("发送邮件失败: " + e.getMessage());
        }
    }
}
