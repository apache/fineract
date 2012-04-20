package org.mifosng.platform.oauthconsumer.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth.common.signature.SharedConsumerSecret;
import org.springframework.security.oauth.common.signature.SignatureSecret;
import org.springframework.security.oauth.provider.ResourceSpecificConsumerDetails;

/**
 * TODO - revisit when tackling better administration support for oauth consumer control with OAuth 2 implementation.
 */
@Entity
@Table(name = "admin_oauth_consumer_application")
public class OauthConsumerDetail extends AbstractPersistable<Long> implements
		ResourceSpecificConsumerDetails {

	@Column(name = "app_key", nullable = false, unique = true, length=50)
	private String consumerKey;
	
	@Column(name = "app_shared_secret", nullable = false, unique = true, length=50)
	private String consumerSharedSecret;

	@Column(name = "app_name", nullable = false, unique = true, length=100)
	private String consumerName;
	
	@SuppressWarnings("unused")
	@Column(name = "app_description", nullable = true, length=500)
	private String consumerDescription;
	
	@SuppressWarnings("unused")
	@Column(name = "app_developedby", nullable = true, length=100)
	private String consumerDevelopedBy;
	
	@SuppressWarnings("unused")
	@Column(name = "app_location_url", nullable = true, length=200)
	private String consumerLocationUrl;

	@Column(name = "resource_name", nullable = false)
	private String resourceName;

	@Column(name = "resource_description", nullable = false)
	private String resourceDescription;

	@Transient
	private List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
	
	@Transient
	private boolean requiredToObtainAuthenticatedToken = true;

	// for jpa/hibernate
	protected OauthConsumerDetail() {
		//
	}

	public OauthConsumerDetail(String consumerKey, String consumerName, String consumerDescription, String consumerDevelopedby, String consumerLocationUrl,
			String resourceName, String resourceDesription) {
		this.consumerKey = consumerKey;
		this.consumerName = consumerName;
		this.consumerDescription = consumerDescription;
		this.consumerDevelopedBy = consumerDevelopedby;
		this.consumerLocationUrl = consumerLocationUrl;
		this.resourceName = resourceName;
		resourceDescription = resourceDesription;
	}

	@Override
	public String getConsumerKey() {
		return this.consumerKey;
	}

	@Override
	public String getConsumerName() {
		return this.consumerName;
	}

	@Override
	public SignatureSecret getSignatureSecret() {
		return new SharedConsumerSecret(this.consumerSharedSecret);
	}

	@Override
	public List<GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getResourceName() {
		return this.resourceName;
	}

	@Override
	public String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * Whether this consumer is required to obtain an authenticated oauth token.
	 * 
	 * @return Whether this consumer is required to obtain an authenticated
	 *         oauth token.
	 */
	public boolean isRequiredToObtainAuthenticatedToken() {
		return requiredToObtainAuthenticatedToken;
	}

	/**
	 * Whether this consumer is required to obtain an authenticated oauth token.
	 * 
	 * @param requiredToObtainAuthenticatedToken
	 *            Whether this consumer is required to obtain an authenticated
	 *            oauth token.
	 */
	public void setRequiredToObtainAuthenticatedToken(
			boolean requiredToObtainAuthenticatedToken) {
		this.requiredToObtainAuthenticatedToken = requiredToObtainAuthenticatedToken;
	}
	
	
}