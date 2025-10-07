package com.example.quiz.service;

import com.example.quiz.model.Answer;
import com.example.quiz.repository.AnswerRepository;
import com.example.quiz.util.JPAUtil;

import jakarta.persistence.EntityManager;
import java.util.List;

public class AnswerService {

    public void addAnswer(Answer answer) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            AnswerRepository repository = new AnswerRepository(em);
            repository.save(answer);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Answer> getAnswersByQuestionId(Long questionId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            AnswerRepository repository = new AnswerRepository(em);
            return repository.findByQuestionId(questionId);
        } finally {
            em.close();
        }
    }

    public void deleteAnswer(Answer answer) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            AnswerRepository repository = new AnswerRepository(em);
            repository.delete(answer);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
