package dev.rezu;

import net.sourceforge.tess4j.Tesseract;

import java.awt.image.BufferedImage;

public class OCRService {

    private static final String OCR_LANGUAGES = "Cyrillic+Latin+Arabic";
    private static final String OCR_DPI = "300";

    private final Tesseract tesseract;

    public OCRService() {
        tesseract = new Tesseract();

        String tessData = System.getenv("TESSDATA_PREFIX");
        if (tessData != null && !tessData.isBlank()) {
            tesseract.setDatapath(tessData);
        } else {
            System.err.println("ERROR: TESSDATA_PREFIX environment variable not set.");
        }
        tesseract.setLanguage(OCR_LANGUAGES);
        tesseract.setVariable("user_defined_dpi", OCR_DPI);
        tesseract.setVariable("tessedit_char_whitelist", "");
        tesseract.setVariable("tessedit_pageseg_mode", "6");
    }

    public String extractText(BufferedImage image) throws Exception {
        String raw = tesseract.doOCR(image);
        return raw == null ? "" : raw.trim();
    }
}