package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-11
 * Time: 14:27
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//用于标识类中的哪些方法需要进行公共字段的自动填充
public @interface AutoFill {
    OperationType value();
}
