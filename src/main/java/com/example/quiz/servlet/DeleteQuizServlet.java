package com.example.quiz.servlet;

import com.example.quiz.model.User;
import com.example.quiz.service.QuizService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/quizzes/delete")
public class DeleteQuizServlet extends HttpServlet {
    private final QuizService quizService = new QuizService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User me = (User) req.getSession().getAttribute("user");
        String idStr = req.getParameter("id");
        boolean ok = false;

        try {
            Long quizId = Long.parseLong(idStr);
            ok = quizService.deleteQuizIfAllowed(me, quizId);
        } catch (Exception ignored) {}

        if (!ok) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/quizzes");
    }
}
