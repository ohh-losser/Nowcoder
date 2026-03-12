package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

//本质上就是 @Component**，只是为了给程序员看，区分不同的层。
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    //再简单的业务 也要controller层 来处理业务逻辑

    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
        //这里discusspost 里面 又的是用户的id 不是用户的名称，选择拿到id后再查一下用户名，之后再用redis存储，方便存储又数据比较直观
        //舍弃了sql id和用户名称的关联
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost) {
        if(discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //转移HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤铭感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

}
