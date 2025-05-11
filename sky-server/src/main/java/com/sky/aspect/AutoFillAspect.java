package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-11
 * Time: 14:34
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    //编写切面表达式
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void AutoFillPointcut(){}

    //前置通知
    @Before("AutoFillPointcut()")
    public void autoFill(JoinPoint joinPoint){
        //1.获取AutoFill注解类型(UPDATE,INSERT)，判断进行数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType value = annotation.value();

        //2.获取方法的参数列表第一个参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];

        //3.获取赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long userId = BaseContext.getCurrentId();

        //4.根据数据库不同操作，利用反射机制获取不同属性的赋值方法进行赋值
        if(value.equals(OperationType.INSERT)){
            //插入操作，为4个公共属性赋值
            try {
                //获取赋值方法
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                //调用invoke方法
                setCreateTime.invoke(entity,now);
                setUpdateTime.invoke(entity,now);
                setCreateUser.invoke(entity,userId);
                setUpdateUser.invoke(entity,userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else{
            //更新操作，为2个公共属性赋值
            try {
                //获取赋值方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                //调用invoke方法
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
