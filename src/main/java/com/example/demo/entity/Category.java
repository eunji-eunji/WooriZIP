package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    //(단, 대분류/소분류에 같은 이름이 올 수 있게 하려면 unique 제거)

    private int depth; // 예: 0 = 대분류, 1 = 소분류 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Builder
    public Category(String name, int depth, Category parent) {
        this.name = name;
        this.depth = depth;
        this.parent = parent;
    }

    public void addChild(Category child) { //연관관계 편의 메서드 추가 (선택적)
        children.add(child);
        child.setParent(this);
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }

}
