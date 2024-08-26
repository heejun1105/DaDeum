package com.hkit.stt.member;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	   private final MemberSecurityService memberSecurityService;

	    public SecurityConfig(MemberSecurityService memberSecurityService) {
	        this.memberSecurityService = memberSecurityService;
	    }

	    @Bean
	    public DaoAuthenticationProvider authenticationProvider() {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	        authProvider.setUserDetailsService(memberSecurityService);
	        authProvider.setPasswordEncoder(passwordEncoder());
	        return authProvider;
	    }

	    @Bean
	    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        http
	            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
	            	.requestMatchers(new AntPathRequestMatcher("/members/checkId")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/admins/**")).hasRole("ADMIN")
	                .requestMatchers(new AntPathRequestMatcher("/members/signup")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/members/login")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/members/generate-api-key")).authenticated()
	                .requestMatchers(new AntPathRequestMatcher("/api/**")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/dadeum/**")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/*.css")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/dadeum/transcribe/file")).authenticated()
	                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
	                .requestMatchers(new AntPathRequestMatcher("/img/**")).permitAll()
	                .anyRequest().authenticated()
	            )
	            .formLogin((formLogin) -> formLogin
	                .loginPage("/members/login")
	                .successHandler((request, response, authentication) -> {
	                    response.sendRedirect("/");  // 항상 홈으로 리다이렉트
	                })
	                .failureUrl("/members/login?error")
	            )
	            .logout((logout) -> logout
	                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
	                .logoutSuccessUrl("/")
	                .invalidateHttpSession(true)
	                .deleteCookies("JSESSIONID")
	                .permitAll()
	            )
	            .authenticationProvider(authenticationProvider())
	            .csrf(csrf -> csrf.disable());
	        return http.build();
	    }

	    @Bean
	    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
	        return authenticationConfiguration.getAuthenticationManager();
	    }

	    @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	}