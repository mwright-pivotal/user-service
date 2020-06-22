package io.pivotal.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

@SpringBootApplication
//@EnableDiscoveryClient
//@EnableCircuitBreaker
@EnableWebMvc
public class UserApplication {

	
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    static {
        HostnameVerifier allHostsValid = (name, sslSession) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
