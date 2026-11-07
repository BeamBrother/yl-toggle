package io.yltoggle.api.auth;

import io.yltoggle.api.base.model.BaseResponse;
import io.yltoggle.api.base.util.JsonMapper;
import io.yltoggle.api.config.JWTConfig;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private LoginFailureHandler loginFailureHandler;

    private LoginSuccessHandler loginSuccessHandler;

    private JWTConfig jwtConfig;

    private UserPasswordAuthenticationProvider userPasswordAuthenticationProvider;

    private GuestAuthenticationProvider guestAuthenticationProvider;

    private AccessTokenAuthenticationProvider accessTokenAuthenticationProvider;

    UserPasswordAuthenticationProcessingFilter userPasswordAuthenticationProcessingFilter(
            AuthenticationManager authenticationManager) {
        UserPasswordAuthenticationProcessingFilter filter = new UserPasswordAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);
        return filter;
    }

    GuestAuthenticationProcessingFilter guestAuthenticationProcessingFilter(
            AuthenticationManager authenticationManager) {
        GuestAuthenticationProcessingFilter filter = new GuestAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(loginFailureHandler);
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        return filter;
    }

    AccessTokenAuthenticationProcessingFilter accessTokenAuthenticationProcessingFilter(
            AuthenticationManager authenticationManager) {
        AccessTokenAuthenticationProcessingFilter filter = new AccessTokenAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(loginFailureHandler);
        return filter;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, e) -> {
            BaseResponse res = new BaseResponse(HttpStatus.UNAUTHORIZED.name().toLowerCase(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(JsonMapper.toJSONString(res));
        };
    }

    @Bean
    AuthenticationManager authenticationManager() {
        return new ProviderManager(Arrays.asList(userPasswordAuthenticationProvider,
                guestAuthenticationProvider, accessTokenAuthenticationProvider));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        http.csrf(AbstractHttpConfigurer::disable);

        http.formLogin(form -> form.loginProcessingUrl("/api/login"));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/internal/**", "/api/login", "/api/guestLogin", "/api/v3/api-docs.yaml",
                        "/api/application/**", "/api/public/**", "/actuator/health")
                .permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated());
        http.exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, e) ->
                        response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                .authenticationEntryPoint(authenticationEntryPoint()));

        http.addFilterBefore(userPasswordAuthenticationProcessingFilter(authenticationManager),
                UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new RemoteAddrFilter(), UsernamePasswordAuthenticationFilter.class);
        if (!jwtConfig.isGuestDisabled()) {
            http.addFilterBefore(guestAuthenticationProcessingFilter(authenticationManager),
                    UserPasswordAuthenticationProcessingFilter.class);
        }
        http.addFilterBefore(accessTokenAuthenticationProcessingFilter(authenticationManager),
                UserPasswordAuthenticationProcessingFilter.class);

        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                jwt.jwtAuthenticationConverter(authenticationConverter())));
        return http.build();
    }

    protected JwtAuthenticationConverter authenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName(JwtHelper.AUTHORITIES_CLAIM_NAME);
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
