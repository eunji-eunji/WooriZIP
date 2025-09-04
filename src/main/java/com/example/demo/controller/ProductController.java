package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.AttributeValue;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.Users;
import com.example.demo.repository.AttributeRepository;
import com.example.demo.repository.AttributeValueRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewPostRepository;
import com.example.demo.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import com.example.demo.entity.ReviewPost;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ProductDetailService productDetailService;
    private final QnaPostService qnaPostService;
    private final ReviewPostService reviewPostService;
    private final WishlistService wishlistService;

    private final ProductRepository productRepository;
    private final ReviewPostRepository reviewPostRepository;
    private final CategoryRepository categoryRepository;


    @GetMapping("/admin/products")
    public String adminProductList(Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        model.addAttribute("loginUser", UserUtils.getUser(authentication));

        // 등록일 기준으로 정렬된 상품 리스트 조회
        List<Product> productList = productRepository.findAllByOrderByCreatedAtDesc();

        // DTO로 변환 (평균 평점 + 리뷰 수 포함)
        List<ProductListDto> dtoList = productList.stream()
                .map(product -> {
                    double avgRating = reviewPostRepository.findByProductId(product.getId())
                            .stream().mapToInt(ReviewPost::getRating).average().orElse(0.0);
                    int reviewCount = reviewPostRepository.countByProductId(product.getId());
                    return new ProductListDto(product, avgRating, reviewCount);
                }).toList();

        model.addAttribute("products", dtoList);
        return "product/admin-list";
    }

    @GetMapping("/products/form")
    public String showProductForm(Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        model.addAttribute("loginUser", UserUtils.getUser(authentication));

        ProductForm productForm = new ProductForm();
        ProductModelDto defaultModel = new ProductModelDto();
        productForm.getProductModelDtoList().add(defaultModel);

        model.addAttribute("productForm", productForm);
        model.addAttribute("attributes", attributeRepository.findAll());
        List<AttributeValue> attributeValues = attributeValueRepository.findAllWithAttribute();
        List<AttributeValueDto> attributeValueDtos = attributeValues.stream()
                .map(av -> new AttributeValueDto(av.getId(), av.getValue(), av.getAttribute().getName()))
                .toList();
        model.addAttribute("attributeValues", attributeValueDtos);
        return "product/products";
    }

    @PostMapping("/admin/products")
    public String createProduct(@ModelAttribute ProductForm form,
                                @RequestParam("images") MultipartFile[] images,
                                @RequestParam(value = "productModelDtoListJson", required = false) String modelsJson,
                                Authentication authentication,
                                Model model) {
        String email = UserUtils.getEmail(authentication);
        try {
            if (email == null) return "redirect:/user/login";
            Users loginUser = UserUtils.getUser(authentication);

            // 옵션 리스트 JSON 파싱 (프론트에서 넘어온 경우)
            if (modelsJson != null && !modelsJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<ProductModelDto> modelDtoList = objectMapper.readValue(
                        modelsJson, new TypeReference<List<ProductModelDto>>() {}
                );
                form.setProductModelDtoList(modelDtoList);
            }

            // 상품 등록 처리
            Long productId = productService.createProduct(form, Arrays.asList(images), loginUser);
            model.addAttribute("productForm", form);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products")
    public String showProductList(@RequestParam(name = "category", required = false) String categoryParam,
                                  @RequestParam(defaultValue = "1") int page,
                                  Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        Users loginUser = email != null ? UserUtils.getUser(authentication) : null;
        model.addAttribute("loginUser", loginUser);

        // 카테고리 파라미터 처리 (ID 또는 이름)
        Long categoryId = null;
        if (categoryParam != null && !categoryParam.isEmpty()) {
            try {
                // 숫자인 경우 ID로 처리
                categoryId = Long.parseLong(categoryParam);
            } catch (NumberFormatException e) {
                // 숫자가 아닌 경우 카테고리 이름으로 ID 찾기
                Category category = categoryRepository.findByName(categoryParam).orElse(null);
                if (category != null) {
                    categoryId = category.getId();
                }
            }
        }

        // 페이징 처리 (한 페이지당 9개)
        int pageSize = 9;
        Page<ProductListDto> productPage = productService.findProductsWithPaging(categoryId, page, pageSize);
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("hasPrevious", productPage.hasPrevious());
        
        return "product/list";
    }


    @GetMapping("/products/{id}")
    public String viewProduct(@PathVariable Long id,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "latest") String sort,
                              @RequestParam(name = "qnaPage", defaultValue = "1") int qnaPage,
                              @RequestParam(name = "qnaFilter", defaultValue = "all") String qnaFilter,
                              Model model,
                              Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        Users user = email != null ? UserUtils.getUser(authentication) : null;

        // ───────────── 상품 상세 정보 ─────────────
        ProductDetailDto dto = productService.getProductDetail(id, user);
        model.addAttribute("product", dto);
        model.addAttribute("loginUser", user);

        // ───────────── 상품 상세정보 (이미지/규격) ─────────────
        ProductDetailInfoDto productDetail = productDetailService.findByProductId(id);
        model.addAttribute("productDetail", productDetail);

        // AttributeValue를 DTO로 변환하여 전달
        List<AttributeValue> attributeValues = attributeValueRepository.findAllWithAttribute();
        List<AttributeValueDto> attributeValueDtos = attributeValues.stream()
                .map(av -> new AttributeValueDto(av.getId(), av.getValue(), av.getAttribute().getName()))
                .toList();
        model.addAttribute("attributeValues", attributeValueDtos);

        // ───────────── 리뷰 데이터 ─────────────
        int reviewPageNum = Math.max(page - 1, 0);
        if (!sort.equals("latest") && !sort.equals("rating")) {
            sort = "latest";
        }

        Page<ReviewPost> reviewPage = reviewPostService.getReviews(id, reviewPageNum, sort);
        Map<Integer, Long> ratingSummary = reviewPostService.getRatingDistribution(id);
        // Product 엔티티의 averageRating 필드 사용
        Double averageRating = dto.getAverageRating();
        boolean hasWritten = (user != null && user.getEmail() != null)
                && reviewPostService.hasWrittenReview(id, user.getEmail());
        boolean hasPurchased = (user != null && user.getEmail() != null)
                && reviewPostService.hasPurchasedProduct(id, user.getEmail());

        // ReviewPost를 ReviewPostDto로 변환
        Page<ReviewPostDto> reviewDtoPage = reviewPage.map(ReviewPostDto::fromEntity);

        model.addAttribute("reviewPage", reviewDtoPage != null ? reviewDtoPage : Page.empty());
        model.addAttribute("ratingSummary", ratingSummary != null ? ratingSummary : new HashMap<>());
        model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        model.addAttribute("hasWritten", hasWritten);
        model.addAttribute("hasPurchased", hasPurchased);
        model.addAttribute("sort", sort);
        model.addAttribute("productId", id);

        // ───────────── QnA 데이터 ─────────────
        int qnaPageNum = Math.max(qnaPage - 1, 0);
        List<QnaPostDto> qnaList = qnaPostService.getQnaPostDtoList(id, qnaPageNum, qnaFilter);
        long qnaTotal = qnaPostService.countByProduct(id, qnaFilter);

        // QnA 작성자들의 구매 여부 확인
        if (qnaList != null) {
            for (QnaPostDto qnaDto : qnaList) {
                boolean isPurchased = qnaPostService.hasPurchasedProduct(id, qnaDto.getEmail());
                qnaDto.setPurchased(isPurchased);
            }
        }

        model.addAttribute("qnaList", qnaList != null ? qnaList : new ArrayList<>());
        model.addAttribute("qnaTotal", qnaTotal);
        model.addAttribute("qnaPage", qnaPage);
        model.addAttribute("qnaFilter", qnaFilter);

        return "product/detail";
    }

    @PostMapping("/wishlist/toggle")
    public String toggleWishlist(@RequestParam Long productId,
                                 Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        Users user = UserUtils.getUser(authentication);
        wishlistService.toggleWishlist(user, productId);
        return "redirect:/products/" + productId;
    }

    @GetMapping("/admin/products/{id}/update")
    public String showUpdateForm(@PathVariable Long id, Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        model.addAttribute("loginUser", UserUtils.getUser(authentication));

        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/admin/products";
        }

        ProductForm form = ProductForm.from(product);
        model.addAttribute("productForm", form);
        model.addAttribute("product", product);
        model.addAttribute("attributes", attributeRepository.findAll());
        List<AttributeValue> attributeValues = attributeValueRepository.findAllWithAttribute();
        List<AttributeValueDto> attributeValueDtos = attributeValues.stream()
                .map(av -> new AttributeValueDto(av.getId(), av.getValue(), av.getAttribute().getName()))
                .toList();
        model.addAttribute("attributeValues", attributeValueDtos);
        return "product/update";
    }

    @PostMapping(value = "/admin/products/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateProduct(@PathVariable Long id,
                                @RequestParam("productJson") String productJson,
                                @RequestParam(value = "images", required = false) MultipartFile[] images,
                                Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        Users loginUser = UserUtils.getUser(authentication);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ProductForm form = objectMapper.readValue(productJson, ProductForm.class);
            List<Integer> deleteIndexes = form.getDeleteIndexes();
            productService.updateProduct(id, form, images, deleteIndexes, loginUser);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteProduct(@PathVariable Long id, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = UserUtils.getEmail(authentication);
            if (email == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            Users user = (Users) UserUtils.getUser(authentication);
            if (user == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return response;
            }

            productService.deleteProduct(id, user);
            response.put("success", true);
            response.put("message", "상품이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return response;
    }
}