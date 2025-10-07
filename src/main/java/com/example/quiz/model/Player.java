package com.example.quiz.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOVO: direktna veza na kviz (popunjava quiz_id)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Postojeće: veza na sesiju (session_id)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    // Ako zadržiš kolonu u bazi:
    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) joinedAt = Instant.now();
        if (score == null) score = 0;
        if (fullName == null && name != null) fullName = name;
    }

    // Getteri/setteri
    public Long getId() { return id; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public QuizSession getSession() { return session; }
    public void setSession(QuizSession session) { this.session = session; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
