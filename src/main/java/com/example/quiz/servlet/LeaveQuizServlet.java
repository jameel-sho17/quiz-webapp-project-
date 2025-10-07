package com.example.quiz.servlet;

import com.example.quiz.model.Player;
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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LeaveQuizServlet", urlPatterns = {"/client/leave"})
public class LeaveQuizServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession httpSession = req.getSession(false);
        Long playerId = (httpSession == null) ? null : (Long) httpSession.getAttribute("playerId");
        String pin = (httpSession == null) ? null : (String) httpSession.getAttribute("sessionPin");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            PlayerRepository pr = new PlayerRepository(em);
            PlayerAnswerRepository par = new PlayerAnswerRepository(em);
            QuizSession session = null;
            if (pin != null) {
                session = new QuizSessionRepository(em).findByPin(pin);
            }

            Player player = pr.findById(playerId);
            if (player != null) {
                // 1) delete dependent rows first (avoids FK violation)
                int delAns = par.deleteByPlayerAndSession(player, session);
                System.out.println("[LeaveQuiz] Deleted " + delAns + " answer(s) for player " + player.getId());
                em.flush();

                // 2) delete the player row
                Long sessionId = (session != null ? session.getId() : null);
                int del = pr.deleteByIdAndSession(player.getId(), sessionId);
                System.out.println("[LeaveQuiz] Deleted player rows = " + del);
            }

            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            System.out.println("[LeaveQuiz][ERROR] " + ex);
        } finally {
            if (em.isOpen()) em.close();
        }

        // clear session and go home
        if (httpSession != null) {
            httpSession.removeAttribute("playerId");
            httpSession.removeAttribute("sessionPin");
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
