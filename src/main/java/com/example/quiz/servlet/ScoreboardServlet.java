package com.example.quiz.servlet;

import com.example.quiz.model.Player;
import com.example.quiz.model.QuizSession;
import com.example.quiz.repository.PlayerRepository;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ScoreboardServlet", urlPatterns = {"/api/session/scoreboard"})
public class ScoreboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pin = req.getParameter("pin");
        if (pin == null || pin.trim().isEmpty()) { resp.setStatus(400); writeJson(resp, "{\"error\":\"pin required\"}"); return; }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizSession s = new QuizSessionRepository(em).findByPin(pin.trim());
            if (s == null) { resp.setStatus(404); writeJson(resp, "{\"error\":\"not found\"}"); return; }

            // top 10 by score desc, then joinedAt asc
            List<Player> top = em.createQuery(
                "select p from Player p where p.session = :s order by p.score desc, p.joinedAt asc, p.id asc",
                Player.class
            ).setParameter("s", s).setMaxResults(10).getResultList();

            StringBuilder json = new StringBuilder();
            json.append('{').append("\"players\":[");
            for (int i=0;i<top.size();i++) {
                Player p = top.get(i);
                json.append('{')
                    .append("\"id\":").append(p.getId()).append(',')
                    .append("\"name\":").append(toJsonString(p.getName())).append(',')
                    .append("\"score\":").append(p.getScore() == null ? 0 : p.getScore())
                    .append('}');
                if (i < top.size()-1) json.append(',');
            }
            json.append("]}");
            writeJson(resp, json.toString());
        } finally { em.close(); }
    }

    private void writeJson(HttpServletResponse resp, String s) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(s);
    }
    private String toJsonString(String s) {
        if (s == null) return "null";
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
