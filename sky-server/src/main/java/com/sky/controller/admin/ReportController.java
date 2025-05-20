package com.sky.controller.admin;

import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.TurnoverStatisticsService;
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
     * 根据指定时间区间统计用户量
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("根据指定时间区间统计用户量")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){
        UserReportVO userReportVO = turnoverStatisticsService.userStatistics(begin,end);
        return Result.success(userReportVO);
    }
}
