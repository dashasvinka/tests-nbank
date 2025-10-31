package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KnownIssue {
    String ticket();
    String description() default "";
    boolean ignoreFailure() default true;
    String[] onlyForArgs() default {};
}