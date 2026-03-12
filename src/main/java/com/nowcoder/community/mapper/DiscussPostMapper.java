package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //首页查询不用userId 将来其他页面查询需要 offset 起始行号 limit 每页显示的行数
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //@Param注解用于给参数取别名
    //如果需要动态的判断条件并且只有一个参数，并且在 if 里面使用，则必须加别名
    //查询帖子的行数
    int selectDiscussPostRows(@Param("userId") int userId);

    //插入一个帖子
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);


}
