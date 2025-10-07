package com.example.quiz.repository;

import com.example.quiz.model.Quiz;
import com.example.quiz.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class QuizRepository {
    
    private EntityManager em;

    public QuizRepository(EntityManager em) {
        this.em = em;
    }
    
    public void save(Quiz quiz) {
        em.persist(quiz);
    }

    public List<Quiz> findAll() {
        TypedQuery<Quiz> query = em.createQuery("SELECT q FROM Quiz q", Quiz.class);
        return query.getResultList();
    }

    public Quiz findById(Long id) {
        return em.find(Quiz.class, id);
    }

    public Quiz findByPin(String pin) {
        TypedQuery<Quiz> query = em.createQuery("SELECT q FROM Quiz q WHERE q.pin = :pin", Quiz.class);
        query.setParameter("pin", pin);
        List<Quiz> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Quiz> findAllByCreatorId(Long creatorId) {
        TypedQuery<Quiz> q = em.createQuery(
            "SELECT q FROM Quiz q WHERE q.createdBy.id = :uid", Quiz.class);
        q.setParameter("uid", creatorId);
        return q.getResultList();
    }

    public void delete(Quiz quiz) {
        em.remove(quiz); 
    }

    public Quiz findByIdWithQuestionsAnswers(Long id) {
        return em.createQuery(
            "SELECT DISTINCT q FROM Quiz q " +       
            "LEFT JOIN FETCH q.questions qs " +      
            "LEFT JOIN FETCH q.createdBy cb " +
            "WHERE q.id = :id", Quiz.class)
        .setParameter("id", id)
        .getResultStream()
        .findFirst()
        .orElse(null);
    }

    public Quiz findByIdWithQuestions(Long id) {
        return em.createQuery(
            "SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions qs " +
            "LEFT JOIN FETCH q.createdBy cb " +
            "WHERE q.id = :id", Quiz.class)
            .setParameter("id", id)
            .getResultStream()
            .findFirst()
            .orElse(null);
    }

    public long countByCreatorId(Long userId) {
        var em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select count(q) from Quiz q where q.createdBy.id = :uid", Long.class)
                .setParameter("uid", userId)
                .getSingleResult();
        } finally { em.close(); }
    }


}
