package com.smartexpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String itrSection;
    private BigDecimal maxLimit;
    private String applicableFor;
    private Boolean isActive;
}