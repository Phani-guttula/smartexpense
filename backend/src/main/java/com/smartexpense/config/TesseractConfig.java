package com.smartexpense.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {
    
    @Value("${tesseract.datapath:}")
    private String tessDataPath;
    
    @Value("${tesseract.language:eng}")
    private String language;
    
    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();
        
        // Set data path - uses system default if not specified
        if (tessDataPath != null && !tessDataPath.isEmpty()) {
            tesseract.setDatapath(tessDataPath);
        }
        
        tesseract.setLanguage(language);
        
        tesseract.setOcrEngineMode(1);
        
        tesseract.setPageSegMode(6);
        
        return tesseract;
    }
}