package com.example.webdisgn.repository;

import com.example.webdisgn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByName(String username);
    Optional<User> findByIdAndDeletedFalse(String id);
    Optional<User> findByNameAndDeletedFalse(String name);
    Optional<User> findByEmailAndDeletedFalse(String email);
    List<User> findByDeletedFalse();
    Optional<User> findByEmail(String email);
}
