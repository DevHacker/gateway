package com.gtechsolutions.gateway.config.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/product-service/fallback")
    public ResponseEntity<String> getProductServiceFallback(){
        return new ResponseEntity<>("Product Service is not available. please try again in few minutes",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
