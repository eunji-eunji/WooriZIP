package com.example.demo.dto;

import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.Users;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

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


}