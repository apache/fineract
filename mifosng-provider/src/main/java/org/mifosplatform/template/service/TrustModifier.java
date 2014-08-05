/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template.service;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("unused")
public class TrustModifier {

    private static final TrustingHostnameVerifier TRUSTING_HOSTNAME_VERIFIER = new TrustingHostnameVerifier();
    private static SSLSocketFactory factory;

    /**
     * Call this with any HttpURLConnection, and it will modify the trust
     * settings if it is an HTTPS connection.
     */
    public static void relaxHostChecking(final HttpURLConnection conn) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        if (conn instanceof HttpsURLConnection) {
            final HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            final SSLSocketFactory factory = prepFactory(httpsConnection);
            httpsConnection.setSSLSocketFactory(factory);
            httpsConnection.setHostnameVerifier(TRUSTING_HOSTNAME_VERIFIER);
        }
    }

    static synchronized SSLSocketFactory prepFactory(final HttpsURLConnection httpsConnection) throws NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException {

        if (factory == null) {
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new AlwaysTrustManager() }, null);
            factory = ctx.getSocketFactory();
        }
        return factory;
    }

    private static final class TrustingHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            return true;
        }
    }

    private static class AlwaysTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}