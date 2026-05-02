package org.example.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Optional;
import org.example.backend.domain.entity.ThemeSettings;
import org.example.backend.repository.ThemeSettingsRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static final Color NAVY = new Color(23, 32, 51);
    private static final Color BLUE = new Color(31, 111, 235);
    private static final Color TEAL = new Color(15, 118, 110);
    private static final Color GOLD = new Color(217, 119, 6);
    private static final Color SOFT_BLUE = new Color(232, 241, 255);
    private static final Color BORDER = new Color(220, 228, 238);
    private static final Color MUTED = new Color(83, 97, 115);

    @Value("${app.storage.pdf-dir}")
    private String pdfDir;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final ThemeSettingsRepository themeSettingsRepository;

    public PdfService(ThemeSettingsRepository themeSettingsRepository) {
        this.themeSettingsRepository = themeSettingsRepository;
    }

    public String generateCertificatePdf(String code, String title, String description, String holder, String status, String qrPath) {
        try {
            File dir = new File(pdfDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Cannot create PDF directory");
            }

            BrandTheme theme = resolveTheme();

            String baseFileName = "certificate-" + code;
            Path tempPath = Path.of(pdfDir, baseFileName + "-temp.pdf");
            Path signedPath = Path.of(pdfDir, baseFileName + ".pdf");

            Document document = new Document(PageSize.A4.rotate(), 42, 42, 34, 34);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tempPath.toFile()));
            document.open();

            drawCertificateBackground(writer, document.getPageSize(), theme);
            document.add(buildCertificateContent(code, title, description, holder, status, qrPath, theme));

            Paragraph footer = new Paragraph(
                    "PDF signe numeriquement par " + theme.appName + " - verification publique par QR code",
                    FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(14);
            document.add(footer);
            document.close();

            signPdf(tempPath.toString(), signedPath.toString());
            Files.deleteIfExists(tempPath);
            return signedPath.toString();
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
    }

    private PdfPTable buildCertificateContent(
            String code,
                String title,
                String description,
            String holder,
            String status,
            String qrPath,
            BrandTheme theme
    ) throws IOException, DocumentException {
        Font kickerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, theme.primary);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, theme.secondary);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 13, MUTED);
        Font descriptionFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, MUTED);
        Font holderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, TEAL);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, theme.secondary);

        PdfPTable layout = new PdfPTable(new float[] {2.25f, 1f});
        layout.setWidthPercentage(100);
        layout.setSpacingBefore(12);

        PdfPCell main = new PdfPCell();
        main.setPadding(28);
        main.setBorder(Rectangle.NO_BORDER);
        main.setBackgroundColor(Color.WHITE);
        Image logo = loadLogo(theme.logoPath);
        if (logo != null) {
            logo.scaleToFit(140, 48);
            logo.setAlignment(Element.ALIGN_LEFT);
            main.addElement(logo);
        }
        main.addElement(spaced(theme.appName.toUpperCase() + " DIGITAL CERTIFICATE", kickerFont, 10));
        main.addElement(spaced("Certificate of Achievement", titleFont, 10));
        main.addElement(spaced("This certificate is proudly awarded to", subtitleFont, 8));
        main.addElement(spaced(holder, holderFont, 10));
        main.addElement(spaced(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, theme.secondary), 10));
        if (description != null && !description.isBlank()) {
            main.addElement(spaced(description, descriptionFont, 14));
        } else {
            main.addElement(spaced("Description non disponible.", descriptionFont, 14));
        }

        PdfPTable meta = new PdfPTable(2);
        meta.setWidthPercentage(100);
        meta.setSpacingBefore(8);
        meta.addCell(metaCell("Certificate code", code, labelFont, valueFont));
        meta.addCell(metaCell("Status", status, labelFont, valueFont));
        meta.addCell(metaCell("Issuer", theme.appName, labelFont, valueFont));
        meta.addCell(metaCell("Verification", "QR code and digital signature", labelFont, valueFont));
        main.addElement(meta);

        PdfPTable signatures = new PdfPTable(2);
        signatures.setSpacingBefore(16);
        signatures.setWidthPercentage(100);
        signatures.addCell(signatureCell("Director", theme.secondary));
        signatures.addCell(signatureCell("Signature", theme.secondary));
        main.addElement(signatures);

        PdfPCell side = new PdfPCell();
        side.setPadding(24);
        side.setBorder(Rectangle.NO_BORDER);
        side.setBackgroundColor(theme.softBackground);

        Paragraph verifyTitle = new Paragraph("Verification", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, theme.secondary));
        verifyTitle.setAlignment(Element.ALIGN_CENTER);
        verifyTitle.setSpacingAfter(14);
        side.addElement(verifyTitle);

        Image qrImage = Image.getInstance(qrPath);
        qrImage.scaleToFit(145, 145);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        side.addElement(qrImage);

        Paragraph codeText = new Paragraph(code, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, theme.primary));
        codeText.setAlignment(Element.ALIGN_CENTER);
        codeText.setSpacingBefore(14);
        side.addElement(codeText);

        Paragraph hint = new Paragraph(
                "Scan this QR code to validate authenticity and certificate status.",
                FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED)
        );
        hint.setAlignment(Element.ALIGN_CENTER);
        hint.setSpacingBefore(10);
        side.addElement(hint);

        layout.addCell(main);
        layout.addCell(side);
        return layout;
    }

    private Paragraph spaced(String text, Font font, float spacingAfter) {
        Paragraph paragraph = new Paragraph(text == null ? "" : text, font);
        paragraph.setSpacingAfter(spacingAfter);
        return paragraph;
    }

    private PdfPCell metaCell(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(new Color(250, 252, 254));
        cell.addElement(spaced(label.toUpperCase(), labelFont, 4));
        cell.addElement(new Paragraph(value == null ? "" : value, valueFont));
        return cell;
    }

    private PdfPCell signatureCell(String label, Color color) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, MUTED);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        Paragraph line = new Paragraph("______________________________", FontFactory.getFont(FontFactory.HELVETICA, 10, color));
        Paragraph text = new Paragraph(label, labelFont);
        text.setSpacingBefore(4);
        cell.addElement(line);
        cell.addElement(text);
        return cell;
    }

    private void drawCertificateBackground(PdfWriter writer, Rectangle page, BrandTheme theme) {
        PdfContentByte canvas = writer.getDirectContentUnder();
        canvas.saveState();
        canvas.setColorFill(new Color(246, 248, 251));
        canvas.rectangle(0, 0, page.getWidth(), page.getHeight());
        canvas.fill();

        canvas.setColorFill(Color.WHITE);
        canvas.roundRectangle(26, 26, page.getWidth() - 52, page.getHeight() - 52, 12);
        canvas.fill();

        canvas.setColorStroke(theme.primary);
        canvas.setLineWidth(3f);
        canvas.roundRectangle(32, 32, page.getWidth() - 64, page.getHeight() - 64, 10);
        canvas.stroke();

        canvas.setColorStroke(GOLD);
        canvas.setLineWidth(1.5f);
        canvas.roundRectangle(42, 42, page.getWidth() - 84, page.getHeight() - 84, 8);
        canvas.stroke();

        canvas.setColorStroke(new Color(180, 193, 214));
        canvas.setLineWidth(1f);
        canvas.setLineDash(6f, 6f, 0f);
        canvas.roundRectangle(52, 52, page.getWidth() - 104, page.getHeight() - 104, 6);
        canvas.stroke();
        canvas.setLineDash(0f);

        drawCornerAccents(canvas, page, theme.secondary);

        drawWatermark(canvas, page, theme.appName);
        canvas.restoreState();
    }

    private void drawCornerAccents(PdfContentByte canvas, Rectangle page, Color color) {
        float size = 26f;
        float inset = 58f;
        canvas.setColorStroke(color);
        canvas.setLineWidth(2.2f);

        // Top-left
        canvas.moveTo(inset, page.getHeight() - inset);
        canvas.lineTo(inset + size, page.getHeight() - inset);
        canvas.moveTo(inset, page.getHeight() - inset);
        canvas.lineTo(inset, page.getHeight() - inset - size);

        // Top-right
        canvas.moveTo(page.getWidth() - inset, page.getHeight() - inset);
        canvas.lineTo(page.getWidth() - inset - size, page.getHeight() - inset);
        canvas.moveTo(page.getWidth() - inset, page.getHeight() - inset);
        canvas.lineTo(page.getWidth() - inset, page.getHeight() - inset - size);

        // Bottom-left
        canvas.moveTo(inset, inset);
        canvas.lineTo(inset + size, inset);
        canvas.moveTo(inset, inset);
        canvas.lineTo(inset, inset + size);

        // Bottom-right
        canvas.moveTo(page.getWidth() - inset, inset);
        canvas.lineTo(page.getWidth() - inset - size, inset);
        canvas.moveTo(page.getWidth() - inset, inset);
        canvas.lineTo(page.getWidth() - inset, inset + size);

        canvas.stroke();
    }

    private void drawWatermark(PdfContentByte canvas, Rectangle page, String text) {
        try {
            PdfGState state = new PdfGState();
            state.setFillOpacity(0.08f);
            canvas.setGState(state);
            canvas.setColorFill(new Color(120, 140, 170));
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            canvas.beginText();
            canvas.setFontAndSize(baseFont, 60);
            canvas.showTextAligned(Element.ALIGN_CENTER, text.toUpperCase(), page.getWidth() / 2, page.getHeight() / 2, 24);
            canvas.endText();
        } catch (Exception ex) {
        }
    }

    private BrandTheme resolveTheme() {
        Optional<ThemeSettings> settings = themeSettingsRepository.findById(1L);
        String appName = settings.map(ThemeSettings::getAppName).filter(v -> v != null && !v.isBlank()).orElse("CertifyHub");
        Color primary = parseColor(settings.map(ThemeSettings::getPrimaryColor).orElse(null), BLUE);
        Color secondary = parseColor(settings.map(ThemeSettings::getSecondaryColor).orElse(null), NAVY);
        Color soft = mixWithWhite(primary, 0.88f, SOFT_BLUE);
        String logoPath = resolveLogoPath(settings.map(ThemeSettings::getLogoUrl).orElse(null));
        return new BrandTheme(appName, primary, secondary, soft, logoPath);
    }

    private String resolveLogoPath(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return null;
        }
        String marker = "/api/public/uploads/";
        if (logoUrl.startsWith(marker)) {
            String fileName = logoUrl.substring(marker.length());
            return Path.of(uploadDir, fileName).toString();
        }
        return null;
    }

    private Image loadLogo(String logoPath) {
        try {
            if (logoPath == null) {
                return null;
            }
            File logoFile = new File(logoPath);
            if (!logoFile.exists()) {
                return null;
            }
            return Image.getInstance(logoFile.getAbsolutePath());
        } catch (Exception ex) {
            return null;
        }
    }

    private Color parseColor(String value, Color fallback) {
        try {
            if (value == null || value.isBlank()) {
                return fallback;
            }
            String normalized = value.trim();
            if (!normalized.startsWith("#")) {
                normalized = "#" + normalized;
            }
            return Color.decode(normalized);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private Color mixWithWhite(Color base, float ratio, Color fallback) {
        try {
            int r = clamp((int) (base.getRed() * (1 - ratio) + 255 * ratio));
            int g = clamp((int) (base.getGreen() * (1 - ratio) + 255 * ratio));
            int b = clamp((int) (base.getBlue() * (1 - ratio) + 255 * ratio));
            return new Color(r, g, b);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static final class BrandTheme {
        private final String appName;
        private final Color primary;
        private final Color secondary;
        private final Color softBackground;
        private final String logoPath;

        private BrandTheme(String appName, Color primary, Color secondary, Color softBackground, String logoPath) {
            this.appName = appName;
            this.primary = primary;
            this.secondary = secondary;
            this.softBackground = softBackground;
            this.logoPath = logoPath;
        }
    }

    private void signPdf(String inputPath, String outputPath) {
        try (FileInputStream fis = new FileInputStream(inputPath);
             FileOutputStream fos = new FileOutputStream(outputPath)) {

            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = getClass().getResourceAsStream("/keystore.p12")) {
                if (is == null) {
                    throw new RuntimeException("Keystore not found in resources");
                }
                ks.load(is, "password".toCharArray());
            }

            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, "password".toCharArray());
            Certificate[] chain = ks.getCertificateChain(alias);

            PdfReader reader = new PdfReader(fis);
            PdfStamper stamper = PdfStamper.createSignature(reader, fos, '\0');

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("Authenticity verification");
            appearance.setLocation("Tunis");
            appearance.setCrypto(pk, chain, null, PdfSignatureAppearance.SELF_SIGNED);
            appearance.setVisibleSignature(new Rectangle(36, 36, 144, 72), 1, "Signature");

            stamper.close();
        } catch (Exception e) {
            throw new RuntimeException("Error signing PDF", e);
        }
    }
}
