package org.apache.fineract.infrastructure.security.config;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.data.TenantAuthenticationDetails;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;

import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty("fineract.security.oauth.enabled")
@EnableConfigurationProperties(ClientProperties.class)
public class AuthorizationServerConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())             // only OAuth2 endpoints
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login"))
                )
                .apply(authorizationServerConfigurer);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationDetailsSource(tenantAuthDetailsSource())
                        .permitAll()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

        // .authenticationDetailsSource(tenantAuthDetailsSource())
        return http.build();
    }

    @Bean
    public OncePerRequestFilter tenantFromBearerFilter(BasicAuthTenantDetailsService tenantDetailsService) {
        BearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return new OncePerRequestFilter() {
            @Override protected void doFilterInternal(@NotNull HttpServletRequest req, @NotNull HttpServletResponse res, @NotNull FilterChain chain)
                    throws ServletException, java.io.IOException {
                try {
                    String token = resolver.resolve(req);
                    if (token != null) {
                        var jwt = JWTParser.parse(token); // not validated here!
                        var claims = jwt.getJWTClaimsSet();
                        Object t = claims.getClaim("tenant");
                        if (t instanceof String s && !s.isBlank()) {
                            ThreadLocalContextUtil.setTenant(tenantDetailsService.loadTenantById(s, false));
                        }
                    }
                    chain.doFilter(req, res);
                } catch (Exception e) {
                    chain.doFilter(req, res); // don't block; real auth will fail later if token is bad
                } finally {
                    ThreadLocalContextUtil.reset();
                }
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    @Bean
    public RegisteredClientRepository registeredClientRepository(ClientProperties clientProps) {
        List<RegisteredClient> clients = clientProps.getRegistrations().entrySet().stream()
                .map(entry -> {
                    ClientProperties.Registration reg = entry.getValue();
                    return RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId(reg.getClientId())
                            .scopes(scopes -> scopes.addAll(reg.getScopes()))
                            .authorizationGrantTypes(grants -> reg.getAuthorizationGrantTypes()
                                    .forEach(grant -> grants.add(new AuthorizationGrantType(grant))))
                            .redirectUris(uris -> uris.addAll(reg.getRedirectUris()))
                            .clientSettings(ClientSettings.builder()
                                    .requireAuthorizationConsent(reg.isRequireAuthorizationConsent())
                                    .build())
                            .build();
                })
                .toList();

        return new InMemoryRegisteredClientRepository(clients);
    }

    @Bean
    @Scope("prototype")
    public AuthenticationDetailsSource<HttpServletRequest, TenantAuthenticationDetails> tenantAuthDetailsSource() {
        return request -> {
            String tenantId = request.getParameter("tenantId");
            String username = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY); // "username"
            String password = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY); // "password"
            return new TenantAuthenticationDetails(username, tenantId, password);
        };
    }

    @Bean public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            UsernamePasswordAuthenticationToken authentication =
                context.getPrincipal();
            // TenantAuthenticationDetails details = (TenantAuthenticationDetails) authentication.getDetails();
            TenantAuthenticationDetails details = (TenantAuthenticationDetails) authentication.getDetails();
            //TenantAuthenticationDetails details = new TenantAuthenticationDetails("mifos", "default", "password");

            AppUser appUser = (AppUser) authentication.getPrincipal();
            List<String> roles = appUser
                    .getRoles()
                    .stream()
                    .map(Role::getName).toList();
            List<String> scope = appUser
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            List<String> finalScope = scope.isEmpty() ? List.of("all") : scope;
            context.getClaims()
                    .claim("scope", finalScope)
                    .claim("role", roles)
                    .claim("tenant", details.getTenantId());
        };
    }
}
