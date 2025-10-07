package com.example.quiz.service;

import com.example.quiz.model.Quiz;
import com.example.quiz.model.User;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Objects;


public class QuizService {
    
    public void createQuiz(Quiz quiz) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            QuizRepository repo = new QuizRepository(em);
            repo.save(quiz);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Quiz> getAllQuizzes() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizRepository repo = new QuizRepository(em);
            return repo.findAll();
        } finally {
            em.close();
        }
    }

    public boolean isPinTaken(String pin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizRepository repo = new QuizRepository(em);
            return repo.findByPin(pin) != null;
        } finally {
            em.close();
        }
    }
    
    public Quiz getQuizById(Long quizId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizRepository repo = new QuizRepository(em);
            return repo.findById(quizId);
        } finally {
            em.close();
        }
    }

    public List<Quiz> getQuizzesFor(User current) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizRepository repo = new QuizRepository(em);
            if (current != null && current.isSuperadmin()) {
                return repo.findAll();
            } else {
                return repo.findAllByCreatorId(current.getId());
            }
        } finally {
            em.close();
        }
    }

    public Quiz getOwnedOrAllIfSuper(User current, Long quizId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuizRepository repo = new QuizRepository(em);
            Quiz q = repo.findById(quizId);
            if (q == null) return null;
            if (current != null && (current.isSuperadmin() || q.getCreatedBy().getId().equals(current.getId()))) {
                return q;
            }
            return null; 
        } finally {
            em.close();
        }
    }

    public boolean deleteQuizIfAllowed(User current, Long quizId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            QuizRepository repo = new QuizRepository(em);
            Quiz quiz = repo.findById(quizId);
            if (quiz == null) return false;

            boolean allowed = current != null &&
                    (current.isSuperadmin() || quiz.getCreatedBy().getId().equals(current.getId()));
            if (!allowed) return false;

            tx.begin();
            quiz = em.merge(quiz);
            repo.delete(quiz);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            ex.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }


    public Quiz getQuizWithChildrenIfAllowed(User current, Long quizId) {
        var em = JPAUtil.getEntityManager();
        try {
            var repo = new QuizRepository(em);
            Quiz q = repo.findByIdWithQuestionsAnswers(quizId);
            if (q == null) return null;

            boolean allowed = current != null && (
                    current.isSuperadmin()
                || q.getCreatedBy() == null
                || (q.getCreatedBy() != null && q.getCreatedBy().getId().equals(current.getId()))
            );
            if (!allowed) return null;

            if (q.getQuestions() != null) {
                q.getQuestions().forEach(qq -> {
                    qq.getAnswers().size();
                });
            }

            return q;
        } finally {
            em.close();
        }
    }

    public Quiz getEditableQuiz(User current, Long quizId) {
        var em = JPAUtil.getEntityManager();
        try {
            var repo = new QuizRepository(em);
            Quiz q = repo.findByIdWithQuestions(quizId);
            if (q == null) return null;
            boolean allowed = current != null && (current.isSuperadmin()
                    || q.getCreatedBy() == null
                    || (q.getCreatedBy() != null && q.getCreatedBy().getId().equals(current.getId())));
            return allowed ? q : null;
        } finally { em.close(); }
    }

    public boolean updateQuizMeta(User current, Long id, String title, String imageUrl, Boolean active) {
        var em = JPAUtil.getEntityManager();
        var tx = em.getTransaction();
        try {
            var repo = new QuizRepository(em);
            Quiz q = repo.findById(id);
            if (q == null) return false;
            boolean allowed = current != null && (current.isSuperadmin()
                    || (q.getCreatedBy()!=null && q.getCreatedBy().getId().equals(current.getId())));
            if (!allowed) return false;

            tx.begin();
            q.setTitle(title);
            if (imageUrl != null && !imageUrl.isBlank()) q.setImageUrl(imageUrl);
            q.setIsActive(active);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally { em.close(); }
    }

    public boolean reorderQuestions(User current, Long quizId, List<Long> questionIdsInOrder) {
        var em = JPAUtil.getEntityManager();
        var tx = em.getTransaction();
        try {
            var qrepo = new QuizRepository(em);
            var repoQ = new QuestionRepository(em);
            Quiz quiz = qrepo.findById(quizId);
            if (quiz == null) return false;

            boolean allowed = current != null && (current.isSuperadmin()
                    || (quiz.getCreatedBy()!=null && quiz.getCreatedBy().getId().equals(current.getId())));
            if (!allowed) return false;

            tx.begin();
            int order = 1;
            for (Long qid : questionIdsInOrder) {
                repoQ.updateOrder(qid, order++);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally { em.close(); }
    }


}