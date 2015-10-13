package org.sdet.junit.extension.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PriorityFilter {
    Class<?>[] value();
}
