package com.koomi.controller;

import com.koomi.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public APIResponse HomeControllerHandler() {
        APIResponse response = new APIResponse();
        response.setMessage("Welcome to e-commerce multi vendor platform");
        return response;
    }

}
