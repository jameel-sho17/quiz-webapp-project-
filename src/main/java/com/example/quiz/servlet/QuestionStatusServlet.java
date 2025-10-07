package com.example.quiz.servlet;

import com.example.quiz.model.Answer;
import com.example.quiz.model.Player;
import com.example.quiz.model.Question;
import com.example.quiz.model.QuizSession;
import com.example.quiz.repository.PlayerAnswerRepository;
import com.example.quiz.repository.PlayerRepository;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@WebServlet(name = "QuestionStatusServlet", urlPatterns = {"/api/question/status"})
public class QuestionStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pin = req.getParameter("pin");
        if (pin == null || pin.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"error\":\"pin is required\"}");
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizSessionRepository sRepo = new QuizSessionRepository(em);
            QuizSession s = sRepo.findByPin(pin.trim());
            if (s == null) { resp.setStatus(404); writeJson(resp, "{\"error\":\"not found\"}"); return; }

            long count = new PlayerRepository(em).countBySession(s);
            Long total = em.createQuery("select count(q) from Question q where q.quiz = :quiz", Long.class)
                           .setParameter("quiz", s.getQuiz()).getSingleResult();

            StringBuilder json = new StringBuilder(512);
            json.append('{');
            json.append("\"status\":\"").append(s.getStatus().name()).append("\",");
            json.append("\"index\":").append(s.getCurrentQuestionIndex()).append(',');
            json.append("\"total\":").append(total).append(',');
            json.append("\"participants\":").append(count).append(',');

            HttpSession httpSession = req.getSession(false);
            Long playerId = (httpSession == null) ? null : (Long) httpSession.getAttribute("playerId");
            boolean answeredByMe = false;

            if (s.getStatus() != QuizSession.Status.RUNNING || s.getQuestionOpenedAt() == null || s.getQuestionDurationSec() == null) {
                json.append("\"state\":\"none\"");
            } else {
                Question q = em.createQuery(
                    "select q from Question q where q.quiz = :quiz order by q.questionOrder asc, q.id asc",
                    Question.class
                ).setParameter("quiz", s.getQuiz())
                 .setFirstResult(s.getCurrentQuestionIndex())
                 .setMaxResults(1)
                 .getResultList()
                 .stream().findFirst().orElse(null);

                if (q == null) {
                    json.append("\"state\":\"none\"");
                } else {
                    long dur = s.getQuestionDurationSec();
                    long elapsed = Duration.between(s.getQuestionOpenedAt(), Instant.now()).getSeconds();
                    long left = Math.max(0, dur - elapsed);

                    if (playerId != null) {
                        Player me = em.find(Player.class, playerId);
                        if (me != null) {
                            PlayerAnswerRepository paRepo = new PlayerAnswerRepository(em);
                            answeredByMe = paRepo.existsByPlayerQuestionSession(me, q, s);
                        }
                    }

                    if (left > 0) {
                        json.append("\"state\":\"open\",");
                        json.append("\"answered\":").append(answeredByMe ? "true" : "false").append(',');
                        json.append("\"timeLeftSec\":").append(left).append(',');
                        json.append("\"question\":{");
                        json.append("\"id\":").append(q.getId()).append(',');
                        json.append("\"text\":").append(toJsonString(getQuestionText(q))).append(',');
                        json.append("\"answers\":[");
                        List<Answer> answers = em.createQuery(
                            "select a from Answer a where a.question = :q order by a.id asc", Answer.class
                        ).setParameter("q", q).getResultList();
                        for (int i=0;i<answers.size();i++) {
                            Answer a = answers.get(i);
                            json.append('{')
                                .append("\"id\":").append(a.getId()).append(',')
                                .append("\"text\":").append(toJsonString(a.getAnswerText()))
                                .append('}');
                            if (i < answers.size()-1) json.append(',');
                        }
                        json.append(']');
                        json.append('}');
                    } else {
                        json.append("\"state\":\"closed\",");
                        json.append("\"timeLeftSec\":0");
                    }
                }
            }

            json.append('}');
            writeJson(resp, json.toString());
        } finally {
            em.close();
        }
    }

    private void writeJson(HttpServletResponse resp, String s) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(s);
    }

    private String getQuestionText(Question q) {
        try {
            return (String) Question.class.getMethod("getQuestionText").invoke(q);
        } catch (Exception ignore) { }
        try {
            return (String) Question.class.getMethod("getText").invoke(q);
        } catch (Exception ignore) { }
        String s = q.toString();
        return s == null ? "" : s;
    }

    private String toJsonString(String s) {
        if (s == null) return "null";
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
