package org.example.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {

    @Value("${app.storage.qr-dir}")
    private String qrDir;

    public String generateQrCode(String payload, String fileName) {
        try {
            File dir = new File(qrDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Cannot create QR directory");
            }
            Path qrPath = Path.of(qrDir, fileName + ".png");
            BitMatrix matrix = new MultiFormatWriter().encode(
                    payload,
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    Map.of(EncodeHintType.MARGIN, 1)
            );
            MatrixToImageWriter.writeToPath(matrix, "PNG", qrPath);
            return qrPath.toString();
        } catch (IOException | com.google.zxing.WriterException e) {
            throw new IllegalStateException("QR code generation failed", e);
        }
    }
}
