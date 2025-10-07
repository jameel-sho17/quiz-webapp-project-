package com.example.quiz.servlet;

import com.example.quiz.model.Player;
import com.example.quiz.model.QuizSession;
import com.example.quiz.repository.PlayerRepository;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "SessionStatusServlet", urlPatterns = {"/api/session/status"})
public class SessionStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pin = req.getParameter("pin");
        resp.setCharacterEncoding("UTF-8");

        if (pin == null || pin.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"error\":\"pin is required\"}");
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizSessionRepository sRepo = new QuizSessionRepository(em);
            QuizSession s = sRepo.findByPin(pin.trim());
            if (s == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"error\":\"session not found\"}");
                return;
            }

            PlayerRepository pRepo = new PlayerRepository(em);
            List<Player> players = pRepo.findBySession(s);

            StringBuilder json = new StringBuilder(256);
            json.append('{');
            json.append("\"status\":\"").append(s.getStatus().name()).append("\",");
            json.append("\"quizId\":").append(s.getQuiz().getId()).append(',');
            json.append("\"sessionId\":").append(s.getId()).append(',');
            json.append("\"count\":").append(players.size()).append(',');
            json.append("\"players\":[");
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (i > 0) json.append(',');
                json.append("{")
                .append("\"id\":").append(p.getId())
                .append(",\"name\":").append(toJsonString(p.getName()))
                .append(",\"score\":").append(p.getScore() == null ? 0 : p.getScore())
                .append("}");
            }
            json.append("]");

            json.append('}');

            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(json.toString());
        } finally {
            em.close();
        }
    }

    private String toJsonString(String s) {
        if (s == null) return "null";
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
