package io.pivotal.user.service;

import io.pivotal.user.domain.RegistrationRequest;
import io.pivotal.user.domain.Scopes;
import io.pivotal.user.domain.User;
import io.pivotal.user.domain.UserBuilder;
import io.pivotal.user.domain.uaa.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.token.OAuth2AccessTokenSupport;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The service in the user microservice.
 *
 * @author David Ferreira Pinto
 * @author Simon Rowe
 */
@Service
@Slf4j
public class UserService {

    private static final Logger logger = LoggerFactory
            .getLogger(UserService.class);

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private WebClient webClient;

    @Autowired
    private Scopes scopes;

    @Value("${targets.admin.uaa}")
    private String uaaTarget;

    @Value("${uaa.identity-zone-id}")
    private String identityZoneId;
    
    @Autowired
    CsrfTokenRepository csrfTokenRepository;

    @PostConstruct
    public void init() {
    	List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    	interceptors.add(new LoggingRequestInterceptor());
    	restTemplate.setInterceptors(interceptors);
    	
        scopes.setGroupId(scopes.getAccount(), groupIdFor(scopes.getAccount()));
        scopes.setGroupId(scopes.getBank(), groupIdFor(scopes.getBank()));
        scopes.setGroupId(scopes.getPortfolio(), groupIdFor(scopes.getPortfolio()));
        scopes.setGroupId(scopes.getTrade(), groupIdFor(scopes.getTrade()));
    }

    public User register(RegistrationRequest registrationRequest) {
        UaaCreateUserRequest uaaCreateUserRequest = UaaCreateUserRequestBuilder.withRegistrationRequest(registrationRequest).build();
        ResponseEntity<UaaUser> uaaUserResponseEntity = restTemplate.exchange(uaaTarget + "/Users", HttpMethod.POST, getEntityWithHeaders(uaaCreateUserRequest), UaaUser.class);
        User user = UserBuilder.withUaaUser(uaaUserResponseEntity.getBody()).build();
        assignUserToGroups(user, scopes.getAccount(), scopes.getTrade(), scopes.getPortfolio(), scopes.getBank());
        return user;
    }

    public User get(String id) {
        ResponseEntity<UaaUser> uaaUsersResponseEntity = restTemplate.exchange(uaaTarget + "/Users/{id}", HttpMethod.GET, getEntityWithHeaders(null), UaaUser.class, id);
        return UserBuilder.withUaaUser(uaaUsersResponseEntity.getBody()).build();
    }


    public String groupIdFor(String groupName) {
        ResponseEntity<UaaGroups> uaaGroupsResponseEntity = restTemplate.exchange(uaaTarget + "/Groups?filter=displayName eq \"{groupName}\"", HttpMethod.GET, getEntityWithHeaders(null), UaaGroups.class, groupName);
        
        
        return uaaGroupsResponseEntity.getBody().getResources().get(0).getId();
    }


    public void assignUserToGroups(User user, String... groups) {
        for (String groupName : groups) {
            ResponseEntity<Map> mapResponseEntity = restTemplate.exchange(uaaTarget + "/Groups/{groupId}/members", HttpMethod.POST, getEntityWithHeaders(new AddUserToGroupRequest(user.getId())), Map.class, scopes.getGroupId(groupName));
        }
    }

    private HttpEntity getEntityWithHeaders(Object body) {
        HttpHeaders headers = csrfHeaders();
        if (StringUtils.isNotBlank(identityZoneId)) {
            headers.set("X-Identity-Zone-Subdomain", identityZoneId);
        }
        HttpEntity entity = new HttpEntity(body,headers);
        return entity;
    }
    
    public HttpHeaders csrfHeaders() {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(null);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", csrfToken.getToken());
        headers.add("Cookie", "X-Uaa-Csrf=" + csrfToken.getToken());

        return headers;
    }
}
