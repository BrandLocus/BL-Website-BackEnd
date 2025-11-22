package com.hvc.brandlocus.config;


import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.sendgrid.SendGrid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Properties;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BeanConfig {
    private final BaseUserRepository userRepository;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;
    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.protocol}")
    private String protocol;

    @Value("${openai.api.key}")
    private String apiKey;





    @Bean
        CorsConfigurationSource corsConfigurationSource(){
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        log.info("authenticating user that want to login");
        return username -> userRepository.findByEmail(username.trim()).orElseThrow(()-> new UsernameNotFoundException("User not found."));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws  Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendGridApiKey);
    }

//    @Bean
//    public JavaMailSender getJavaMailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(host);
//        mailSender.setPort(port);
//        mailSender.setUsername(username);
//        mailSender.setPassword(password);
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", protocol);
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.ssl.enable", "true");
//        props.put("mail.debug", "true");
//
//        return mailSender;
//    }

    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(465);

        mailSender.setUsername("chukwudileid@gmail.com");
        mailSender.setPassword("hztz odhc iohf yxsf");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }


    @Bean
    public OpenAIClient openAIClient(@Value("${openai.api.key}") String apiKey) {
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }




}
