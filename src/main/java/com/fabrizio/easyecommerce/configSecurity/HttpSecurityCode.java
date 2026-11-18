package com.fabrizio.easyecommerce.configSecurity;

import com.fabrizio.easyecommerce.enums.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class HttpSecurityCode {

   @Autowired
   private AuthenticationProvider authenticationProvider;

   @Autowired
   private JwtAuthenticationFilter jwtAuthenticationFilter;

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
       http.csrf(csrfConfig -> csrfConfig.disable())
               .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
               .sessionManagement(sessionManConfig -> sessionManConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authenticationProvider(authenticationProvider)
               .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
               .headers(headers -> headers
                       .httpStrictTransportSecurity(hsts -> hsts
                               .includeSubDomains(true)
                               .maxAgeInSeconds(31536000)
                       )
                       .contentSecurityPolicy(csp -> csp
                               .policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none';")
                       )
                       .referrerPolicy(referrer -> referrer
                               .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                       )
                       .permissionsPolicy(permissions -> permissions
                               .policy("geolocation=(), microphone=(), camera=()")
                       )
               )
               .authorizeHttpRequests(buildRequestMatchers());
       return http.build();

   }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "http://localhost:8082",
                "http://127.0.0.1:8082"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> buildRequestMatchers(){
        return authConfig -> {
            authConfig.requestMatchers(
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
            ).permitAll();

            authConfig.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/prometheus").permitAll();
            authConfig.requestMatchers("/actuator/**").hasRole("ADMINISTRATOR");
            authConfig.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            authConfig.requestMatchers(HttpMethod.POST,"/api/auth/login").permitAll();
            authConfig.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();
            authConfig.requestMatchers(HttpMethod.GET, "/api/auth/auth-me").authenticated();

            authConfig.requestMatchers(HttpMethod.GET, "/api/categories").permitAll();
            authConfig.requestMatchers(HttpMethod.POST, "/api/categories").hasAuthority(Permission.SAVE_ONE_CATEGORY.name());
            authConfig.requestMatchers(HttpMethod.PUT, "/api/categories/*").hasAuthority(Permission.UPDATE_ONE_CATEGORY.name());
            authConfig.requestMatchers(HttpMethod.DELETE, "/api/categories/*").hasAuthority(Permission.DELETE_ONE_CATEGORY.name());

            authConfig.requestMatchers(HttpMethod.GET, "/api/products").permitAll();
            authConfig.requestMatchers(HttpMethod.GET, "/api/products/*").permitAll();
            authConfig.requestMatchers(HttpMethod.POST, "/api/products").hasAuthority(Permission.SAVE_ONE_PRODUCT.name());
            authConfig.requestMatchers(HttpMethod.PUT, "/api/products/*").hasAuthority(Permission.UPDATE_ONE_PRODUCT.name());
            authConfig.requestMatchers(HttpMethod.DELETE, "/api/products/*").hasAuthority(Permission.DELETE_ONE_PRODUCT.name());

            authConfig.requestMatchers(HttpMethod.GET, "/api/cart").hasAuthority(Permission.READ_ONE_CART.name());
            authConfig.requestMatchers(HttpMethod.POST, "/api/cart/create/*").hasAuthority(Permission.SAVE_ONE_CART.name());
            authConfig.requestMatchers(HttpMethod.POST, "/api/cart/add/*").hasAuthority(Permission.ADD_ITEM_CART.name());
            authConfig.requestMatchers(HttpMethod.PUT, "/api/cart/item/*").hasAuthority(Permission.UPDATE_QUANTITY_ITEM_CART.name());
            authConfig.requestMatchers(HttpMethod.DELETE, "/api/cart/item/*").hasAuthority(Permission.DELETE_ITEM_CART.name());
            authConfig.requestMatchers(HttpMethod.DELETE, "/api/cart/clear").hasAuthority(Permission.CLEAR_CART.name());
            authConfig.requestMatchers(HttpMethod.DELETE, "/api/cart/clear/*").hasAuthority(Permission.CLEAR_CART.name());

            authConfig.requestMatchers(HttpMethod.POST, "/api/orders/checkout").hasAuthority(Permission.PERFORM_CHECKOUT.name());
            authConfig.requestMatchers(HttpMethod.GET, "/api/orders/my-orders").hasAuthority(Permission.READ_MY_ORDERS.name());
            authConfig.requestMatchers(HttpMethod.PUT, "/api/orders/*/status").hasAuthority(Permission.UPDATE_ORDER_STATUS.name());
            authConfig.requestMatchers(HttpMethod.GET, "/api/orders/admin/all").hasAuthority(Permission.READ_ALL_ORDERS.name());

            authConfig.anyRequest().denyAll();

        };
    }
}
