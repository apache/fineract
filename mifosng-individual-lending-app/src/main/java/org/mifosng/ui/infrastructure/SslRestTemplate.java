package org.mifosng.ui.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@Component
public class SslRestTemplate extends RestTemplate {

	@Autowired
	public SslRestTemplate(final CustomClientHttpRequestFactory simpleClientHttpRequestFactory) {
		super(simpleClientHttpRequestFactory);
	}
}