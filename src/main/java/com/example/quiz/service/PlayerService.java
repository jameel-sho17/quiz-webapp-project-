package com.example.quiz.service;

import com.example.quiz.model.Player;
import com.example.quiz.model.QuizSession;
import com.example.quiz.repository.PlayerRepository;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;

public class PlayerService {

    public static class JoinResult {
        public final Player player;
        public final QuizSession session;
        public JoinResult(Player p, QuizSession s) { this.player = p; this.session = s; }
    }

    public JoinResult joinByPin(String pin, String name) {
        String pinTrim = pin == null ? "" : pin.trim();
        String nameTrim = name == null ? "" : name.trim();
        if (pinTrim.isEmpty() || nameTrim.isEmpty()) {
            throw new IllegalArgumentException("PIN i ime su obavezni.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            QuizSessionRepository sRepo = new QuizSessionRepository(em);
            QuizSession s = sRepo.findByPin(pinTrim);
            if (s == null) {
                throw new IllegalArgumentException("Neispravan PIN.");
            }
            if (s.getStatus() != QuizSession.Status.WAITING) {
                throw new IllegalStateException("Prijava nije dozvoljena (sesija nije u statusu WAITING).");
            }

            Player p = new Player();
            p.setSession(s);
            p.setQuiz(s.getQuiz());
            p.setName(nameTrim);
            p.setFullName(nameTrim);
            p.setScore(0);

            new PlayerRepository(em).save(p);

            em.getTransaction().commit();
            return new JoinResult(p, s);
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
