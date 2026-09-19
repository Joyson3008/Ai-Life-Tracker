package com.joyson.ai_life_tracker.dto;

import java.util.ArrayList;
import java.util.List;

public class DailyLearningContent {
    private String csTopic;
    private String csExplanation;
    private String csExample;
    private String hindiWord;
    private String hindiMeaning;
    private String hindiExample;
    private String teluguWord;
    private String teluguMeaning;
    private String teluguExample;
    private String malayalamWord;
    private String malayalamMeaning;
    private String malayalamExample;
    private List<VocabularyItem> vocabulary = new ArrayList<>();
    private List<VocabularyItem> hindiWords = new ArrayList<>();
    private List<VocabularyItem> teluguWords = new ArrayList<>();
    private List<VocabularyItem> malayalamWords = new ArrayList<>();

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
    public List<VocabularyItem> getVocabulary() { return vocabulary; }
    public List<VocabularyItem> getHindiWords() { return hindiWords; }
    public List<VocabularyItem> getTeluguWords() { return teluguWords; }
    public List<VocabularyItem> getMalayalamWords() { return malayalamWords; }

    public void setCsTopic(String value) { csTopic = value; }
    public void setCsExplanation(String value) { csExplanation = value; }
    public void setCsExample(String value) { csExample = value; }
    public void setHindiWord(String value) { hindiWord = value; }
    public void setHindiMeaning(String value) { hindiMeaning = value; }
    public void setHindiExample(String value) { hindiExample = value; }
    public void setTeluguWord(String value) { teluguWord = value; }
    public void setTeluguMeaning(String value) { teluguMeaning = value; }
    public void setTeluguExample(String value) { teluguExample = value; }
    public void setMalayalamWord(String value) { malayalamWord = value; }
    public void setMalayalamMeaning(String value) { malayalamMeaning = value; }
    public void setMalayalamExample(String value) { malayalamExample = value; }
    public void setVocabulary(List<VocabularyItem> value) { vocabulary = value; }
    public void setHindiWords(List<VocabularyItem> value) { hindiWords = value; }
    public void setTeluguWords(List<VocabularyItem> value) { teluguWords = value; }
    public void setMalayalamWords(List<VocabularyItem> value) { malayalamWords = value; }

    public static class VocabularyItem {
        private String word;
        private String meaningEnglish;
        private String meaningTamil;
        private String exampleEnglish;
        private String exampleTamil;
        private String exampleRoman;
        public String getWord() { return word; }
        public String getMeaningEnglish() { return meaningEnglish; }
        public String getMeaningTamil() { return meaningTamil; }
        public String getExampleEnglish() { return exampleEnglish; }
        public String getExampleTamil() { return exampleTamil; }
        public String getExampleRoman() { return exampleRoman; }
        public void setWord(String value) { word = value; }
        public void setMeaningEnglish(String value) { meaningEnglish = value; }
        public void setMeaningTamil(String value) { meaningTamil = value; }
        public void setExampleEnglish(String value) { exampleEnglish = value; }
        public void setExampleTamil(String value) { exampleTamil = value; }
        public void setExampleRoman(String value) { exampleRoman = value; }
    }
}
