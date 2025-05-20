package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-15
 * Time: 16:17
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 使用微信登录功能实现用户登录模块
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        //1.调用微信平台提供的接口获取openId
        String openId = getOpenId(userLoginDTO);

        //2.判断openId是否有效，无效抛出登录异常
        if(openId == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //3.根据openId到数据库中查找
        User user = userMapper.getByOpenId(openId);

        //4.判断是否为新用户
        if(user == null){
            //新用户：构造user对象，向user表中插入记录
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        //5.返回user对象
        return user;
    }

    /**
     * 获取openId
     * @param userLoginDTO
     * @return
     */
    private String getOpenId(UserLoginDTO userLoginDTO){
        //设置查询参数
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",userLoginDTO.getCode());
        map.put("grant_type","authorization_code");
        //使用HttpClient客户端向接口平台发送HttpGet请求
        String response = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(response);
        String openid = jsonObject.getString("openid");

        return openid;
    }

    /**
     * 根据指定时间区间统计用户量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        //根据指定日期区间计算出每一天的日期
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> userSumList = new ArrayList<>();
        List<Integer> newUserSumList = new ArrayList<>();

        for(LocalDate date : dateList){
            //获取每一天的最小最大时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //将查询条件封装到map集合
            Map<String,Object> map = new HashMap<>();
            map.put("endTime",endTime);

            //select sum(1) from user where create_time > ? and create_time < ?

            //获取当天内新增的用户数量
            Double userSum = userMapper.userSumByMap(map);

            map.put("beginTime",beginTime);

            //获取到当天结束前的所有用户数量
            Double newUserSum = userMapper.userSumByMap(map);

            //判断获取的值是否为null
            userSum = userSum == null ? 0.0 : userSum;
            newUserSum = newUserSum == null ? 0.0 : newUserSum;

            userSumList.add(userSum.intValue());
            newUserSumList.add(newUserSum.intValue());
        }

        //将获取的数据转化为指定格式
        String dateString = StringUtils.join(dateList, ",");
        String userSumString = StringUtils.join(userSumList,",");
        String newUserSumString = StringUtils.join(newUserSumList,",");

        return UserReportVO.builder()
                .dateList(dateString)
                .totalUserList(userSumString)
                .newUserList(newUserSumString)
                .build();
    }
}
