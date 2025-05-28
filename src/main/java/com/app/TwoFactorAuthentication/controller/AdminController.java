package com.app.TwoFactorAuthentication.controller;

import com.app.TwoFactorAuthentication.dto.Admin.AddOrRemoveMultipleRolesToUsersRequestBody;
import com.app.TwoFactorAuthentication.dto.Admin.AddRoleRequestBody;
import com.app.TwoFactorAuthentication.dto.response.StandardApiResponse;
import com.app.TwoFactorAuthentication.entity.Role;
import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.exceptions.securityExceptions.ForbiddenAction;
import com.app.TwoFactorAuthentication.service.Admin.AdminService;
import com.app.TwoFactorAuthentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private AdminService adminService;
    private UserService userService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @PostMapping("/roles/{name}")
    public ResponseEntity<StandardApiResponse> addRole(@PathVariable String name){
        String response = adminService.addRole(name);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                response,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @GetMapping("/roles")
    public ResponseEntity<StandardApiResponse> getRoles(){
        List<Role> list = adminService.getRoles();
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "List of roles fetched successfully.",
                list,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @PostMapping("/add-single-role")
    public ResponseEntity<StandardApiResponse> addRole(@RequestBody AddRoleRequestBody addRoleRequestBody){
        long userId = addRoleRequestBody.userId();
        long roleId = addRoleRequestBody.roleId();
        String response = adminService.addRoleToUser(userId, roleId);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                response,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @PostMapping("/add-multiple-role")
    public ResponseEntity<StandardApiResponse> addMultipleRole(@RequestBody AddOrRemoveMultipleRolesToUsersRequestBody addOrRemoveMultipleRolesToUsersRequestBody){
        Long userId = addOrRemoveMultipleRolesToUsersRequestBody.userId();
        Long[] roleIds = addOrRemoveMultipleRolesToUsersRequestBody.roleIds();
        String response = adminService.addMultipleRoleToUser(userId, roleIds);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                response,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @DeleteMapping("/remove-single-role")
    public ResponseEntity<StandardApiResponse> removeRole(@RequestBody AddRoleRequestBody addRoleRequestBody){
        long userId = addRoleRequestBody.userId();
        long roleId = addRoleRequestBody.roleId();
        String response = adminService.removeRoleToUser(userId, roleId);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                response,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @DeleteMapping("/remove-multiple-role")
    public ResponseEntity<StandardApiResponse> removeMultipleRole(@RequestBody AddOrRemoveMultipleRolesToUsersRequestBody addOrRemoveMultipleRolesToUsersRequestBody){
        Long userId = addOrRemoveMultipleRolesToUsersRequestBody.userId();
        Long[] roleIds = addOrRemoveMultipleRolesToUsersRequestBody.roleIds();
        String response = adminService.removeMultipleRoleToUser(userId, roleIds);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                response,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @GetMapping("/users")
    public ResponseEntity<StandardApiResponse> findAll(){
        List<User> list = userService.findAll();
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "Fetched all users.",
                list,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<StandardApiResponse> deleteByEmail(@PathVariable long id){
        userService.deleteById(id);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                " User deleted.",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<StandardApiResponse> deleteRole(@PathVariable long id){
        adminService.deleteRole(id);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                " Role deleted.",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @PutMapping("users/enable/{id}")
    public ResponseEntity<StandardApiResponse> toggleEnableForUser(@PathVariable long id){
        String response = adminService.toggleEnableForUser(id);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                response,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @PutMapping("/users")
    public ResponseEntity<StandardApiResponse> update(@RequestBody User user){
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

    @GetMapping("/role-count")
    public ResponseEntity<StandardApiResponse> roleCount(){
        Map<String, Integer> data = adminService.getRoleCount();
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "Success.",
                data,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }
}
