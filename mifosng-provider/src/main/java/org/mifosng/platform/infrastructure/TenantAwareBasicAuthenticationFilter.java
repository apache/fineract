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
 *
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
				// ignore to allow 'preflight' requests from AJAX applications in different domain
			} else {
			
				final String tenantId = request.getHeader(tenantRequestHeader);
		
				if (tenantId == null && exceptionIfHeaderMissing) {
					throw new InvalidTenantIdentiferException(tenantRequestHeader + " header not found in request.");
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
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, tenantRequestHeader + " header not found in request.");
		} finally {
			
		}
	}
}