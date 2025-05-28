package com.app.TwoFactorAuthentication.service;

import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.exceptions.securityExceptions.ResourceNotFoundException;
import com.app.TwoFactorAuthentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user  = userRepository.findByEmail(username).orElseThrow(()->new ResourceNotFoundException("User not found."));
        UserDetails customUserDetails = new CustomUserDetails(user);
        return customUserDetails;
    }

    public User update(User user){
        Optional<User> existingUser = userRepository.findById(user.getId());
        if(!existingUser.isPresent()){
            throw new UsernameNotFoundException("User does not exist.");
        }
        user.setPassword(existingUser.get().getPassword());
        return userRepository.save(user);
    }

    public List<User> findAll(){
        return  userRepository.findAll();
    }

    public boolean deleteById(long id){
        Optional<User> existingUser = userRepository.findById(id);
        if(!existingUser.isPresent()){
            throw new UsernameNotFoundException("User does not exist.");
        }
        userRepository.deleteById(id);
        return true;
    }

    public User findByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }

    public User updatePassword(String email, String password){
        User user = findByEmail(email);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }
}
