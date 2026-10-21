package com.taskflow.taskflowpro.impl;
import com.taskflow.taskflowpro.dto.RegisterRequest;
import com.taskflow.taskflowpro.entity.User;
import com.taskflow.taskflowpro.repository.UserRepository;
import com.taskflow.taskflowpro.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public void registerUser(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration details are required");
        }

        String fullName = clean(request.getFullName());
        if (fullName == null) {
            throw new IllegalArgumentException("Full name is required");
        }

        String email = clean(request.getEmail());
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }

        String password = request.getPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String role = clean(request.getRole());
        if (role == null) {
            role = "STUDENT";
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        String registerNumber = clean(request.getRegisterNumber());
        if ("STUDENT".equalsIgnoreCase(role)) {
            if (registerNumber == null) {
                throw new IllegalArgumentException("Register number is required for students");
            }
        }

        if (registerNumber != null && userRepository.existsByRegisterNumber(registerNumber)) {
            throw new IllegalArgumentException("An account with this register number already exists");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setRegisterNumber(registerNumber);
        user.setBranch(clean(request.getBranch()));
        user.setDepartment(clean(request.getDepartment()));
        user.setYear(clean(request.getYear()));
        user.setSection(clean(request.getSection()));

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("An account with this email already exists");
            }
            if (registerNumber != null && userRepository.existsByRegisterNumber(registerNumber)) {
                throw new IllegalArgumentException("An account with this register number already exists");
            }
            throw new IllegalArgumentException("Registration failed due to duplicate information. Please verify your details.");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void saveUser(User user) {

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}