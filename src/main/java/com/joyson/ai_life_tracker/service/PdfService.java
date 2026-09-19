package com.joyson.ai_life_tracker.service;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.joyson.ai_life_tracker.entity.DailyLog;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generatePdf(DailyLog log) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 🔥 CONTENT
        document.add(new Paragraph("AI LIFE TRACKER REPORT"));
        document.add(new Paragraph("-----------------------------------"));

        document.add(new Paragraph("Score: " + log.getScore()));
        document.add(new Paragraph("Date: " + log.getDate()));

        document.add(new Paragraph("\n📖 Bible Review:\n" + log.getBibleReview()));
        document.add(new Paragraph("\n📚 Book Review:\n" + log.getBookReview()));
        document.add(new Paragraph("\n💻 Coding Review:\n" + log.getCodingReview()));
        document.add(new Paragraph("\n🎓 CS Topic:\n" + log.getCsTopicReview()));
        document.add(new Paragraph("\n🏫 College:\n" + log.getCollegeReview()));
        document.add(new Paragraph("\n📝 Diary:\n" + log.getDiaryReview()));

        document.add(new Paragraph("\n💰 Expenses:\n" + log.getExpensesReview()));
        document.add(new Paragraph("\n🎬 Movie:\n" + log.getMovieReview()));
        document.add(new Paragraph("\n📱 Phone Usage:\n" + log.getPhoneUsageReview()));

        document.add(new Paragraph("\n📊 Final Summary:\n" + log.getFinalSummary()));
        document.add(new Paragraph("\n🔥 Motivation:\n" + log.getMotivation()));

        document.close();

        return out.toByteArray();
    }
}