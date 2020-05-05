package com.cube.examples.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cube.examples.errors.CustomAccessDeniedHandler;
import com.cube.examples.errors.CustomAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {


    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public ServerSecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint, @Qualifier("userService")
            UserDetailsService userDetailsService) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
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