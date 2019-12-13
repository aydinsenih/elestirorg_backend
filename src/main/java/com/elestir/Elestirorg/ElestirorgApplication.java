package com.elestir.Elestirorg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
public class ElestirorgApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElestirorgApplication.class, args);
	}

	@EnableWebSecurity
	@Configuration
	class WebSecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception{
			http.csrf().disable().anonymous().authorities("ROLE_ANONYMOUS").and().authorizeRequests().antMatchers(HttpMethod.POST, "/**").permitAll();
		}
	}

}
