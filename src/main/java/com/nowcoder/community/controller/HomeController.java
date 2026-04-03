package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//web层 表示这是一个处理 HTTP 请求的控制器。
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //**作用**：建立请求 URL 和处理方法之间的映射关系。
    //**特点**：**默认匹配所有请求方式**。不管你是 GET、POST、PUT 还是 DELETE 发过来的请求，只要路径对，它全接。
    //    **位置**：
    //
    //            - **在类上**：定义一级路径（模块名）。
    //            - **在方法上**：定义二级路径（具体功能名）。
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    //参数 springmvc会自动实例化model和page，并将page注入model
    //所以，在thymeleaf中可以直接访问Page对象中的数据
    public String getIndexPage(Model model, Page page) {

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPost(0, page.getOffset(), page.getLimit());

        //Object 代表的是“任何类型的 Java 对象”。
        //第一个 Object 是 DiscussPost 对象：
        //当你执行 map.put("post", post) 时，这里的 Object 实际上是一个帖子实例。
        //第二个 Object 是 User 对象：
        //当你执行 map.put("user", userService.findUserById(...)) 时，这里的 Object 实际上是一个用户实例。
        List<Map<String, Object>> discussPosts = new ArrayList<Map<String, Object>>();

        if(list != null) {
            for(DiscussPost post : list) {
                //在前端 HTML 模板（比如 Thymeleaf）里，通过这个 map，你可以非常方便地写出这样的代码：
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                //赞
                // 1. 确保 post 不为空，且 id 有效
                if (post != null) {
                    long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                    map.put("likeCount", likeCount);
                } else {
                    map.put("likeCount", 0);
                }

                discussPosts.add(map);
            }
        }

        //在你的这段代码场景中（Spring MVC 控制器）：
        //这个 Map 最终是传给 Thymeleaf 模板引擎的。
        //Thymeleaf 非常聪明，它在后台会自动识别出 Object 背后真实的类型。所以你在 HTML 里写 ${map.post.title} 时，它能自动知道去 DiscussPost 对象里找 title 属性。

        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }


    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

}
