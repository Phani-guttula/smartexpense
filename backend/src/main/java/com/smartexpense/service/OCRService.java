package com.smartexpense.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartexpense.dto.response.OCRResultDTO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OCRService {
    
    @Autowired
    private Tesseract tesseract;
    
   
    public OCRResultDTO processReceipt(MultipartFile file) throws IOException, TesseractException {
        
        // Step 1: Read image from uploaded file
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("Failed to read image file");
        }
        
        // Step 2: Preprocess image for better OCR accuracy
        BufferedImage processedImage = preprocessImage(originalImage);
        
        // Step 3: Perform OCR
        String rawText = tesseract.doOCR(processedImage);
        
        // Step 4: Parse extracted text
        OCRResultDTO result = parseReceiptText(rawText);
        
        return result;
    }
    
    private BufferedImage preprocessImage(BufferedImage original) {

        BufferedImage gray = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D g = gray.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();

        // Binary threshold
        for (int y = 0; y < gray.getHeight(); y++) {
            for (int x = 0; x < gray.getWidth(); x++) {

                int rgb = gray.getRGB(x, y) & 0xFF;

                int binary = rgb > 130 ? 255 : 0;

                int newPixel = (binary << 16) | (binary << 8) | binary;

                gray.setRGB(x, y, newPixel);
            }
        }

        return gray;
    }
    
    private OCRResultDTO parseReceiptText(String text) {
        
        OCRResultDTO result = new OCRResultDTO();
        result.setRawText(text);
        
        // Clean up text - remove extra whitespace
        String cleanText = text
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n+", "\n")
                .trim();
        
        // Extract amount
        BigDecimal amount = extractAmount(cleanText);
        result.setAmount(amount);
        result.setAmountConfidence(amount != null ? 0.85 : 0.0);
        
        // Extract date
        LocalDate date = extractDate(cleanText);
        result.setDate(date);
        result.setDateConfidence(date != null ? 0.80 : 0.0);
        
        // Extract merchant name
        String merchant = extractMerchantName(cleanText);
        result.setMerchantName(merchant);
        
        // Calculate overall confidence
        double overallConfidence = calculateOverallConfidence(result);
        result.setOverallConfidence(overallConfidence);
        
        // Suggest category based on merchant/keywords
        String category = suggestCategory(merchant, cleanText);
        result.setSuggestedCategory(category);
        
        // Add notes if confidence is low
        if (overallConfidence < 0.6) {
            result.setNotes("Low confidence - please verify extracted data");
        }
        
        return result;
    }
    

    private BigDecimal extractAmount(String text) {

        // Normalize commas and spaces
        text = text.replaceAll(",", "");

        // PRIORITY 1 -> Total Amount
        Pattern totalPattern = Pattern.compile(
            "(?i)(total amount|total|grand total)\\s*[:₹ ]*([0-9]{1,7}(?:\\.[0-9]{1,2})?)"
        );

        Matcher totalMatcher = totalPattern.matcher(text);

        if (totalMatcher.find()) {
            return new BigDecimal(totalMatcher.group(2));
        }

        // PRIORITY 2 -> Bill Amount
        Pattern billPattern = Pattern.compile(
            "(?i)(bill amount|amount paid|paid amount)\\s*[:₹ ]*([0-9]{1,7}(?:\\.[0-9]{1,2})?)"
        );

        Matcher billMatcher = billPattern.matcher(text);

        if (billMatcher.find()) {
            return new BigDecimal(billMatcher.group(2));
        }

        // PRIORITY 3 -> Largest ₹ amount
        Pattern rupeePattern = Pattern.compile(
            "₹\\s*([0-9]{1,7}(?:\\.[0-9]{1,2})?)"
        );

        Matcher rupeeMatcher = rupeePattern.matcher(text);

        BigDecimal max = null;

        while (rupeeMatcher.find()) {

            BigDecimal value = new BigDecimal(rupeeMatcher.group(1));

            if (max == null || value.compareTo(max) > 0) {
                max = value;
            }
        }

        return max;
    }
    
    private LocalDate extractDate(String text) {
        
        // Date patterns to try
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("(\\d{2}[/.-]\\d{2}[/.-]\\d{4})"));  // DD/MM/YYYY
        patterns.add(Pattern.compile("(\\d{4}[/.-]\\d{2}[/.-]\\d{2})"));  // YYYY-MM-DD
        patterns.add(Pattern.compile("(\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4})")); // DD MMM YYYY
        
        // Date formatters
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        formatters.add(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        formatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        formatters.add(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                
                // Try each formatter
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDate.parse(dateStr, formatter);
                    } catch (DateTimeParseException e) {
                        // Try next formatter
                    }
                }
            }
        }
        
        // If no date found, return today's date as fallback
        return LocalDate.now();
    }
    
   
    //Extract merchant name from receipt
   private String extractMerchantName(String text) {

    String[] lines = text.split("\\n");

    List<String> ignorePatterns = List.of(
        "payment successful",
        "bill details",
        "payment details",
        "transaction id",
        "bharat connect",
        "utr",
        "debited from",
        "total amount",
        "bill amount",
        "powered by",
        "customer name",
        "bill number",
        "bill date",
        "05:",
        "pm on",
        "am on"
    );

    for (String line : lines) {

        String cleaned = line.trim();

        if (cleaned.length() < 4) {
            continue;
        }

        String lower = cleaned.toLowerCase();

        boolean ignore = ignorePatterns.stream()
                .anyMatch(lower::contains);

        if (ignore) {
            continue;
        }

        // Skip lines containing dates/times
        if (cleaned.matches(".*\\d{1,2}:\\d{2}.*") ||
            cleaned.matches(".*\\d{2}.*May.*\\d{4}.*")) {
            continue;
        }

        // Skip transaction/numeric-heavy lines
        if (cleaned.matches(".*\\d{5,}.*")) {
            continue;
        }

        // Prefer finance/company-like names
        if (lower.contains("finance") ||
            lower.contains("bank") ||
            lower.contains("store") ||
            lower.contains("mart") ||
            lower.contains("hospital") ||
            lower.contains("pharmacy")) {

        	cleaned = cleaned
        	        .replaceAll("₹", "")
        	        .replaceAll("\\b\\d{2,}\\b", "")
        	        .replaceAll("[^a-zA-Z& ]", "")
        	        .replaceAll("\\s+", " ")
        	        .trim();

        	cleaned = cleaned.replaceAll("^[A-Z]\\s+", "");

        	return cleaned;
        }
    }

    return "Unknown Merchant";
}
     
    private double calculateOverallConfidence(OCRResultDTO result) {
        
        double confidence = 0.0;
        int fields = 0;
        
        if (result.getAmountConfidence() != null) {
            confidence += result.getAmountConfidence();
            fields++;
        }
        
        if (result.getDateConfidence() != null) {
            confidence += result.getDateConfidence();
            fields++;
        }
        
        if (result.getMerchantName() != null && !result.getMerchantName().isEmpty()) {
            confidence += 0.75;
            fields++;
        }
        
        return fields > 0 ? confidence / fields : 0.0;
    }
    
    // Suggest expense category based on merchant name and keywords 
    private String suggestCategory(String merchant, String text) {
        
        String lowerText = (merchant + " " + text).toLowerCase();
        
        // Medical/Pharmacy keywords
        if (lowerText.contains("apollo") || lowerText.contains("pharmacy") ||
            lowerText.contains("hospital") || lowerText.contains("clinic") ||
            lowerText.contains("medical") || lowerText.contains("medicine")) {
            return "80D";
        }
        
        // LOAN / EMI
        if (lowerText.contains("loan") ||
                lowerText.contains("emi") ||
                lowerText.contains("finance") ||
                lowerText.contains("home finance") ||
                lowerText.contains("installment")) {

                return "24(b)";
            }
        
        // Insurance keywords
        if (lowerText.contains("lic") || lowerText.contains("insurance") ||
            lowerText.contains("premium")) {
            return "80C";
        }
        
        // Fuel keywords
        if (lowerText.contains("petrol") || lowerText.contains("diesel") ||
            lowerText.contains("fuel") || lowerText.contains("hp") ||
            lowerText.contains("bharat petroleum") || lowerText.contains("iocl")) {
            return "FUEL";
        }
        
        // Investment keywords
        if (lowerText.contains("mutual fund") || lowerText.contains("ppf") ||
            lowerText.contains("elss") || lowerText.contains("nsc")) {
            return "80C";
        }
        
        // Default to OTHERS
        return "OTHERS";
    }
}