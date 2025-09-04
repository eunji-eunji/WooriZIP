package com.example.demo.dto;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.Users;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDetailDto {

    private Product product;
    private List<Review> reviews;
    private boolean liked;

    private List<ProductModelDto> productModels;  // 각 모델 (옵션) 정보
    
    public ProductDetailDto(Product product, boolean liked) {
        this.product = product;
        this.liked = liked;
    }
    public Category getCategory() {
        return product.getCategory();
    }

    public String getName() {
        return product.getName();
    }

    public Long getWriterId() {
        return product.getUser() != null ? product.getUser().getId() : null;
    }

    public int getPrice() {
        return product.getPrice();
    }


    public List<?> getImages() {
        return product.getImages();
    }

    public List<ProductModelDto> getProductModels() {
        return this.productModels;
    }

    public Long getId() {
        return product.getId();
    }

    public Users getUser() {
        return product.getUser();
    }

    public double getAverageRating() {
        return product.getAverageRating();
    }

    public Category getParentCategory() {
        if (product.getCategory() == null) {
            return null;
        }
        Category current = product.getCategory();
        // depth가 1이라면 중분류, 부모가 0이라면 대분류임
        if (current.getDepth() == 1) {
            return current.getParent(); // 대분류
        } else if (current.getDepth() == 0) {
            return null; // 대분류만 있음, 부모 없음
        } else {
            // depth 2 이상 (소분류 이상) -> 중분류가 부모일 것임
            return current.getParent(); // 중분류
        }
    }

    public String getCategoryDisplay() {
        Category current = product.getCategory();
        if (current == null) return "";

        if (current.getDepth() == 0) {
            return current.getName(); // 대분류만 있음
        } else if (current.getDepth() == 1) {
            // 중분류, 대분류 둘 다 표시
            return current.getParent().getName() + " > " + current.getName();
        } else if (current.getDepth() >= 2) {
            // 소분류 이상은 중분류까지만 표시
            Category parent = current.getParent();
            if (parent != null) {
                return parent.getParent().getName() + " > " + parent.getName();
            } else {
                return current.getName();
            }
        }
        return current.getName();
    }

}