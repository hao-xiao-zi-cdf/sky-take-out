package com.sky.mapper;

import com.sky.entity.User;
import com.sky.vo.UserReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-15
 * Time: 17:01
 */
@Mapper
public interface UserMapper {
    /**
     * 根据openId查询用户
     * @param openId
     * @return
     */
    @Select("select * from user where openid = #{openId}")
    User getByOpenId(String openId);

    /**
     * 新增用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id=#{id}")
    User getById(Long userId);

    /**
     * 获取当天的（总,新）用户数量
     * @param
     */
    Double userSumByMap(Map<String,Object> map);
}
