const express = require('express');
const base64url = require('base64url');
const crypto = require('crypto');
const open = (...args) => import('open').then(m => m.default(...args));


// fetch workaround for CommonJS
const fetch = (...args) => import('node-fetch').then(({ default: fetch }) => fetch(...args));

// === Config ===
const clientId = 'frontend-client';
const authServer = 'https://localhost:8443/fineract-provider';
const redirectUri = 'http://localhost:3000/callback';
const scope = 'read';

// === PKCE values ===
const codeVerifier = base64url(crypto.randomBytes(32));
const codeChallenge = base64url(crypto.createHash('sha256').update(codeVerifier).digest());

console.log('code_verifier:', codeVerifier);
console.log('code_challenge:', codeChallenge);

// === Express callback server ===
const app = express();

app.get('/callback', async (req, res) => {
    process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';
    const code = req.query.code;
    const state = req.query.state;

    console.log('✅ Authorization code received:', code);

    // Token request
    const tokenResponse = await fetch(`${authServer}/oauth2/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code,
            redirect_uri: redirectUri,
            client_id: clientId,
            code_verifier: codeVerifier
        })
    });

    const token = await tokenResponse.json();

    console.log('📦 Token response:', token);
    res.send('✅ Authorization complete. You may close this tab.');
    process.exit(0);
});

app.listen(3000, () => {
    console.log(`🚀 Listening on ${redirectUri}`);

    const authUrl = `${authServer}/oauth2/authorize?` + new URLSearchParams({
        response_type: 'code',
        client_id: clientId,
        redirect_uri: redirectUri,
        scope,
        state: 'xyz',
        code_challenge: codeChallenge,
        code_challenge_method: 'S256'
    });

    console.log('🌐 Opening browser for login:', authUrl);
    open(authUrl);
});
