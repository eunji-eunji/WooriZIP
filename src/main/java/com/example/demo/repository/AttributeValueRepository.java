package com.example.demo.repository;

import com.example.demo.entity.AttributeValue;
import com.example.demo.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    // AttributeValue와 그 Attribute를 즉시 로딩하는 쿼리 (템플릿 오류 방지)
    @org.springframework.data.jpa.repository.Query("SELECT av FROM AttributeValue av JOIN FETCH av.attribute")
    List<AttributeValue> findAllWithAttribute();
    boolean existsByAttributeAndValue(Attribute attribute, String value);
}