package com.example.quiz.servlet;

import com.example.quiz.model.User;
import com.example.quiz.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/admin/users/edit")
public class EditUserServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        Long id = parseId(req.getParameter("id"));
        if (id == null) { resp.sendRedirect(req.getContextPath()+"/admin/users"); return; }

        try {
            Optional<User> u = userService.getUserByIdForSuper(me, id);
            if (u.isEmpty()) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            req.setAttribute("editUser", u.get());
            req.getRequestDispatcher("/WEB-INF/views/admin/editUser.jsp").forward(req, resp);
        } catch (SecurityException se) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        Long id = parseId(req.getParameter("id"));
        String name = req.getParameter("name");
        String password = req.getParameter("password"); 

        try {
            boolean ok = userService.updateAdminProfile(me, id, name, password);
            req.getSession().setAttribute(ok ? "userInfo" : "userError",
                    ok ? "Profile updated." : "Username already taken.");
            resp.sendRedirect(req.getContextPath()+"/admin/users");
        } catch (SecurityException se) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private Long parseId(String s){ try { return Long.valueOf(s); } catch(Exception e){ return null; } }
}