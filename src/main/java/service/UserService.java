package com.taskflow.taskflowpro.service;
import com.taskflow.taskflowpro.dto.RegisterRequest;
import com.taskflow.taskflowpro.entity.User;


import java.util.List;

public interface UserService {

    void registerUser(RegisterRequest request);

    List<User> getAllUsers();

    User getUserById(Long id);

    void saveUser(User user);

    void deleteUser(Long id);

}