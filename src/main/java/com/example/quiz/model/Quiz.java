package com.example.quiz.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "image_url")
    private String imageUrl;

    private String pin; 

    @Column(name = "is_active")
    private Boolean isActive = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    private List<Question> questions;

    //konstruktori
    public Quiz() {}
    
    public Quiz(String title, String imageUrl, String pin, User createdBy) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.pin = pin;
        this.createdBy = createdBy;
        this.isActive = false;
    }

    //geteri i seteri
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

}
