package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    // 插入登录凭证
    @Insert("insert into login_ticket(user_id, ticket, status, expired) " +
            "values(#{userId}, #{ticket}, #{status}, #{expired})") //对象中的属性
    @Options(useGeneratedKeys = true, keyProperty = "id") // 实现数据库自增主键的回填
    int insertLoginTicket(LoginTicket loginTicket);
    // 根据凭证查询登录凭证
    @Select("select id, user_id, ticket, status, expired from login_ticket where ticket = #{ticket}")
    LoginTicket selectByTicket(String ticket);
    // 更新登录凭证状态
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
