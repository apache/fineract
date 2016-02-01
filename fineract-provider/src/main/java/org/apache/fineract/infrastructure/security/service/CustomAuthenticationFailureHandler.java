/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    protected final Log logger = LogFactory.getLog(getClass());

    private String defaultFailureUrl;
    private boolean forwardToDestination = false;
    private boolean allowSessionCreation = true;
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public CustomAuthenticationFailureHandler() {}

    /**
     * Performs the redirect or forward to the {@code defaultFailureUrl} if set,
     * otherwise returns a 401 error code.
     * <p>
     * If redirecting or forwarding, {@code saveException} will be called to
     * cache the exception for use in the target view.
     */
    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException exception) throws IOException, ServletException {

        if (this.defaultFailureUrl == null) {
            this.logger.debug("No failure URL set, sending 401 Unauthorized error");

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + exception.getMessage());
        } else {
            saveException(request, exception);

            if (this.forwardToDestination) {
                this.logger.debug("Forwarding to " + this.defaultFailureUrl);

                request.getRequestDispatcher(this.defaultFailureUrl).forward(request, response);
            } else {
                this.logger.debug("Redirecting to " + this.defaultFailureUrl);

                final String oauthToken = request.getParameter("oauth_token");
                request.setAttribute("oauth_token", oauthToken);
                final String url = this.defaultFailureUrl + "?oauth_token=" + oauthToken;
                this.redirectStrategy.sendRedirect(request, response, url);
            }
        }
    }

    /**
     * Caches the {@code AuthenticationException} for use in view rendering.
     * <p>
     * If {@code forwardToDestination} is set to true, request scope will be
     * used, otherwise it will attempt to store the exception in the session. If
     * there is no session and {@code allowSessionCreation} is {@code true} a
     * session will be created. Otherwise the exception will not be stored.
     */
    protected final void saveException(final HttpServletRequest request, final AuthenticationException exception) {
        if (this.forwardToDestination) {
            request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
        } else {
            final HttpSession session = request.getSession(false);

            if (session != null || this.allowSessionCreation) {
                request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
            }
        }
    }

    /**
     * The URL which will be used as the failure destination.
     * 
     * @param defaultFailureUrl
     *            the failure URL, for example "/loginFailed.jsp".
     */
    public void setDefaultFailureUrl(final String defaultFailureUrl) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), "'" + defaultFailureUrl + "' is not a valid redirect URL");
        this.defaultFailureUrl = defaultFailureUrl;
    }

    protected boolean isUseForward() {
        return this.forwardToDestination;
    }

    /**
     * If set to <tt>true</tt>, performs a forward to the failure destination
     * URL instead of a redirect. Defaults to <tt>false</tt>.
     */
    public void setUseForward(final boolean forwardToDestination) {
        this.forwardToDestination = forwardToDestination;
    }

    /**
     * Allows overriding of the behaviour when redirecting to a target URL.
     */
    public void setRedirectStrategy(final RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    protected RedirectStrategy getRedirectStrategy() {
        return this.redirectStrategy;
    }

    protected boolean isAllowSessionCreation() {
        return this.allowSessionCreation;
    }

    public void setAllowSessionCreation(final boolean allowSessionCreation) {
        this.allowSessionCreation = allowSessionCreation;
    }
}