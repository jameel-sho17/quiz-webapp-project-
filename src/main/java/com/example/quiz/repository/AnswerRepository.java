package com.example.quiz.repository;

import com.example.quiz.model.Answer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class AnswerRepository {

    private final EntityManager em;

    public AnswerRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Answer answer) {
        em.persist(answer);
    }

    public List<Answer> findByQuestionId(Long questionId) {
        TypedQuery<Answer> query = em.createQuery(
            "SELECT a FROM Answer a WHERE a.question.id = :questionId", Answer.class);
        query.setParameter("questionId", questionId);
        return query.getResultList();
    }

    public void delete(Answer answer) {
        em.remove(answer);
    }

    public void deleteByQuestionId(Long questionId) {
        em.createQuery("DELETE FROM Answer a WHERE a.question.id = :qid")
        .setParameter("qid", questionId)
        .executeUpdate();
    }
}
