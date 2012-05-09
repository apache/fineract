package org.mifosng.oauth;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth.common.OAuthConsumerParameter;
import org.springframework.security.oauth.common.OAuthException;
import org.springframework.security.oauth.common.signature.OAuthSignatureMethod;
import org.springframework.security.oauth.common.signature.SignatureSecret;
import org.springframework.security.oauth.common.signature.UnsupportedSignatureMethodException;
import org.springframework.security.oauth.provider.ConsumerAuthentication;
import org.springframework.security.oauth.provider.ConsumerCredentials;
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.InvalidOAuthParametersException;
import org.springframework.security.oauth.provider.filter.ProtectedResourceProcessingFilter;
import org.springframework.security.oauth.provider.token.OAuthProviderToken;

public class CustomProtectedResourceProcessingFilter extends
		ProtectedResourceProcessingFilter {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
	    HttpServletRequest request = (HttpServletRequest) servletRequest;
	    HttpServletResponse response = (HttpServletResponse) servletResponse;

	    if (!skipProcessing(request)) {
	      if (requiresAuthentication(request, response, chain)) {
	        if (!allowMethod(request.getMethod().toUpperCase())) {

	          response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	          return;
	        }

	        try {
	          Map<String, String> oauthParams = getProviderSupport().parseParameters(request);

	          if (parametersAreAdequate(oauthParams)) {

	            String consumerKey = oauthParams.get(OAuthConsumerParameter.oauth_consumer_key.toString());
	            if (consumerKey == null) {
	              throw new InvalidOAuthParametersException(messages.getMessage("OAuthProcessingFilter.missingConsumerKey", "Missing consumer key."));
	            }

	            //load the consumer details.
	            ConsumerDetails consumerDetails = getConsumerDetailsService().loadConsumerByConsumerKey(consumerKey);

	            //validate the parameters for the consumer.
	            validateOAuthParams(consumerDetails, oauthParams);

	            //extract the credentials.
	            String token = oauthParams.get(OAuthConsumerParameter.oauth_token.toString());
	            String signatureMethod = oauthParams.get(OAuthConsumerParameter.oauth_signature_method.toString());
	            String signature = oauthParams.get(OAuthConsumerParameter.oauth_signature.toString());
	            String signatureBaseString = getProviderSupport().getSignatureBaseString(request);
	            ConsumerCredentials credentials = new ConsumerCredentials(consumerKey, signature, signatureMethod, signatureBaseString, token);

	            //create an authentication request.
	            ConsumerAuthentication authentication = new ConsumerAuthentication(consumerDetails, credentials, oauthParams);
	            authentication.setDetails(createDetails(request, consumerDetails));

	            Authentication previousAuthentication = SecurityContextHolder.getContext().getAuthentication();
	            try {
	              //set the authentication request (unauthenticated) into the context.
	              SecurityContextHolder.getContext().setAuthentication(authentication);

	              //validate the signature.
	              if (token == null) {
	            	  validateSignature(authentication);
	              } else {
	            	  if (!"OPTIONS".equalsIgnoreCase(request.getMethod().toUpperCase())) {
		            	  validateSignature(authentication);
		              }
	              }

	              //mark the authentication request as validated.
	              authentication.setSignatureValidated(true);

	              //mark that processing has been handled.
	              request.setAttribute(OAUTH_PROCESSING_HANDLED, Boolean.TRUE);

	              //go.
	              onValidSignature(request, response, chain);
	            }
	            finally {
	              //clear out the consumer authentication to make sure it doesn't get cached.
	              resetPreviousAuthentication(previousAuthentication);
	            }
	          }
	          else if (!isIgnoreInadequateCredentials()) {
	            throw new InvalidOAuthParametersException(messages.getMessage("OAuthProcessingFilter.missingCredentials", "Inadequate OAuth consumer credentials."));
	          }
	          else {
	            chain.doFilter(request, response);
	          }
	        }
	        catch (AuthenticationException ae) {
	          fail(request, response, ae);
	        }
	        catch (ServletException e) {
	          if (e.getRootCause() instanceof AuthenticationException) {
	            fail(request, response, (AuthenticationException) e.getRootCause());
	          }
	          else {
	            throw e;
	          }
	        }
	      }
	      else {
	        chain.doFilter(servletRequest, servletResponse);
	      }
	    }
	    else {
	      chain.doFilter(servletRequest, servletResponse);
	    }
	  }
	
	@Override
	protected void validateSignature(ConsumerAuthentication authentication)
			throws AuthenticationException {
		SignatureSecret secret = authentication.getConsumerDetails()
				.getSignatureSecret();
		String token = authentication.getConsumerCredentials().getToken();
		OAuthProviderToken authToken = null;
		if (token != null && !"".equals(token)) {
			authToken = getTokenServices().getToken(token);
		}

		String signatureMethod = authentication.getConsumerCredentials()
				.getSignatureMethod();
		OAuthSignatureMethod method;
		try {
			method = getSignatureMethodFactory().getSignatureMethod(
					signatureMethod, secret,
					authToken != null ? authToken.getSecret() : null);
		} catch (UnsupportedSignatureMethodException e) {
			throw new OAuthException(e.getMessage(), e);
		}

		String signatureBaseString = authentication.getConsumerCredentials()
				.getSignatureBaseString();
		String signature = authentication.getConsumerCredentials()
				.getSignature();
		method.verify(signatureBaseString, signature);
	}
}
