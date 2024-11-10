package com.koomi.controller;


import com.koomi.domain.USER_ROLE;
import com.koomi.entity.VerificationCode;
import com.koomi.request.LoginRequest;
import com.koomi.request.SignupRequest;
import com.koomi.response.APIResponse;
import com.koomi.response.AuthResponse;
import com.koomi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("v1/signup")
    public ResponseEntity<AuthResponse> createUserHandle(@RequestBody SignupRequest req) {

        String jwt = authService.signup(req);

        AuthResponse response = new AuthResponse();
        response.setJwt(jwt);
        response.setMessage("User created successfully");
        response.setRole(USER_ROLE.CUSTOMER);

        return ResponseEntity.ok(response);
    }

    @PostMapping("v1/send/login-signup-otp")
    public ResponseEntity<APIResponse> sendOtpHandle(@RequestBody VerificationCode req) throws Exception {

        authService.sentLoginOtp(req.getEmail());

        APIResponse response = new APIResponse();
        response.setMessage("OTP sent successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("v1/signing")
    public ResponseEntity<AuthResponse> loginHandle(@RequestBody LoginRequest req) {

        AuthResponse response =  authService.signing(req);

        return ResponseEntity.ok(response);
    }

}

