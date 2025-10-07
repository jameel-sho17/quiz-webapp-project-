package com.example.quiz.servlet;

import com.example.quiz.model.Player;
import com.example.quiz.model.QuizSession;
import com.example.quiz.service.PlayerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "JoinQuizServlet", urlPatterns = {"/start-quiz"})
public class JoinQuizServlet extends HttpServlet {
    private final PlayerService playerService = new PlayerService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pin = req.getParameter("pin");
        String name = req.getParameter("name");

        if (pin == null || pin.isBlank() || name == null || name.isBlank()) {
            req.setAttribute("error", "PIN i ime su obavezni.");
            req.getRequestDispatcher("/join-quiz.jsp").forward(req, resp);
            return;
        }

        try {
            PlayerService.JoinResult res = playerService.joinByPin(pin.trim(), name.trim());
            Player player = res.player;
            QuizSession session = res.session;

            // Sačuvaj podatke o igraču u HTTP sesiju (klijent).
            HttpSession httpSession = req.getSession(true);
            httpSession.setAttribute("playerId", player.getId());
            httpSession.setAttribute("playerName", player.getName());
            httpSession.setAttribute("sessionPin", session.getPin());
            httpSession.setAttribute("sessionId", session.getId());

            // Prebaci na "čekanje"
            req.setAttribute("session", session);
            req.getRequestDispatcher("/wait.jsp").forward(req, resp);

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/join-quiz.jsp").forward(req, resp);
        } catch (IllegalStateException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/join-quiz.jsp").forward(req, resp);
        }
    }
}
