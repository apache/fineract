/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * This Configuration replaces what formerly was in web.xml. Beans are loaded only when "oauth" Profile is enabled. 
 * 
 * @see <a
 *      href="http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-convert-an-existing-application-to-spring-boot">#howto-convert-an-existing-application-to-spring-boot</a>
 */
@Configuration
@Profile("oauth")
public class WebXmlOauthConfiguration {


    @Bean
    public Filter springSecurityFilterChain() {
        return new DelegatingFilterProxy();
    }

    @Bean
    public ServletRegistrationBean jersey() {
        Servlet jerseyServlet = new SpringServlet();
        ServletRegistrationBean jerseyServletRegistration = new ServletRegistrationBean();
        jerseyServletRegistration.setServlet(jerseyServlet);
        jerseyServletRegistration.addUrlMappings("/api/v1/*");
        jerseyServletRegistration.setName("jersey-servlet");
        jerseyServletRegistration.setLoadOnStartup(1);
        jerseyServletRegistration.addInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
//        jerseyServletRegistration.addInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters",
//                ResponseCorsFilter.class.getName());
        jerseyServletRegistration.addInitParameter("com.sun.jersey.config.feature.DisableWADL", "true");
        // debugging for development:
        // jerseyServletRegistration.addInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters",
        // LoggingFilter.class.getName());
        return jerseyServletRegistration;
    }

    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(dispatcherServlet);
        registrationBean.addUrlMappings("/api/oauth/token");
        return registrationBean;
    }

}