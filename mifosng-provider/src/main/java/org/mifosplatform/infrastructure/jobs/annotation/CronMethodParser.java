package org.mifosplatform.infrastructure.jobs.annotation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * Parser to find method which is marked with CronTargetMethod annotation
 * 
 */
public class CronMethodParser {

    private static final String SEARCH_PACKAGE = "org.mifosplatform.";

    private static final String CRON_ANNOTATION_ATTRIBUTE_NAME = "jobName";

    private static final String RESOURCE_PATTERN = "**/*.class";

    private static final Map<String, String[]> targetMethosMap = new HashMap<String, String[]>();

    private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

    public static final int CLASS_INDEX = 0;

    public static final int METHOD_INDEX = 1;

    public static String[] findTargetMethodDetails(final String attributeValue) throws IOException {
        if (!targetMethosMap.containsKey(attributeValue)) {
            findAnnotationMethods(CronTarget.class, CRON_ANNOTATION_ATTRIBUTE_NAME);
        }
        return targetMethosMap.get(attributeValue);
    }

    /**
     * method adds all the method names to map with annotation attribute value
     * as key
     */
    private static void findAnnotationMethods(final Class<? extends Annotation> annotationClass, final String attributeName)
            throws IOException {
        final String basePackagePath = ClassUtils.convertClassNameToResourcePath(new StandardEnvironment()
                .resolveRequiredPlaceholders(SEARCH_PACKAGE));
        final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackagePath + "/" + RESOURCE_PATTERN;
        final Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (final Resource resource : resources) {
            if (resource.isReadable()) {
                final MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                final Set<MethodMetadata> metadataSet = metadataReader.getAnnotationMetadata().getAnnotatedMethods(
                        annotationClass.getName());
                if (metadataSet != null && metadataSet.size() > 0) {
                    for (final MethodMetadata metadata : metadataSet) {
                        final Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
                        final JobName attributeValue = (JobName) attributes.get(attributeName);
                        final String className = metadata.getDeclaringClassName();
                        final String[] mapVal = { className, metadata.getMethodName() };
                        targetMethosMap.put(attributeValue.toString(), mapVal);
                    }
                }
            }
        }
    }
}
