package com.joyson.ai_life_tracker.dto;

public class AIResponse {

    private int score;
    private String bibleReview;
    private String bookReview;
    private String codingReview;
    private String csTopicReview;
    private String collegeReview;
    private String diaryReview;
    private String expensesReview;
    private String movieReview;
    private String phoneUsageReview;
    private String finalSummary;
    private String motivation;

    // ✅ Getters & Setters (IMPORTANT)

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getBibleReview() { return bibleReview; }
    public void setBibleReview(String bibleReview) { this.bibleReview = bibleReview; }

    public String getBookReview() { return bookReview; }
    public void setBookReview(String bookReview) { this.bookReview = bookReview; }

    public String getCodingReview() { return codingReview; }
    public void setCodingReview(String codingReview) { this.codingReview = codingReview; }

    public String getCsTopicReview() { return csTopicReview; }
    public void setCsTopicReview(String csTopicReview) { this.csTopicReview = csTopicReview; }

    public String getCollegeReview() { return collegeReview; }
    public void setCollegeReview(String collegeReview) { this.collegeReview = collegeReview; }

    public String getDiaryReview() { return diaryReview; }
    public void setDiaryReview(String diaryReview) { this.diaryReview = diaryReview; }

    public String getExpensesReview() { return expensesReview; }
    public void setExpensesReview(String expensesReview) { this.expensesReview = expensesReview; }

    public String getMovieReview() { return movieReview; }
    public void setMovieReview(String movieReview) { this.movieReview = movieReview; }

    public String getPhoneUsageReview() { return phoneUsageReview; }
    public void setPhoneUsageReview(String phoneUsageReview) { this.phoneUsageReview = phoneUsageReview; }

    public String getFinalSummary() { return finalSummary; }
    public void setFinalSummary(String finalSummary) { this.finalSummary = finalSummary; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
}