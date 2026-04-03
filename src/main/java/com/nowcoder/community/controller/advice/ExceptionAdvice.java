package com.nowcoder.community.controller.advice;

import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);
    private final CommentMapper commentMapper;

    public ExceptionAdvice(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.error("服务器内部错误: ", e);
        String requestedWith = request.getHeader("x-requested-with");
        if (requestedWith != null && requestedWith.equals("XMLHttpRequest")) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.println("服务器内部错误");
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }

    }


}
