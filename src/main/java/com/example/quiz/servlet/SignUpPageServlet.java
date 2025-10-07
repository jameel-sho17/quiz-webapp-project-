package com.example.quiz.servlet;

import com.example.quiz.model.User;
import com.example.quiz.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public class SignUpPageServlet extends HttpServlet {

    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name = req.getParameter("name");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        boolean isAdmin = true; //samo admini trebaju da se prijave, obicni korisnici ne


        if (name == null || name.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Username and password are required.");
            req.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("error", "Passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
            return;
        }


        Optional<User> existingUser = userRepository.findAll()
                .stream()
                .filter(u -> u.getName().equals(name))
                .findFirst();

        if (existingUser.isPresent()) {
            req.setAttribute("error", "Username is already taken.");
            req.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setName(name);
        user.setPassword(password); 
        user.setAdmin(isAdmin);
        userRepository.save(user);

        var session = req.getSession(true);


        User persisted = userRepository.findByNameAndPassword(name, password).orElse(user);
        session.setAttribute("user", persisted);

        resp.sendRedirect(req.getContextPath() + "/admin/quizzes");

    }
}
