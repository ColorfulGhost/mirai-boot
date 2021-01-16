package moe.iacg.miraiboot.annotation;


import moe.iacg.miraiboot.constants.Commands;

import java.lang.annotation.*;

/**
 * @author Ghost
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandPrefix {
    Commands command();

    String prefix() default "/" ;

}
