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
               .sessionManagement(sessionManConfig -> sessionManConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authenticationProvider(authenticationProvider)
               .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
               .authorizeHttpRequests(buildRequestMatchers());
   return http.build();

   }

    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> buildRequestMatchers(){
        return authConfig -> {
            authConfig.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
            authConfig.requestMatchers(HttpMethod.POST,"/api/auth/login").permitAll();
            authConfig.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();
            authConfig.requestMatchers(HttpMethod.GET, "/api/auth/auth-me").authenticated();

            authConfig.requestMatchers(HttpMethod.GET, "/api/categories").hasAuthority(Permission.READ_ALL_CATEGORIES.name());
            authConfig.requestMatchers(HttpMethod.POST, "/api/categories").hasAuthority(Permission.SAVE_ONE_CATEGORY.name());
            authConfig.requestMatchers(HttpMethod.PUT, "/api/categories/{name}").hasAuthority(Permission.UPDATE_ONE_CATEGORY.name());
            authConfig.requestMatchers(HttpMethod.DELETE, "/api/categories/{id}").hasAuthority(Permission.DELETE_ONE_CATEGORY.name());

            authConfig.anyRequest().denyAll();

        };
    }

}
