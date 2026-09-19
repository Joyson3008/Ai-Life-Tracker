package com.joyson.ai_life_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_learning", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_learning_user_date", columnNames = {"user_id", "date"})
})
public class DailyLearning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 255)
    private String csTopic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String csExplanation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String csExample;

    @Column(nullable = false, length = 255)
    private String hindiWord;
    @Column(nullable = false, length = 500)
    private String hindiMeaning;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String hindiExample;

    @Column(nullable = false, length = 255)
    private String teluguWord;
    @Column(nullable = false, length = 500)
    private String teluguMeaning;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String teluguExample;

    @Column(nullable = false, length = 255)
    private String malayalamWord;
    @Column(nullable = false, length = 500)
    private String malayalamMeaning;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String malayalamExample;

    @OneToMany(mappedBy = "dailyLearning", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyVocabulary> vocabulary = new ArrayList<>();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public LocalDate getDate() { return date; }
    public String getCsTopic() { return csTopic; }
    public String getCsExplanation() { return csExplanation; }
    public String getCsExample() { return csExample; }
    public String getHindiWord() { return hindiWord; }
    public String getHindiMeaning() { return hindiMeaning; }
    public String getHindiExample() { return hindiExample; }
    public String getTeluguWord() { return teluguWord; }
    public String getTeluguMeaning() { return teluguMeaning; }
    public String getTeluguExample() { return teluguExample; }
    public String getMalayalamWord() { return malayalamWord; }
    public String getMalayalamMeaning() { return malayalamMeaning; }
    public String getMalayalamExample() { return malayalamExample; }
    public List<DailyVocabulary> getVocabulary() { return vocabulary; }

    public void setUser(User user) { this.user = user; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setCsTopic(String csTopic) { this.csTopic = csTopic; }
    public void setCsExplanation(String csExplanation) { this.csExplanation = csExplanation; }
    public void setCsExample(String csExample) { this.csExample = csExample; }
    public void setHindiWord(String hindiWord) { this.hindiWord = hindiWord; }
    public void setHindiMeaning(String hindiMeaning) { this.hindiMeaning = hindiMeaning; }
    public void setHindiExample(String hindiExample) { this.hindiExample = hindiExample; }
    public void setTeluguWord(String teluguWord) { this.teluguWord = teluguWord; }
    public void setTeluguMeaning(String teluguMeaning) { this.teluguMeaning = teluguMeaning; }
    public void setTeluguExample(String teluguExample) { this.teluguExample = teluguExample; }
    public void setMalayalamWord(String malayalamWord) { this.malayalamWord = malayalamWord; }
    public void setMalayalamMeaning(String malayalamMeaning) { this.malayalamMeaning = malayalamMeaning; }
    public void setMalayalamExample(String malayalamExample) { this.malayalamExample = malayalamExample; }
    public void setVocabulary(List<DailyVocabulary> vocabulary) { this.vocabulary = vocabulary; }
}
