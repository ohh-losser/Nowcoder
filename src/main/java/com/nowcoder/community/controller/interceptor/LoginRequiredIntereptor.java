package com.nowcoder.community.controller.interceptor;


import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginRequiredIntereptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否有LoginRequired注解
//        handler 是个通用的对象。
//        HandlerMethod 特指那些 Controller 中的处理方法。
//        为什么要转换：只有 HandlerMethod 才能获取到我们自己定义的注解（如 @LoginRequired）。
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
            if (loginRequired != null) {
                // 检查用户是否登录
                User user = hostHolder.getUser();
                if (user == null) {
                    // 未登录，跳转到登录页面
                    response.sendRedirect(request.getContextPath() + "/login");
                    return false;
                }
            }
        }
        return true;
    }

}
