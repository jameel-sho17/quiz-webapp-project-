package com.example.quiz.repository;

import com.example.quiz.model.User;
import com.example.quiz.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    public List<User> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<User> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = em.find(User.class, id);
            return Optional.ofNullable(user);
        } finally {
            em.close();
        }
    }

    public Optional<User> findByNameAndPassword(String name, String password) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.name = :name AND u.password = :password", User.class);
            query.setParameter("name", name);
            query.setParameter("password", password);

            List<User> users = query.getResultList();
            if (users.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(users.get(0));
            }
        } finally {
            em.close();
        }
    }

    public void save(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (user.getId() == null) {
                em.persist(user);
            } else {
                em.merge(user);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, id);
            if (user != null) {
                em.remove(user);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<User> findByName(String name) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u WHERE u.name = :n", User.class);
            q.setParameter("n", name);
            List<User> list = q.getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally { em.close(); }
    }

    public boolean existsByNameExcludingId(String name, Long excludeId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.name = :n AND (:x IS NULL OR u.id <> :x)",
                Long.class
            );
            q.setParameter("n", name);
            q.setParameter("x", excludeId);
            return q.getSingleResult() > 0;
        } finally { em.close(); }
    }

    public List<User> findAllAdmins() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.admin = true ORDER BY u.name", User.class)
                    .getResultList();
        } finally { em.close(); }
    }

    public boolean updateUsernameAndPassword(Long id, String newName, String newPassword) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User u = em.find(User.class, id);
            if (u == null) { em.getTransaction().rollback(); return false; }
            if (newName != null && !newName.isBlank()) u.setName(newName.trim());
            if (newPassword != null && !newPassword.isBlank()) u.setPassword(newPassword);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

}
