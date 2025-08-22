package org.apache.fineract.infrastructure.security.config;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.fineract.infrastructure.security.data.TenantAuthenticationDetails;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
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
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .formLogin(withDefaults());
                 // .authenticationDetailsSource(tenantAuthDetailsSource())
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("frontend-client")
                .scope("read")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // public client
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:3000/callback") // your frontend
                .build();
        return new InMemoryRegisteredClientRepository(client);
    }

    @Bean public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            UsernamePasswordAuthenticationToken authentication =
                context.getPrincipal();
            // TenantAuthenticationDetails details = (TenantAuthenticationDetails) authentication.getDetails();
            TenantAuthenticationDetails details = new TenantAuthenticationDetails("mifos", "mifos", "password");

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
