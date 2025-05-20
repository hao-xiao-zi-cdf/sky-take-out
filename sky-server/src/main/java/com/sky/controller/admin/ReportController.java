package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.TurnoverStatisticsService;
import com.sky.service.UserService;
import com.sky.service.impl.OrderServiceImpl;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-20
 * Time: 11:13
 */
@RestController
@Api(tags = "管理端统计相关接口")
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {

    @Autowired
    private TurnoverStatisticsService turnoverStatisticsService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    /**
     * 根据指定时间区间统计每一天的营业额
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("根据指定时间区间统计每一天的营业额")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){
        TurnoverReportVO turnoverReportVO = turnoverStatisticsService.getTurnover(begin,end);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户数据统计")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){

        UserReportVO userReportVO = userService.userStatistics(begin, end);

        return Result.success(userReportVO);
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单数据统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        OrderReportVO orderReportVO = orderService.ordersStatistics(begin,end);
        return Result.success(orderReportVO);
    }
}
