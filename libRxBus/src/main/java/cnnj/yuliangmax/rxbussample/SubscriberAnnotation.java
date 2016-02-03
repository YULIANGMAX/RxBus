package cnnj.yuliangmax.rxbussample;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SubscriberAnnotation {

    String tag() default "default";

    ThreadScheduler scheduler() default ThreadScheduler.MAIN_THREAD;

}
