package com.example.quiz.service;

import com.example.quiz.model.User;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.util.JPAUtil;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    public Optional<User> getUserByNameAndPassword(String name, String password) {
        return userRepository.findByNameAndPassword(name, password);
    }
    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(id);
    }

    public List<User> getAllAdminsForSuper(User current) {
        if (current == null || !current.isSuperadmin()) throw new SecurityException("Forbidden");
        return userRepository.findAllAdmins();
    }

    public Optional<User> getUserByIdForSuper(User current, Long id) {
        if (current == null || !current.isSuperadmin()) throw new SecurityException("Forbidden");
        return userRepository.findById(id);
    }

    public boolean updateAdminProfile(User current, Long id, String newName, String newPassword) {
        if (current == null || !current.isSuperadmin()) throw new SecurityException("Forbidden");

        if (newName != null && !newName.isBlank()) {
            if (userRepository.existsByNameExcludingId(newName.trim(), id)) {
                return false; 
            }
        }
        return userRepository.updateUsernameAndPassword(id, newName, newPassword);
    }

    public boolean deleteAdminBySuper(User superUser, Long id) {
        if (superUser == null || !superUser.isSuperadmin()) return false;

        var target = userRepository.findById(id);
        if (target.isEmpty()) return false;
        if (target.get().isSuperadmin()) return false;  

        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizRepository qr = new QuizRepository(em);
            long owned = qr.countByCreatorId(id);
            if (owned > 0) {
                throw new IllegalStateException("User owns " + owned + " quizzes.");
            }

            userRepository.delete(id);
            return true;
        } finally {
            em.close();
        }
    }


}