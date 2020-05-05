package com.cube.examples.security;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

import com.cube.examples.errors.CustomAccessDeniedHandler;
import com.cube.examples.errors.CustomAuthenticationEntryPoint;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public ResourceServerConfiguration(CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId("orders");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .antMatcher("/orders/**")
                .authorizeRequests()
                .antMatchers("/orders/signin**").permitAll()
                .antMatchers("/orders/signin/**").permitAll()
                .antMatchers("/orders/getOrders**").hasAnyAuthority("ADMIN", "USER")
                .antMatchers("/orders/postOrder**").hasAuthority("ADMIN")
                .antMatchers("/orders/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint).accessDeniedHandler(new CustomAccessDeniedHandler());
    }

}