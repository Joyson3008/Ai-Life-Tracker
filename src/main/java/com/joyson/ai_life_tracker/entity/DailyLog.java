package com.joyson.ai_life_tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_logs")
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String bibleReading;
    private String bookReading;
    private String csTopic;
    private String codingWork;
    private String movie;
    private String collegeActivity;
    private String phoneUsage;

    @Column(length = 2000)
    private String diary;

    private Double expenses;

    // 🔥 NEW AI FIELDS

    private int score;

    @Column(columnDefinition = "TEXT")
    private String bibleReview;

    @Column(columnDefinition = "TEXT")
    private String bookReview;

    @Column(columnDefinition = "TEXT")
    private String codingReview;

    @Column(columnDefinition = "TEXT")
    private String csTopicReview;

    @Column(columnDefinition = "TEXT")
    private String collegeReview;

    @Column(columnDefinition = "TEXT")
    private String diaryReview;

    @Column(columnDefinition = "TEXT")
    private String expensesReview;

    @Column(columnDefinition = "TEXT")
    private String movieReview;

    @Column(columnDefinition = "TEXT")
    private String phoneUsageReview;

    @Column(columnDefinition = "TEXT")
    private String finalSummary;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    // (Optional - you can remove later if not needed)
    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 🔗 Relationship with User
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // =====================
    // GETTERS
    // =====================

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getBibleReading() { return bibleReading; }
    public String getBookReading() { return bookReading; }
    public String getCsTopic() { return csTopic; }
    public String getCodingWork() { return codingWork; }
    public String getMovie() { return movie; }
    public String getCollegeActivity() { return collegeActivity; }
    public String getPhoneUsage() { return phoneUsage; }
    public String getDiary() { return diary; }
    public Double getExpenses() { return expenses; }

    public int getScore() { return score; }
    public String getBibleReview() { return bibleReview; }
    public String getBookReview() { return bookReview; }
    public String getCodingReview() { return codingReview; }
    public String getCsTopicReview() { return csTopicReview; }
    public String getCollegeReview() { return collegeReview; }
    public String getDiaryReview() { return diaryReview; }
    public String getExpensesReview() { return expensesReview; }
    public String getMovieReview() { return movieReview; }
    public String getPhoneUsageReview() { return phoneUsageReview; }
    public String getFinalSummary() { return finalSummary; }
    public String getMotivation() { return motivation; }

    public String getAiFeedback() { return aiFeedback; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }

    // =====================
    // SETTERS
    // =====================

    public void setId(Long id) { this.id = id; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setBibleReading(String bibleReading) { this.bibleReading = bibleReading; }
    public void setBookReading(String bookReading) { this.bookReading = bookReading; }
    public void setCsTopic(String csTopic) { this.csTopic = csTopic; }
    public void setCodingWork(String codingWork) { this.codingWork = codingWork; }
    public void setMovie(String movie) { this.movie = movie; }
    public void setCollegeActivity(String collegeActivity) { this.collegeActivity = collegeActivity; }
    public void setPhoneUsage(String phoneUsage) { this.phoneUsage = phoneUsage; }
    public void setDiary(String diary) { this.diary = diary; }
    public void setExpenses(Double expenses) { this.expenses = expenses; }

    public void setScore(int score) { this.score = score; }
    public void setBibleReview(String bibleReview) { this.bibleReview = bibleReview; }
    public void setBookReview(String bookReview) { this.bookReview = bookReview; }
    public void setCodingReview(String codingReview) { this.codingReview = codingReview; }
    public void setCsTopicReview(String csTopicReview) { this.csTopicReview = csTopicReview; }
    public void setCollegeReview(String collegeReview) { this.collegeReview = collegeReview; }
    public void setDiaryReview(String diaryReview) { this.diaryReview = diaryReview; }
    public void setExpensesReview(String expensesReview) { this.expensesReview = expensesReview; }
    public void setMovieReview(String movieReview) { this.movieReview = movieReview; }
    public void setPhoneUsageReview(String phoneUsageReview) { this.phoneUsageReview = phoneUsageReview; }
    public void setFinalSummary(String finalSummary) { this.finalSummary = finalSummary; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUser(User user) { this.user = user; }
}