package com.app.TwoFactorAuthentication.service;
import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.UserAlreadyExistsException;
import com.app.TwoFactorAuthentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Service
public class RegistrationService {
    @Autowired
    private UserRepository userRepository;

    public String registerUser(String email, String name, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setPassword(hashedPassword);
        userRepository.save(newUser);

        return "User registered successfully.";
    }
}
