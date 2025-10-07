package com.example.quiz.servlet;

import com.example.quiz.model.User;
import com.example.quiz.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/users/delete")
public class DeleteUserServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User me = (User) req.getSession().getAttribute("user");
        String ctx = req.getContextPath();

        if (me == null || !me.isSuperadmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Long id = null;
        try { id = Long.valueOf(req.getParameter("id")); } catch (Exception ignored) {}
        if (id == null) {
            req.getSession().setAttribute("userError", "Bad request: missing id.");
            resp.sendRedirect(ctx + "/admin/users");
            return;
        }

        if (me.getId().equals(id)) {
            req.getSession().setAttribute("userError", "You cannot delete your own account.");
            resp.sendRedirect(ctx + "/admin/users");
            return;
        }

        try {
            boolean ok = userService.deleteAdminBySuper(me, id);
            req.getSession().setAttribute(
                    ok ? "userInfo" : "userError",
                    ok ? "User deleted." : "Cannot delete this user."
            );
            resp.sendRedirect(ctx + "/admin/users");

        } catch (IllegalStateException ex) {
            req.getSession().setAttribute("userError", ex.getMessage());
            resp.sendRedirect(ctx + "/admin/users");

        } catch (SecurityException se) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

}