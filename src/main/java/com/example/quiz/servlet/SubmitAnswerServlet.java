package com.example.quiz.servlet;

import com.example.quiz.model.*;
import com.example.quiz.repository.PlayerAnswerRepository;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "SubmitAnswerServlet", urlPatterns = {"/api/answer/submit"})
@MultipartConfig
public class SubmitAnswerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        Long playerId = (httpSession == null) ? null : (Long) httpSession.getAttribute("playerId");
        String pin = (httpSession == null) ? null : (String) httpSession.getAttribute("sessionPin");

        System.out.println("[SubmitAnswer][params] incoming with queryString=" + req.getQueryString());
        try {
            Map<String,String[]> pm = req.getParameterMap();
            for (Map.Entry<String,String[]> e : pm.entrySet()) {
                System.out.println("[SubmitAnswer][params] " + e.getKey() + "=" + java.util.Arrays.toString(e.getValue()));
            }
        } catch(Exception ignore){}

        if (playerId == null || pin == null) {
            resp.setStatus(401);
            writeJson(resp, "{\"error\":\"not joined\"}");
            return;
        }

        // --- prikupiti ID-eve odgovora (podržane sve varijante imena parametara) ---
        List<Long> selectedIds = new ArrayList<>();
        String[] repeated  = req.getParameterValues("answerIds");
        String[] repeated2 = req.getParameterValues("answerId");
        String[] repeated3 = req.getParameterValues("answerIds[]");
        if (repeated  != null) for (String v : repeated)  try { selectedIds.add(Long.parseLong(v)); } catch (Exception ignore) {}
        if (repeated2 != null) for (String v : repeated2) try { selectedIds.add(Long.parseLong(v)); } catch (Exception ignore) {}
        if (repeated3 != null) for (String v : repeated3) try { selectedIds.add(Long.parseLong(v)); } catch (Exception ignore) {}

        if (selectedIds.isEmpty()) {
            String csv = req.getParameter("answerIds");
            if (csv == null) csv = req.getParameter("answerId");
            if (csv == null) csv = req.getParameter("answerIds[]");
            if (csv != null) {
                for (String s : csv.split(",")) {
                    s = s.trim();
                    if (!s.isEmpty()) try { selectedIds.add(Long.parseLong(s)); } catch (Exception ignore) {}
                }
            }
        }
        if (selectedIds.isEmpty()) {
            System.out.println("[SubmitAnswer] selectedIds is EMPTY (after reading params)");
            resp.setStatus(400);
            writeJson(resp, "{\"error\":\"no answers provided\"}");
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            QuizSession s = new QuizSessionRepository(em).findByPin(pin);
            if (s == null) { resp.setStatus(404); writeJson(resp, "{\"error\":\"session not found\"}"); return; }
            if (s.getStatus() != QuizSession.Status.RUNNING || s.getQuestionOpenedAt() == null || s.getQuestionDurationSec() == null) {
                resp.setStatus(409); writeJson(resp, "{\"error\":\"no open question\"}"); return;
            }
            long elapsed = Duration.between(s.getQuestionOpenedAt(), Instant.now()).getSeconds();
            if (elapsed >= s.getQuestionDurationSec()) {
                resp.setStatus(409); writeJson(resp, "{\"error\":\"time elapsed\"}"); return;
            }

            Player player = em.find(Player.class, playerId);
            if (player == null) { resp.setStatus(400); writeJson(resp, "{\"error\":\"invalid player\"}"); return; }

            // Trenutno pitanje po indeksu sesije
            Question currentQ = em.createQuery(
                "select q from Question q where q.quiz = :quiz order by q.questionOrder asc, q.id asc",
                Question.class
            ).setParameter("quiz", s.getQuiz())
             .setFirstResult(s.getCurrentQuestionIndex())
             .setMaxResults(1)
             .getResultList().stream().findFirst().orElse(null);
            if (currentQ == null) { resp.setStatus(409); writeJson(resp, "{\"error\":\"no current question\"}"); return; }

            PlayerAnswerRepository paRepo = new PlayerAnswerRepository(em);
            if (paRepo.existsByPlayerQuestionSession(player, currentQ, s)) {
                resp.setStatus(409); writeJson(resp, "{\"error\":\"already answered\"}"); return;
            }

            // Validni odgovori za pitanje
            List<Answer> allForQ = em.createQuery(
                "select a from Answer a where a.question = :q", Answer.class
            ).setParameter("q", currentQ).getResultList();

            Map<Long, Answer> byId = allForQ.stream().collect(Collectors.toMap(Answer::getId, a -> a));
            // Zadrži samo selekcije koje pripadaju ovom pitanju
            LinkedHashSet<Long> filtered = selectedIds.stream()
                    .filter(byId::containsKey)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (filtered.isEmpty()) {
                resp.setStatus(400);
                writeJson(resp, "{\"error\":\"answers do not match question\"}");
                return;
            }

            // Tačnost: skup mora biti identičan skupu svih tačnih odgovora
            Set<Long> correctIds = allForQ.stream()
                    .filter(Answer::isCorrect)
                    .map(Answer::getId)
                    .collect(Collectors.toSet());
            boolean correct = !correctIds.isEmpty() && correctIds.equals(filtered);

            // --- JEDAN red u player_answers po pitanju (rješava unique key) ---
            PlayerAnswer pa = new PlayerAnswer();
            pa.setSession(s);
            pa.setPlayer(player);
            pa.setQuestion(currentQ);

            // Ako entitet ima chosenAnswerIds (String), snimi CSV svih izabranih
            try {
                Method m = PlayerAnswer.class.getMethod("setChosenAnswerIds", String.class);
                String csv = filtered.stream().map(String::valueOf).collect(Collectors.joining(","));
                m.invoke(pa, csv);
            } catch (NoSuchMethodException nsme) {
                // fallback: veži prvi odgovor (da red nije “prazan”)
                try {
                    Long first = filtered.iterator().next();
                    Answer ans = byId.get(first);
                    if (ans != null) pa.setAnswer(ans);
                } catch (Exception ignore) {}
            } catch (Exception ignore) {}

            // postavi ukupnu tačnost pokušaja
            try { pa.setCorrect(correct); } catch (Exception ignore) {}

            // Ako postoji polje awardedPoints, zapiši ga
            int awarded = 0;
            if (correct) {
                int pts = getQuestionPoints(currentQ);
                int current = (player.getScore() == null ? 0 : player.getScore());
                awarded = Math.max(pts, 0);
                player.setScore(current + awarded);
                em.merge(player);
            }
            try {
                Method m = PlayerAnswer.class.getMethod("setAwardedPoints", int.class);
                m.invoke(pa, awarded);
            } catch (Exception ignore) {}

            paRepo.save(pa); // <= sada samo jedan INSERT

            em.getTransaction().commit();
            writeJson(resp, "{\"ok\":true,\"correct\":" + (correct ? "true" : "false") + ",\"awarded\":" + awarded + ",\"score\":" + (player.getScore()==null?0:player.getScore()) + "}");
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    private int getQuestionPoints(Question q) {
        try {
            java.lang.reflect.Method m = Question.class.getMethod("getPoints");
            Object val = m.invoke(q);
            if (val instanceof Number) return ((Number) val).intValue();
        } catch (Exception ignore) {}
        return 1;
    }

    private void writeJson(HttpServletResponse resp, String s) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(s);
    }
}
