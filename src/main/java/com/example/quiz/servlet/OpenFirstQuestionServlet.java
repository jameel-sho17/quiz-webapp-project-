package com.example.quiz.servlet;

import com.example.quiz.model.QuizSession;
import com.example.quiz.model.User;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;

@WebServlet(name = "OpenFirstQuestionServlet", urlPatterns = {"/admin/session/open-first"})
public class OpenFirstQuestionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        User u = (httpSession == null) ? null : (User) httpSession.getAttribute("user");
        if (u == null || !u.isAdmin()) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        String pin = req.getParameter("pin");
        //int dur = parseInt(req.getParameter("dur"), 20);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            QuizSessionRepository sRepo = new QuizSessionRepository(em);
            QuizSession s = sRepo.findByPin(pin);
            if (s == null) { resp.sendError(404); return; }

            // set RUNNING + open first question (index 0)
            s.setStatus(QuizSession.Status.RUNNING);
            s.setCurrentQuestionIndex(0);
            s.setQuestionOpenedAt(Instant.now());

            Integer dur = em.createQuery(
                "select q.durationSeconds " +
                "from Question q " +
                "where q.quiz = :quiz " +
                "order by q.questionOrder asc, q.id asc",
                Integer.class
            ).setParameter("quiz", s.getQuiz())
            .setMaxResults(1)
            .getSingleResult();

            int safeDur = (dur == null) ? 20 : Math.max(1, Math.min(dur, 60));

            s.setQuestionDurationSec(safeDur);
            
            sRepo.save(s);

            em.getTransaction().commit();
            req.setAttribute("session", s);
            req.getRequestDispatcher("/WEB-INF/views/admin/session.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally { em.close(); }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
