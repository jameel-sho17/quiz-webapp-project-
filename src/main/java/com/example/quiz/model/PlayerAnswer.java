package com.example.quiz.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "player_answers",
    uniqueConstraints = @UniqueConstraint(name = "uk_player_answer_one_per_question",
        columnNames = {"session_id","player_id","question_id","answer_id"})
)
public class PlayerAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private QuizSession session;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "answered_at", nullable = false, updatable = false)
    private Instant answeredAt;

    @PrePersist
    protected void onCreate() {
        if (answeredAt == null) answeredAt = Instant.now();
    }

    public Long getId() { return id; }
    public QuizSession getSession() { return session; }
    public void setSession(QuizSession session) { this.session = session; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public Answer getAnswer() { return answer; }
    public void setAnswer(Answer answer) { this.answer = answer; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }
}
