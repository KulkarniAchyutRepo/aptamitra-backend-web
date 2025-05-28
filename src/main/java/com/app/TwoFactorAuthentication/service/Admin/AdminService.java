package com.app.TwoFactorAuthentication.service.Admin;

import com.app.TwoFactorAuthentication.entity.Role;
import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.UserAlreadyExistsException;
import com.app.TwoFactorAuthentication.exceptions.securityExceptions.RoleNotFoundException;
import com.app.TwoFactorAuthentication.repository.Admin.UserRoleRepository;
import com.app.TwoFactorAuthentication.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    public String addRole(String name) {
        if (roleRepository.existsByName(name)) {
            throw new UserAlreadyExistsException("Role already exists.");
        }

        Role role = new Role(name);
        roleRepository.save(role);

        return "Role added successfully.";
    }

    public List<Role> getRoles(){
        return roleRepository.findAll();
    }

    @Transactional
    public void deleteRole(long id){
        roleRepository.deleteById(id);
    }

    @Transactional
    public String addRoleToUser(Long userId, Long roleId) {
        // Find user by ID
        Optional<User> userOptional = userRoleRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }
        User user = userOptional.get();

        // Find role by Id
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new RoleNotFoundException("Role not found.");
        }
        Role role = roleOptional.get();

        // Add role to user and save
        Set<Role> userRoles = user.getRoles();
        if(userRoles==null){
            userRoles = new HashSet<>();
        }
        userRoles.add(role);
        user.setRoles(userRoles);
        userRoleRepository.save(user);

        return "Role added successfully to user.";
    }

    @Transactional
    public String removeRoleToUser(Long userId, Long roleId) {
        // Find user by ID
        Optional<User> userOptional = userRoleRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }
        User user = userOptional.get();

        // Find role by Id
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new RoleNotFoundException("Role not found.");
        }
        Role role = roleOptional.get();

        // Add role to user and save
        Set<Role> userRoles = user.getRoles();
        if(userRoles==null){
            userRoles = new HashSet<>();
        }
        userRoles.remove(role);
        user.setRoles(userRoles);
        userRoleRepository.save(user);

        return "Role removed successfully.";
    }

    @Transactional
    public String removeMultipleRoleToUser(Long userId, Long[] roleIds) {
        // Find user by ID
        Optional<User> userOptional = userRoleRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }
        User user = userOptional.get();

        // Iterate through each roleId and remove corresponding role from user's roles
        Set<Role> userRoles = user.getRoles();
        if (userRoles == null) {
            userRoles = new HashSet<>();
        }

        for (Long roleId : roleIds) {
            Optional<Role> roleOptional = roleRepository.findById(roleId);
            if (roleOptional.isPresent()) {
                Role role = roleOptional.get();
                userRoles.remove(role);
            } else {
                throw new RoleNotFoundException("Role with ID " + roleId + " not found.");
            }
        }

        // Update user's roles and save
        user.setRoles(userRoles);
        userRoleRepository.save(user);

        return "Roles removed successfully.";
    }

    @Transactional
    public String addMultipleRoleToUser(Long userId, Long[] roleIds) {
        // Find user by ID
        Optional<User> userOptional = userRoleRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }
        User user = userOptional.get();

        // Iterate through each roleId and remove corresponding role from user's roles
        Set<Role> userRoles = user.getRoles();
        if (userRoles == null) {
            userRoles = new HashSet<>();
        }

        for (Long roleId : roleIds) {
            Optional<Role> roleOptional = roleRepository.findById(roleId);
            if (roleOptional.isPresent()) {
                Role role = roleOptional.get();
                userRoles.add(role);
            } else {
                throw new RoleNotFoundException("Role with ID " + roleId + " not found.");
            }
        }

        // Update user's roles and save
        user.setRoles(userRoles);
        userRoleRepository.save(user);

        return "Roles added successfully.";
    }

    public String toggleEnableForUser(Long id){
        // Find user by ID
        Optional<User> userOptional = userRoleRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }
        User user = userOptional.get();
        user.setEnabled(!user.isEnabled());
        userRoleRepository.save(user);
        return "User enabled set to "+user.isEnabled();
    }

    public Map<String, Integer> getRoleCount() {
        List<List<Object>> results = roleRepository.getRoleCount();
        Map<String, Integer> roleCountMap = new HashMap<>();

        for (List<Object> result : results) {
            String roleName = (String) result.get(0);
            Integer userCount = ((Number) result.get(1)).intValue();
            roleCountMap.put(roleName, userCount);
        }

        return roleCountMap;
    }
}



