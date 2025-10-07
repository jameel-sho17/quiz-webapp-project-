package com.example.quiz.repository;

import com.example.quiz.model.Quiz;
import com.example.quiz.model.QuizSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class QuizSessionRepository {
    private final EntityManager em;
    public QuizSessionRepository(EntityManager em) { this.em = em; }

    public QuizSession save(QuizSession s) {
        if (s.getId() == null) {
            em.persist(s);
            return s;
        } else {
            return em.merge(s);
        }
    }

    public boolean existsByPin(String pin) {
        Long count = em.createQuery("select count(s) from QuizSession s where s.pin = :pin", Long.class)
            .setParameter("pin", pin).getSingleResult();
        return count != null && count > 0;
    }

    public QuizSession findByPin(String pin) {
        try {
            return em.createQuery("select s from QuizSession s join fetch s.quiz where s.pin = :pin", QuizSession.class)
                .setParameter("pin", pin).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public QuizSession findActiveByQuiz(Quiz quiz) {
        try {
            TypedQuery<QuizSession> q = em.createQuery(
                "select s from QuizSession s where s.quiz = :quiz and s.status in (com.example.quiz.model.QuizSession$Status.WAITING, com.example.quiz.model.QuizSession$Status.RUNNING) order by s.id desc",
                QuizSession.class);
            q.setParameter("quiz", quiz);
            q.setMaxResults(1);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
