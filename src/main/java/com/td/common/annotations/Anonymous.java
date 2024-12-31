package com.td.common.annotations;

import java.lang.annotation.*;

/**
 * 匿名访问不鉴权注解
 *
 * @author Td
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Anonymous {
}
