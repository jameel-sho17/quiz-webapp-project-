package com.example.quiz.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "quiz_sessions", indexes = {
    @Index(name = "uk_quiz_sessions_pin", columnList = "pin", unique = true)
})
public class QuizSession {

    public enum Status { INACTIVE, WAITING, RUNNING, FINISHED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false, length = 6, unique = true)
    private String pin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.INACTIVE;

    @Column(name = "current_question_index", nullable = false)
    private int currentQuestionIndex = 0; // 0-based

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    // when the current question was opened
    @Column(name = "question_opened_at")
    private Instant questionOpenedAt;

    // current question duration in seconds
    @Column(name = "question_duration_sec")
    private Integer questionDurationSec;

    @PrePersist
    protected void onCreate() { this.createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public Instant getQuestionOpenedAt() { return questionOpenedAt; }
    public void setQuestionOpenedAt(Instant questionOpenedAt) { this.questionOpenedAt = questionOpenedAt; }
    public Integer getQuestionDurationSec() { return questionDurationSec; }
    public void setQuestionDurationSec(Integer questionDurationSec) { this.questionDurationSec = questionDurationSec; }
}
