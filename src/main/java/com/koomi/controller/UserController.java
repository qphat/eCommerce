package com.koomi.controller;

import com.koomi.entity.User;
import com.koomi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("users/v1/profile")
    public ResponseEntity<User> createUserHandle(
            @RequestHeader("Authorization") String jwt) {

        jwt = jwt.replace("Bearer ", "");

        User user = userService.findUserByJwtToken(jwt);

        return ResponseEntity.ok(user);
    }
}
