/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.mifosplatform.template.domain.Template;
import org.mifosplatform.template.domain.TemplateFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Service
public class TemplateMergeService {
	private final static Logger logger = LoggerFactory.getLogger(TemplateMergeService.class);

    // private final FromJsonHelper fromApiJsonHelper;
    private Map<String, Object> scopes;
    private String authToken;

    // @Autowired
    // public TemplateMergeService(final FromJsonHelper fromApiJsonHelper) {
    // this.fromApiJsonHelper = fromApiJsonHelper;
    // }

    public void setAuthToken(final String authToken) {
        this.authToken = authToken;
    }

    public String compile(final Template template, final Map<String, Object> scopes) throws MalformedURLException, IOException {
        this.scopes = scopes;
        this.scopes.put("static", new TemplateFunctions());

        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache mustache = mf.compile(new StringReader(template.getText()), template.getName());

        final Map<String, Object> mappers = getCompiledMapFromMappers(template.getMappersAsMap());
        this.scopes.putAll(mappers);

        expandMapArrays(scopes);

        final StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, this.scopes);

        return stringWriter.toString();
    }

	private Map<String, Object> getCompiledMapFromMappers(final Map<String, String> data) {
        final MustacheFactory mf = new DefaultMustacheFactory();

        if (data != null) {
            for (final Map.Entry<String, String> entry : data.entrySet()) {
                final Mustache mappersMustache = mf.compile(new StringReader(entry.getValue()), "");
                final StringWriter stringWriter = new StringWriter();

                mappersMustache.execute(stringWriter, this.scopes);
                String url = stringWriter.toString();
                if (!url.startsWith("http")) {
                    url = this.scopes.get("BASE_URI") + url;
                }
                try {
                    this.scopes.put(entry.getKey(), getMapFromUrl(url));
                } catch (final IOException e) {
                	logger.error("getCompiledMapFromMappers() failed", e);
                }
            }
        }
        return this.scopes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapFromUrl(final String url) throws MalformedURLException, IOException {
        final HttpURLConnection connection = getConnection(url);

        final String response = getStringFromInputStream(connection.getInputStream());
        HashMap<String, Object> result = new HashMap<>();
        if (connection.getContentType().equals("text/plain")) {
            result.put("src", response);
        } else {
            result = new ObjectMapper().readValue(response, HashMap.class);
        }
        return result;
    }

    private HttpURLConnection getConnection(final String url) {
        if (this.authToken == null) {
            final String name = SecurityContextHolder.getContext().getAuthentication().getName();
            final String password = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(name, password.toCharArray());
                }
            });
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (this.authToken != null) {
                connection.setRequestProperty("Authorization", "Basic " + this.authToken);
            }
            TrustModifier.relaxHostChecking(connection);

            connection.setDoInput(true);

        } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
        	logger.error("getConnection() failed, return null", e);
        }

        return connection;
    }

    // TODO Replace this with appropriate alternative available in Guava
    private static String getStringFromInputStream(final InputStream is) {
        BufferedReader br = null;
        final StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (final IOException e) {
        	logger.error("getStringFromInputStream() failed", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
    
	@SuppressWarnings("unchecked")
	private void expandMapArrays(Object value) {
		if (value instanceof Map) {
			Map<String, Object> valueAsMap = (Map<String, Object>) value;
			for (Entry<String, Object> valueAsMapEntry : valueAsMap.entrySet()) {
				Object valueAsMapEntryValue = valueAsMapEntry.getValue();
				if (valueAsMapEntryValue instanceof Map) { // JSON Object
					expandMapArrays(valueAsMapEntryValue);
				} else if (valueAsMapEntryValue instanceof Iterable) { // JSON Array
					Iterable<Object> valueAsMapEntryValueIterable = (Iterable<Object>) valueAsMapEntryValue;
					String valueAsMapEntryKey = valueAsMapEntry.getKey();
					int i = 0;
					for (Object object : valueAsMapEntryValueIterable) {
						valueAsMap.put(valueAsMapEntryKey + "#" + i, object);
						++i;
						expandMapArrays(object);
					}
				}
			}
		}		
	}

}
