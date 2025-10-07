package com.example.quiz.service;

import com.example.quiz.model.Answer;
import com.example.quiz.model.Question;
import com.example.quiz.model.Quiz;
import com.example.quiz.model.User;
import com.example.quiz.repository.AnswerRepository;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class QuestionService {

    public void addQuestion(Question question) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            QuestionRepository repository = new QuestionRepository(em);
            repository.save(question);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Question> getQuestionsByQuizId(Long quizId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuestionRepository repository = new QuestionRepository(em);
            return repository.findByQuizId(quizId);
        } finally {
            em.close();
        }
    }

    public Question getQuestionById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuestionRepository repository = new QuestionRepository(em);
            return repository.findById(id);
        } finally {
            em.close();
        }
    }

    public void deleteQuestion(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            QuestionRepository repository = new QuestionRepository(em);
            Question question = repository.findById(id);
            if (question != null) {
                repository.delete(question);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public long countForQuiz(Long quizId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return new QuestionRepository(em).countByQuizId(quizId);
        } finally { em.close(); }
    }

    public boolean addQuestionToQuiz(User current, Long quizId,
                                     String text, int duration, int points,
                                     List<Answer> answers) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            QuizRepository quizRepo = new QuizRepository(em);
            QuestionRepository qRepo = new QuestionRepository(em);

            Quiz quiz = quizRepo.findById(quizId);
            if (quiz == null) return false;

            boolean allowed = current != null && (current.isSuperadmin()
                    || (quiz.getCreatedBy()!=null && quiz.getCreatedBy().getId().equals(current.getId())));
            if (!allowed) return false;

            boolean hasCorrect = answers != null && answers.stream().anyMatch(Answer::isCorrect);
            if (!hasCorrect) return false;

            tx.begin();
            Question q = new Question();
            q.setQuiz(quiz);
            q.setQuestionText(text);
            q.setDurationSeconds(duration);
            q.setPoints(points);

            Integer maxOrder = qRepo.findMaxOrderForQuiz(quizId);
            q.setQuestionOrder((maxOrder == null ? 0 : maxOrder) + 1);

            if (answers != null) {
                for (Answer a : answers) {
                    a.setQuestion(q);
                }
                q.setAnswers(answers);
            }

            qRepo.save(q);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally { em.close(); }
    }

    public boolean updateQuestion(User current, Long questionId,
                                  String text, int duration, int points,
                                  List<Answer> newAnswers) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            QuestionRepository qRepo = new QuestionRepository(em);
            AnswerRepository aRepo = new AnswerRepository(em);

            Question q = qRepo.findByIdWithQuizAndAnswers(questionId);
            if (q == null) return false;

            Quiz quiz = q.getQuiz();
            boolean allowed = current != null && (current.isSuperadmin()
                    || (quiz.getCreatedBy()!=null && quiz.getCreatedBy().getId().equals(current.getId())));
            if (!allowed) return false;

            boolean hasCorrect = newAnswers != null && newAnswers.stream().anyMatch(Answer::isCorrect);
            if (!hasCorrect) return false;

            tx.begin();
            q.setQuestionText(text);
            q.setDurationSeconds(duration);
            q.setPoints(points);

            aRepo.deleteByQuestionId(q.getId());
            for (Answer a : newAnswers) {
                a.setQuestion(q);
                em.persist(a);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally { em.close(); }
    }

    public boolean deleteQuestionIfAllowed(User current, Long questionId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            QuestionRepository qRepo = new QuestionRepository(em);
            Question q = qRepo.findByIdWithQuizAndAnswers(questionId);
            if (q == null) return false;

            Quiz quiz = q.getQuiz();
            boolean allowed = current != null && (current.isSuperadmin()
                    || (quiz.getCreatedBy()!=null && quiz.getCreatedBy().getId().equals(current.getId())));
            if (!allowed) return false;

            long total = qRepo.countByQuizId(quiz.getId());
            if (total <= 5) return false; 

            tx.begin();
            q = em.merge(q);
            qRepo.delete(q);

            em.createQuery(
                "UPDATE Question q SET q.questionOrder = q.questionOrder - 1 " +
                "WHERE q.quiz.id = :qid AND q.questionOrder > :oldOrder"
            ).setParameter("qid", quiz.getId())
             .setParameter("oldOrder", q.getQuestionOrder())
             .executeUpdate();

            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally { em.close(); }
    }
    
}
