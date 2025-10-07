package com.example.quiz.repository;

import com.example.quiz.model.Player;
import com.example.quiz.model.QuizSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class PlayerRepository {
    private final EntityManager em;
    public PlayerRepository(EntityManager em) { this.em = em; }

    // ---- CRUD helpers ----
    public Player save(Player p) {
        if (p == null) return null;
        if (p.getId() == null) { em.persist(p); return p; }
        return em.merge(p);
    }

    public Player findById(Long id) { return id == null ? null : em.find(Player.class, id); }

    public List<Player> findBySession(QuizSession s) {
        if (s == null) return java.util.Collections.emptyList();
        TypedQuery<Player> q = em.createQuery("SELECT p FROM Player p WHERE p.session = :s ORDER BY p.id ASC", Player.class);
        q.setParameter("s", s);
        return q.getResultList();
    }

    public long countBySession(QuizSession s) {
        if (s == null) return 0L;
        Long n = em.createQuery("SELECT COUNT(p) FROM Player p WHERE p.session = :s", Long.class)
                .setParameter("s", s)
                .getSingleResult();
        return (n == null ? 0L : n);
    }

    /** Deletes a player by id (and optional session constraint). Returns number of rows deleted. */
    public int deleteByIdAndSession(Long playerId, Long sessionId) {
        if (playerId == null) return 0;
        if (sessionId == null) {
            return em.createQuery("DELETE FROM Player p WHERE p.id = :pid")
                .setParameter("pid", playerId)
                .executeUpdate();
        }
        return em.createQuery("DELETE FROM Player p WHERE p.id = :pid AND p.session.id = :sid")
            .setParameter("pid", playerId)
            .setParameter("sid", sessionId)
            .executeUpdate();
    }

    /** Bulk delete all players by session id. */
    public int deleteBySession(Long sessionId) {
        if (sessionId == null) return 0;
        return em.createQuery("DELETE FROM Player p WHERE p.session.id = :sid")
            .setParameter("sid", sessionId)
            .executeUpdate();
    }
}
