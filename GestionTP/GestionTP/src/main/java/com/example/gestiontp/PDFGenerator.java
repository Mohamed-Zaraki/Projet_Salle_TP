package com.example.gestiontp;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.awt.SystemColor.info;


public class PDFGenerator {

    public static void generatePannePDF(Window ownerWindow, String typePanne, String description, String degre,
                                        LocalDate dateSignalement, String salle) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport de panne");

        // Generate default file name
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
        String defaultName = "Rapport_Panne_" + typePanne + "_" + dateStr + "_" + timeStr + ".pdf";

        fileChooser.setInitialFileName(defaultName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file == null) {
            return; // User canceled
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Header Table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new int[]{1, 3});

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);

            try (InputStream is = PDFGenerator.class.getResourceAsStream("/imagesApp/Logo.png")) {
                if (is != null) {
                    Image logo = Image.getInstance(is.readAllBytes());
                    logo.scaleToFit(50, 50);
                    logoCell.addElement(logo);
                } else {
                    logoCell.setPhrase(new Phrase("Système de gestion des salles TP"));
                }
            }

            headerTable.addCell(logoCell);

            PdfPCell companyCell = new PdfPCell(new Phrase(
                    "Ilyes Bouayed\nRésponsable des salles TP"));
            companyCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(companyCell);
            document.add(headerTable);

            // Export date
            Paragraph exportDate = new Paragraph("Date d'export : " + LocalDate.now());
            exportDate.setAlignment(Element.ALIGN_RIGHT);
            document.add(exportDate);

            // Title
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Rapport d'une Panne", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10);
            title.setSpacingAfter(10);
            document.add(title);


            // Info table
            PdfPTable infoTable = new PdfPTable(1);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);
            infoTable.addCell("Date de signalement     : " + dateSignalement.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            infoTable.addCell("Détails              : " + description);
            infoTable.addCell("Degré                    : " + degre);
            infoTable.addCell("Salle                    : "  + salle);
            document.add(infoTable);




            // Footer
            Font footerFont = new Font(Font.HELVETICA, 10);
            Paragraph footer = new Paragraph("Document généré par le système de gestion des salles TP.", footerFont);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}