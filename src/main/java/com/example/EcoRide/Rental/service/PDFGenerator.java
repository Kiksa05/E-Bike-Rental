package com.example.EcoRide.Rental.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PDFGenerator {

    //update
    private static final Logger logger = LoggerFactory.getLogger(PDFGenerator.class);

    public ByteArrayInputStream generateTermsAndConditionsPdf() throws IOException {
        logger.info("Starting PDF generation");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(out));
             Document document = new Document(pdfDoc)) {

            // Title font
            Paragraph title = new Paragraph("EcoRide Rental: Terms and Conditions for Seamless E-Bike Rentals")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);

            // Content font
            Paragraph content = new Paragraph("\n\nDobrodošli v EcoRide Rental, kjer se enostavna najema električnih koles sreča s pogoji poslovanja! Preden se odpravite na pot z našimi e-kolesi, vas vabimo, da si preberete spodnje pogoje, ki zagotavljajo nemoten najem.\n" +
                    "\n" +
                    "Rezervacije in Plačila:\n" +
                    "\n" +
                    "Rezervacije so potrjene šele po prejemu plačila.\n" +
                    "Vse cene vključujejo določene pristojbine, vendar nekatere stroške lahko dodamo naknadno.\n" +
                    "Varnost in Uporaba E-Koles:\n" +
                    "\n" +
                    "E-kolo mora uporabljati samo registrirani najemnik.\n" +
                    "E-kolo je treba vrniti v enakem stanju, kot je bilo prevzeto.\n" +
                    "Zavarovanje in Odgovornost:\n" +
                    "\n" +
                    "Vsa e-kolesa so zavarovana, vendar obstajajo določeni pogoji odgovornosti najemnika.\n" +
                    "Obvezno je prijaviti vsako škodo ali nesrečo takoj ob njenem nastanku.\n" +
                    "Čas Najema in Zamude:\n" +
                    "\n" +
                    "Zamude pri vračilu e-kolesa se lahko zaračunajo.\n" +
                    "Dodatni čas najema je mogoč, vendar je treba to predhodno dogovoriti.\n" +
                    "Preklic in Spremembe:\n" +
                    "\n" +
                    "Rezervacijo je mogoče brezplačno preklicati v določenem časovnem okviru.\n" +
                    "Spremembe rezervacij so odvisne od razpoložljivosti.\n" +
                    "Pridržek Pravic:\n" +
                    "\n" +
                    "EcoRide Rental si pridržuje pravico do zavrnitve najema ali prekinitve najema iz določenih razlogov.\n" +
                    "Preden zaključite rezervacijo, prosimo, preberite celoten dokument s pogoji poslovanja. Pri EcoRide Rental smo tu, da zagotovimo varno in prijetno izkušnjo najema električnih koles. Hvala, ker izberete EcoRide Rental!")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.JUSTIFIED);

            document.add(title);
            document.add(content);
        } catch (Exception e) {
            logger.error("Error during PDF generation", e);
            throw new IOException("Failed to generate PDF", e);
        }
        logger.info("PDF size: {} bytes", out.size());
        return new ByteArrayInputStream(out.toByteArray());
    }
}
