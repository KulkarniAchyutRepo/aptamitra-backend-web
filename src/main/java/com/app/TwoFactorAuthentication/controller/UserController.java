package com.app.TwoFactorAuthentication.controller;

import com.app.TwoFactorAuthentication.dto.response.StandardApiResponse;
import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.exceptions.securityExceptions.ForbiddenAction;
import com.app.TwoFactorAuthentication.service.JwtService;
import com.app.TwoFactorAuthentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;
    private JwtService jwtService;

    @Autowired
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }


    @PutMapping("/")
    public ResponseEntity<StandardApiResponse> update(@RequestBody User user, @RequestHeader("Authorization") String token){
        if(!user.getEmail().equals(isValidUser(token.substring(7)))){
            throw new ForbiddenAction("This action cannot be done.");
        }
        User updatedUser = userService.update(user);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "Update successful.",
                updatedUser,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    public String isValidUser(String token){
        return jwtService.getUsernameFromToken(token);
    }
}
