package org.example.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    @Value("${app.storage.pdf-dir}")
    private String pdfDir;

    public String generateCertificatePdf(String code, String title, String holder, String status, String qrPath) {
        try {
            File dir = new File(pdfDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Cannot create PDF directory");
            }

            String fileName = "certificate-" + code + ".pdf";
            Path output = Path.of(pdfDir, fileName);
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(output.toFile()));
            document.open();
            document.add(new Paragraph("Digital Certificate", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20)));
            document.add(new Paragraph(""));
            document.add(new Paragraph("Certificate Code: " + code));
            document.add(new Paragraph("Title: " + title));
            document.add(new Paragraph("Holder: " + holder));
            document.add(new Paragraph("Status: " + status));
            document.add(new Paragraph(""));
            Image qrImage = Image.getInstance(qrPath);
            qrImage.scaleToFit(120, 120);
            document.add(qrImage);
            document.close();
            return output.toString();
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
    }
}
