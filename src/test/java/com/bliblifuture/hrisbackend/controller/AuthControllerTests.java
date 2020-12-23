package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.LoginCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LoginRequest;
import com.bliblifuture.hrisbackend.model.response.LoginResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = AuthController.class)
public class AuthControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private AuthController authController;

    String username = "username@mail.com";

    Principal principal;

    User user;

    ServerWebExchange swe = MockServerWebExchange.from(MockServerHttpRequest.get("").build());

    @Before
    public void setup(){
        user = User.builder()
                .username(username)
                .employeeId("EMP-123")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN))
                .build();

        principal = new Principal() {
            @Override
            public String getName() {
                return user.getUsername();
            }
        };
    }

    @Test
    public void loginTest() throws ParseException {
        LoginRequest request = LoginRequest.builder()
                .username(username)
                .password("password")
                .build();

        Date joinDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-1-1");
        UserResponse userResponse = UserResponse.builder()
                .name("name")
                .username(username)
                .roles(user.getRoles())
                .department("DEP-123")
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .joinDate(joinDate)
                .leave(LeaveResponse.builder().remaining(10).build())
                .build();

        String token = "token";
        LoginResponse loginResponse = LoginResponse.builder()
                .userResponse(userResponse)
                .accessToken(token)
                .build();

        Mockito.when(commandExecutor.execute(LoginCommand.class, request))
                .thenReturn(Mono.just(loginResponse));

        Response<UserResponse> expected = new Response<>();
        expected.setData(userResponse);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        authController.login(request, swe)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());

                    List<HttpCookie> httpCookies = swe.getRequest().getCookies().get("userToken");

                    Assert.assertNotNull(httpCookies);
                    for (HttpCookie cookie:httpCookies) {
                        if (cookie.getName().equals("userToken")){
                            Assert.assertEquals(token, cookie.getValue());
                        }
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(LoginCommand.class, request);
    }

    @Test
    public void logout() {
        String data = "LOGGED_OUT";

        Response<String> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        authController.logout(swe)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());

                    List<HttpCookie> httpCookies = swe.getRequest().getCookies().get("userToken");
                    Assert.assertNull(httpCookies);
                });
    }

}
