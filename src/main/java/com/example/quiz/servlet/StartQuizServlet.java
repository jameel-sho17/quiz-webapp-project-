package com.example.quiz.servlet;

import com.example.quiz.model.QuizSession;
import com.example.quiz.service.QuizSessionService;
import com.example.quiz.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "StartQuizServlet", urlPatterns = {"/admin/quizzes/start"})
public class StartQuizServlet extends HttpServlet {
    private final QuizSessionService sessionService = new QuizSessionService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        User u = (httpSession == null) ? null : (User) httpSession.getAttribute("user");
        if (u == null || !u.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String quizIdStr = req.getParameter("quizId");
        if (quizIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "quizId is required");
            return;
        }
        Long quizId = Long.parseLong(quizIdStr);

        QuizSession session = sessionService.startSession(quizId);
        req.setAttribute("session", session);

        req.getRequestDispatcher("/WEB-INF/views/admin/session.jsp").forward(req, resp);
    }
}
