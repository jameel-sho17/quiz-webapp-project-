package com.example.quiz.service;

import com.example.quiz.model.Quiz;
import com.example.quiz.model.QuizSession;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.repository.QuizSessionRepository;
import com.example.quiz.util.JPAUtil;
import com.example.quiz.util.PinGenerator;
import jakarta.persistence.EntityManager;
import java.time.Instant;

public class QuizSessionService {

    public QuizSession startSession(Long quizId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            QuizRepository quizRepo = new QuizRepository(em);
            Quiz quiz = quizRepo.findById(quizId);
            if (quiz == null) {
                throw new IllegalArgumentException("Quiz not found: " + quizId);
            }

            QuizSessionRepository sessionRepo = new QuizSessionRepository(em);

            // Always create NEW session with NEW PIN
            String pin;
            int attempts = 0;
            do {
                pin = PinGenerator.generate6Digit();
                attempts++;
                if (attempts > 50) throw new IllegalStateException("Could not generate unique PIN");
            } while (sessionRepo.existsByPin(pin));

            QuizSession s = new QuizSession();
            s.setQuiz(quiz);
            s.setPin(pin);
            s.setStatus(QuizSession.Status.WAITING);
            s.setStartedAt(Instant.now());

            sessionRepo.save(s);
            em.getTransaction().commit();
            return s;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public QuizSession startRunningByPin(String pin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            QuizSessionRepository sessionRepo = new QuizSessionRepository(em);
            QuizSession s = sessionRepo.findByPin(pin);
            if (s == null) throw new IllegalArgumentException("Session not found for PIN: " + pin);
            if (s.getStatus() == QuizSession.Status.FINISHED || s.getStatus() == QuizSession.Status.CANCELLED) {
                throw new IllegalStateException("Session already ended.");
            }
            s.setStatus(QuizSession.Status.RUNNING);
            sessionRepo.save(s);
            em.getTransaction().commit();
            return s;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void cancelByPin(String pin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            QuizSessionRepository sessionRepo = new QuizSessionRepository(em);
            QuizSession s = sessionRepo.findByPin(pin);
            if (s != null) {
                s.setStatus(QuizSession.Status.CANCELLED);
                sessionRepo.save(s);
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
