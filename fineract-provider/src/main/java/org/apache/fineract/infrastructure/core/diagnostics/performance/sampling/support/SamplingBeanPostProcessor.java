/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingConfiguration;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingService;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingServiceFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class SamplingBeanPostProcessor implements BeanPostProcessor {

    private SamplingConfiguration samplingConfiguration;
    private SamplingServiceFactory samplingServiceFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (isSamplingEnabled()) {
            Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
            boolean isSamplingConfiguredForBean = samplingConfiguration.isSamplingConfiguredForClass(beanClass);
            if (isSamplingConfiguredForBean) {
                log.info("Sampling is enabled for {}", beanClass);
                SamplingService samplingService = samplingServiceFactory.forClass(beanClass);
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setTarget(bean);
                proxyFactory.addAdvice(new SamplingMethodInterceptor(samplingService));
                return proxyFactory.getProxy();
            }
        }

        return bean;
    }

    private boolean isSamplingEnabled() {
        return samplingConfiguration != null && samplingConfiguration.isSamplingEnabled();
    }

    @Autowired
    public void setSamplingServiceFactory(SamplingServiceFactory samplingServiceFactory) {
        this.samplingServiceFactory = samplingServiceFactory;
    }

    @Autowired
    public void setSamplingConfiguration(SamplingConfiguration samplingConfiguration) {
        this.samplingConfiguration = samplingConfiguration;
    }
}
