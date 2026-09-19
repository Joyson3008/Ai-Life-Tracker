package com.joyson.ai_life_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "daily_learning_vocabulary")
public class DailyVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_learning_id", nullable = false)
    @JsonIgnore
    private DailyLearning dailyLearning;

    @Column(nullable = false, length = 255)
    private String language;

    @Column(nullable = false, length = 255)
    private String word;

    @Column(length = 500)
    private String meaningEnglish;

    @Column(nullable = false, length = 500)
    private String meaning;

    @Column(length = 500)
    private String meaningTamil;

    @Column(columnDefinition = "TEXT")
    private String exampleEnglish;

    @Column(columnDefinition = "TEXT")
    private String exampleTamil;

    @Column(columnDefinition = "TEXT")
    private String exampleRoman;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String example;

    public Long getId() { return id; }
    public DailyLearning getDailyLearning() { return dailyLearning; }
    public String getWord() { return word; }
    public String getLanguage() { return language; }
    public String getMeaningEnglish() { return meaningEnglish; }
    public String getMeaning() { return meaning; }
    public String getMeaningTamil() { return meaningTamil; }
    public String getExampleEnglish() { return exampleEnglish; }
    public String getExampleTamil() { return exampleTamil; }
    public String getExampleRoman() { return exampleRoman; }
    public String getExample() { return example; }

    public void setDailyLearning(DailyLearning dailyLearning) { this.dailyLearning = dailyLearning; }
    public void setWord(String word) { this.word = word; }
    public void setLanguage(String language) { this.language = language; }
    public void setMeaningEnglish(String meaningEnglish) { this.meaningEnglish = meaningEnglish; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public void setMeaningTamil(String meaningTamil) { this.meaningTamil = meaningTamil; }
    public void setExampleEnglish(String exampleEnglish) { this.exampleEnglish = exampleEnglish; }
    public void setExampleTamil(String exampleTamil) { this.exampleTamil = exampleTamil; }
    public void setExampleRoman(String exampleRoman) { this.exampleRoman = exampleRoman; }
    public void setExample(String example) { this.example = example; }
}
