package com.example.quiz.repository;

import com.example.quiz.model.Player;
import com.example.quiz.model.PlayerAnswer;
import com.example.quiz.model.Question;
import com.example.quiz.model.QuizSession;
import jakarta.persistence.EntityManager;

public class PlayerAnswerRepository {
    private final EntityManager em;
    public PlayerAnswerRepository(EntityManager em) { this.em = em; }

    // ---- CRUD helpers ----
    public PlayerAnswer save(PlayerAnswer pa) {
        if (pa == null) return null;
        if (pa.getId() == null) { em.persist(pa); return pa; }
        return em.merge(pa);
    }

    public boolean existsByPlayerQuestionSession(Player player, Question question, QuizSession session) {
        if (player == null || question == null) return false;
        String jpql = "SELECT COUNT(pa) FROM PlayerAnswer pa WHERE pa.player.id = :pid AND pa.question.id = :qid";
        if (session != null) jpql += " AND pa.session.id = :sid";
        var q = em.createQuery(jpql, Long.class)
                .setParameter("pid", player.getId())
                .setParameter("qid", question.getId());
        if (session != null) q.setParameter("sid", session.getId());
        Long cnt = q.getSingleResult();
        return cnt != null && cnt > 0;
    }

    /** Delete all answers for a player (optionally scoped to a session). Returns # of rows deleted. */
    public int deleteByPlayerAndSession(Player player, QuizSession session) {
        if (player == null) return 0;
        if (session != null) {
            return em.createQuery("DELETE FROM PlayerAnswer pa WHERE pa.player.id = :pid AND pa.session.id = :sid")
                .setParameter("pid", player.getId())
                .setParameter("sid", session.getId())
                .executeUpdate();
        } else {
            return em.createQuery("DELETE FROM PlayerAnswer pa WHERE pa.player.id = :pid")
                .setParameter("pid", player.getId())
                .executeUpdate();
        }
    }

    /** Bulk delete all answers in a given session. */
    public int deleteBySessionId(Long sessionId) {
        if (sessionId == null) return 0;
        return em.createQuery("DELETE FROM PlayerAnswer pa WHERE pa.session.id = :sid")
                .setParameter("sid", sessionId)
                .executeUpdate();
    }
}
