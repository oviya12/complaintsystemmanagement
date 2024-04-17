package com.example.complaintsystemmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.complaintsystemmanagement.User;
import com.example.complaintsystemmanagement.UserDTO;
import org.springframework.stereotype.Repository;


public interface UserRepository extends JpaRepository<User, Long> {
    UserDTO findProjectedById(long id);
    User findByUsername(String username);
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();


        // Define a method to find a user by ID and return a UserDTO




}


