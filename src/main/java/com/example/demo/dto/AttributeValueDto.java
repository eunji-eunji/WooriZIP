package com.example.demo.dto;

public class AttributeValueDto {
    private Long id;
    private String value;
    private String attributeName;

    public AttributeValueDto() {}

    public AttributeValueDto(Long id, String value, String attributeName) {
        this.id = id;
        this.value = value;
        this.attributeName = attributeName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}