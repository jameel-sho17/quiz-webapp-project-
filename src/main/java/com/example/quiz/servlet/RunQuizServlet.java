package com.example.quiz.servlet;

import com.example.quiz.model.QuizSession;
import com.example.quiz.model.User;
import com.example.quiz.service.QuizSessionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "RunQuizServlet", urlPatterns = {"/admin/session/run"})
public class RunQuizServlet extends HttpServlet {
    private final QuizSessionService sessionService = new QuizSessionService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        User u = (httpSession == null) ? null : (User) httpSession.getAttribute("user");
        if (u == null || !u.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String pin = req.getParameter("pin");
        if (pin == null || pin.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "pin is required");
            return;
        }

        QuizSession s = sessionService.startRunningByPin(pin.trim());
        // Vrati na isti admin ekran (session.jsp) sa sesijom
        req.setAttribute("session", s);
        req.getRequestDispatcher("/WEB-INF/views/admin/session.jsp").forward(req, resp);
    }
}
