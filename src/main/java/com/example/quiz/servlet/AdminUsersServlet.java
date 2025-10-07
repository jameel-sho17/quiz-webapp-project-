package com.example.quiz.servlet;

import com.example.quiz.model.User;
import com.example.quiz.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            List<User> admins = userService.getAllAdminsForSuper(me);
            req.setAttribute("admins", admins);
            req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
        } catch (SecurityException se) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}