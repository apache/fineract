package org.mifosng.platform.infrastructure;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * A customised version of spring security's {@link BasicAuthenticationFilter}.
 * 
 * This filter is responsible for extracting multi-tenant and basic auth
 * credentials from the request and checking that the details provided are
 * valid.
 * 
 * If multi-tenant and basic auth credentials are valid, the details of the
 * tenant are stored in {@link MifosPlatformTenant} and stored in a
 * {@link ThreadLocal} variable for this request using
 * {@link ThreadLocalContextUtil}.
 * 
 * If multi-tenant and basic auth credentials are invalid, a http error response
 * is returned.
 */
public class TenantAwareBasicAuthenticationFilter extends BasicAuthenticationFilter {

	@Autowired
	private TenantDetailsService tenantDetailsService;
	
	private String tenantRequestHeader = "X-Mifos-Platform-TenantId";
	private boolean exceptionIfHeaderMissing = true;
	
	public TenantAwareBasicAuthenticationFilter(AuthenticationManager authenticationManager, AuthenticationEntryPoint authenticationEntryPoint) {
		super(authenticationManager, authenticationEntryPoint);
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		try {
		
			if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
				// ignore to allow 'preflight' requests from AJAX applications in different origin (domain name)
			} else {
			
				String tenantId = request.getHeader(tenantRequestHeader);
				if (org.apache.commons.lang.StringUtils.isBlank(tenantId)) {
					tenantId = request.getParameter("tenantIdentifier");
				}
		
				if (tenantId == null && exceptionIfHeaderMissing) {
					throw new InvalidTenantIdentiferException("No tenant identifier found: Add request header of '" + tenantRequestHeader +
							"' or add the parameter 'tenantIdentifier' to query string of request URL.");
				}
				
				// check tenants database for tenantId
				MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById(tenantId);
				
				ThreadLocalContextUtil.setTenant(tenant);
			}
		
			super.doFilter(req, res, chain);
		} catch (InvalidTenantIdentiferException e) {
			// deal with exception at low level
			SecurityContextHolder.getContext().setAuthentication(null);

			response.addHeader("WWW-Authenticate", "Basic realm=\"" + "Mifos Platform API" + "\"");
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} finally {
			
		}
	}
}