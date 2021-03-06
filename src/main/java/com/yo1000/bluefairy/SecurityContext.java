package com.yo1000.bluefairy;

import com.yo1000.bluefairy.model.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by yoichi.kikuchi on 15/03/21.
 */
@Configuration
@EnableWebMvcSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityContext extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserService userService;

    @Autowired
    private ShaPasswordEncoder shaPasswordEncoder;

    @Autowired
    private SaltSource saltSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // login
                .regexMatchers("^/auth/login.*").permitAll()
                .regexMatchers("^/auth/register.*").permitAll()
                // profile
                .regexMatchers("^/profile.*").hasAnyAuthority("ADMIN", "DEVEL", "USER")
                // ADMIN and any GET
                .regexMatchers(HttpMethod.GET, "^.*").hasAnyAuthority("ADMIN", "DEVEL", "USER")
                .regexMatchers(HttpMethod.POST, "^.*").hasAnyAuthority("ADMIN")
                .regexMatchers(HttpMethod.PUT, "^.*").hasAnyAuthority("ADMIN")
                .regexMatchers(HttpMethod.DELETE, "^.*").hasAnyAuthority("ADMIN")
                // DEVEL
                .regexMatchers(HttpMethod.POST, "^/(?!(?:user)).*").hasAnyAuthority("DEVEL")
                .regexMatchers(HttpMethod.POST, "^/api/v1/(?!(?:user|image)).*").hasAnyAuthority("DEVEL")
                .regexMatchers(HttpMethod.PUT, "^/api/v1/(?!(?:user|image)).*").hasAnyAuthority("DEVEL")
                .regexMatchers(HttpMethod.DELETE, "^/api/v1/(?!(?:user|image)).*").hasAnyAuthority("DEVEL")
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/")
                .failureUrl("/auth/login");

        http.logout()
                .logoutUrl("/auth/logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/auth/login");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(this.getUserService());
        provider.setPasswordEncoder(this.getShaPasswordEncoder());
        provider.setSaltSource(this.getSaltSource());

        auth.authenticationProvider(provider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .regexMatchers("^/resources/.*");
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserDetails authorizedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        return userDetails;
    }

    protected UserService getUserService() {
        return userService;
    }

    protected ShaPasswordEncoder getShaPasswordEncoder() {
        return shaPasswordEncoder;
    }

    protected SaltSource getSaltSource() {
        return saltSource;
    }
}
