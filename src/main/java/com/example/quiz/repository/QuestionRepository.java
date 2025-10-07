package com.example.quiz.repository;

import com.example.quiz.model.Question;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class QuestionRepository {

    private final EntityManager em;

    public QuestionRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Question question) {
        em.persist(question);
    }

    public Question findById(Long id) {
        return em.find(Question.class, id);
    }

    public List<Question> findByQuizId(Long quizId) {
        TypedQuery<Question> query = em.createQuery(
            "SELECT q FROM Question q WHERE q.quiz.id = :quizId ORDER BY q.questionOrder", Question.class);
        query.setParameter("quizId", quizId);
        return query.getResultList();
    }

    public void delete(Question question) {
        em.remove(question);
    }

    public void updateOrder(Long questionId, int order) {
        Question q = em.find(Question.class, questionId);
        if (q != null) q.setQuestionOrder(order);
    }

    public Question findByIdWithQuizAndAnswers(Long id) {
        return em.createQuery(
            "SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.quiz z " +
            "LEFT JOIN FETCH q.answers a " +
            "WHERE q.id = :id", Question.class
        ).setParameter("id", id).getResultStream().findFirst().orElse(null);
    }

    public long countByQuizId(Long quizId) {
        return em.createQuery(
            "SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :qid", Long.class
        ).setParameter("qid", quizId).getSingleResult();
    }

    public Integer findMaxOrderForQuiz(Long quizId) {
        return em.createQuery(
            "SELECT MAX(q.questionOrder) FROM Question q WHERE q.quiz.id = :qid", Integer.class
        ).setParameter("qid", quizId).getSingleResult();
    }

}