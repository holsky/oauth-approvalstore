package com.schmeisky;


import net.minidev.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.springframework.security.crypto.codec.Base64.encode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {OAuthConfig.class, SecurityConfig.class})
public class ApprovalStoreTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;


    private MockMvc mockMvc;

    private final String testClientAuthHeader = "Basic " + new String(encode(("client:").getBytes()));

    private static final String TOKEN_URL = "/oauth/token";
    private static final String AUTHORIZE_URL = "/oauth/authorize";
    private static final String ACCESS_CONFIRMATION_URL = "/access_confirmation";
    private static final String REDIRECT_URL = "http://www.client.com";

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
    }

    @Test
    public void testGrantsOnlyScopesThatWereApprovedNotDenied() throws Exception {
        MockHttpSession session = login();
        session = requestAuthorization(session);
        final String code = authorizeReadScopeDenyWriteScope(session);

        mockMvc.perform(post(TOKEN_URL).header(HttpHeaders.AUTHORIZATION, testClientAuthHeader) 
                    .accept(MediaType.APPLICATION_JSON)                                         
                    .param(OAuth2Utils.GRANT_TYPE, "authorization_code")
                    .param(OAuth2Utils.REDIRECT_URI, REDIRECT_URL)                              
                    .param("code", code))
                .andExpect(jsonPath("$.scope").value(is("read")));   
    }

    private MockHttpSession requestAuthorization(final MockHttpSession session) throws Exception {
        return (MockHttpSession)
                mockMvc.perform(
                        get(AUTHORIZE_URL)
                                .session(session)
                                .param(OAuth2Utils.CLIENT_ID, "client")
                                .param(OAuth2Utils.REDIRECT_URI, REDIRECT_URL)
                                .param(OAuth2Utils.RESPONSE_TYPE, "code")
                                .param(OAuth2Utils.SCOPE, "read write"))                     
                        .andReturn().getRequest().getSession();
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")                                                     
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)           
                                .param("username", "user")                                        
                                .param("password", "password"))                            
                        .andReturn().getRequest().getSession();
    }

    private String authorizeReadScopeDenyWriteScope(final MockHttpSession session) throws Exception {
        final MockHttpServletResponse resp = mockMvc.perform(post(AUTHORIZE_URL)                                         
                                .session(session)                                           
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)         
                                .param(OAuth2Utils.USER_OAUTH_APPROVAL, "true")             
                                .param("scope.read", "true")
                                .param("scope.write", "false"))
                           .andReturn().getResponse();
        return extractCodeParameter(resp);
    }

    private static String extractCodeParameter(final MockHttpServletResponse resp) {
        return UriComponentsBuilder.fromHttpUrl(resp.getRedirectedUrl()).build().getQueryParams().getFirst("code");
    }
}
