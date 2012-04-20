package org.mifosng.oauth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.AntPathRequestMatcher;
import org.springframework.security.web.util.RequestMatcher;

public class OAuthConsumerFilterFilterInvocationSecurityMetadataSource
		implements FilterInvocationSecurityMetadataSource {

	private final FilterInvocationSecurityMetadataSource metadataSource;

	public OAuthConsumerFilterFilterInvocationSecurityMetadataSource() {
		LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
		RequestMatcher key = new AntPathRequestMatcher("/sparklr/**");
		Collection<ConfigAttribute> value = new ArrayList<ConfigAttribute>();
		value.add(new SecurityConfig("sparklrPhotos"));
		requestMap.put(key, value);

		this.metadataSource = new DefaultFilterInvocationSecurityMetadataSource(
				requestMap);
	}

	@Override
	public Collection<ConfigAttribute> getAttributes(final Object object)
			throws IllegalArgumentException {
		return this.metadataSource.getAttributes(object);
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return this.metadataSource.getAllConfigAttributes();
	}

	@Override
	public boolean supports(final Class<?> clazz) {
		return this.metadataSource.supports(clazz);
	}
}