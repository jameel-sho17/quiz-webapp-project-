package com.example.quiz.servlet;

import com.example.quiz.model.QuizSession;
import com.example.quiz.repository.PlayerAnswerRepository;
import com.example.quiz.repository.PlayerRepository;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin action: cancel/close a running session and go back to quiz list.
 * Fixes FK constraint by deleting PlayerAnswer rows before deleting Players.
 */
@WebServlet(name = "CancelSessionServlet", urlPatterns = {
        "/admin/cancelSession", "/admin/sessions/cancel", "/admin/backToQuizzes", "/admin/session/cancel",
})
public class CancelSessionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pin = req.getParameter("pin"); // or provided via form/hidden input
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            QuizSessionRepository qsRepo = new QuizSessionRepository(em);
            QuizSession session = (pin != null && !pin.isBlank()) ? qsRepo.findByPin(pin) : null;
            if (session == null) {
                // try by id if provided
                String sidStr = req.getParameter("sessionId");
                if (sidStr != null && !sidStr.isBlank()) {
                    try { session = em.find(QuizSession.class, Long.valueOf(sidStr)); } catch (Exception ignore) {}
                }
            }

            Long sid = (session != null ? session.getId() : null);
            PlayerAnswerRepository par = new PlayerAnswerRepository(em);
            PlayerRepository pr = new PlayerRepository(em);

            // 1) delete answers (dependent) first -> prevents FK violation
            int delAns = par.deleteBySessionId(sid);
            System.out.println("[AdminCancel] Deleted answers in session " + sid + " = " + delAns);
            em.flush();

            // 2) delete players in that session
            int delPlayers = pr.deleteBySession(sid);
            System.out.println("[AdminCancel] Deleted players in session " + sid + " = " + delPlayers);

            // (optional) mark session as CANCELLED or delete it if your model allows
            if (session != null) {
                try {
                    session.setStatus(QuizSession.Status.CANCELLED);
                    em.merge(session);
                } catch (Exception ignore) {}
            }

            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            System.out.println("[AdminCancel][ERROR] " + ex);
        } finally {
            if (em.isOpen()) em.close();
        }

        // back to quizzes list
        resp.sendRedirect(req.getContextPath() + "/admin/quizzes");
    }
}
