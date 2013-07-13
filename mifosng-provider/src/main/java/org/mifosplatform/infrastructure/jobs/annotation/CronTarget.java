package org.mifosplatform.infrastructure.jobs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mifosplatform.infrastructure.jobs.service.JobName;

/**
 * Annotation that marks a method to be picked while scheduling a cron jobs.
 * 
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CronTarget {

    JobName jobName();
}
