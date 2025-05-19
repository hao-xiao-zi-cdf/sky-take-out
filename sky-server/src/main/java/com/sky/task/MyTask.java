package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-19
 * Time: 15:56
 */
@Component
@Slf4j
public class MyTask {

//    @Scheduled(cron = "*/3 * * * * ?")
    public void taskOne(){
        log.info("定时任务，每3S执行一次");
    }
}
