package com.example.demo.controller;

import com.example.demo.dto.ProductDetailInfoDto;
import com.example.demo.entity.Product;
import com.example.demo.entity.Users;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductDetailService;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductDetailService productDetailService;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // 관리자용 상품 선택 페이지 (상세정보 관리)
    @GetMapping("/admin/product-details")
    public String adminProductDetailList(Model model, Authentication authentication) {
        Users loginUser = (Users) UserUtils.getUser(authentication);
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        // 모든 상품 조회 (페이지네이션 없이)
        List<Product> allProducts = productRepository.findAll();
        
        // 각 상품에 대한 상세정보 존재 여부 확인
        Map<Long, Boolean> hasDetailMap = new HashMap<>();
        for (Product product : allProducts) {
            boolean hasDetail = productDetailService.existsByProductId(product.getId());
            hasDetailMap.put(product.getId(), hasDetail);
        }

        model.addAttribute("products", allProducts);
        model.addAttribute("hasDetailMap", hasDetailMap);
        model.addAttribute("loginUser", loginUser);
        
        return "productdetail/admin-list";
    }

    // 관리자용 상세정보 등록 페이지
    @GetMapping("/admin/product-details/{productId}/add")
    public String addDetailForm(@PathVariable Long productId, Model model, Authentication authentication) {
        try {
            // 관리자 권한 체크
            Users loginUser = (Users) UserUtils.getUser(authentication);
            if (loginUser == null) {
                return "redirect:/user/login";
            }

            // 상품 조회
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

            // 기존 상세정보 조회 (없어도 null로 처리)
            ProductDetailInfoDto existingDetail = null;
            try {
                existingDetail = productDetailService.findByProductId(productId);
            } catch (Exception e) {
                // 상세정보가 없거나 오류가 있어도 계속 진행
                System.out.println("기존 상세정보 조회 실패: " + e.getMessage());
            }
            
            model.addAttribute("product", product);
            model.addAttribute("productDetail", existingDetail);
            model.addAttribute("loginUser", loginUser);
            
            return "productdetail/add";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "페이지 로드 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    // 상세정보 저장/수정
    @PostMapping("/admin/product-details/{productId}/detail/save")
    @ResponseBody
    public String saveDetail(@PathVariable Long productId,
                           @RequestParam("detailInfo") String detailInfo,
                           @RequestParam(value = "files", required = false) MultipartFile[] files,
                           @RequestParam(value = "removedImages", required = false) List<String> removedImages,
                           Authentication authentication) {
        try {
            Users loginUser = (Users) UserUtils.getUser(authentication);
            if (loginUser == null) {
                return "unauthorized";
            }

            ProductDetailInfoDto dto = new ProductDetailInfoDto();
            dto.setProductId(productId);
            dto.setDetailInfo(detailInfo);

            // 인테리어 게시판 방식으로 파일 처리
            if (files != null && files.length > 0 && !files[0].isEmpty()) {
                handleMultipleFiles(dto, files);
            }

            productDetailService.saveProductDetail(dto, removedImages);
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 관리자용 상세정보 저장/수정 (JSON 응답)
    @PostMapping("/admin/product-details/save")
    @ResponseBody
    public Map<String, Object> saveProductDetail(@RequestParam("productId") Long productId,
                                                @RequestParam("detailInfo") String detailInfo,
                                                @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                @RequestParam(value = "removedImages", required = false) List<String> removedImages,
                                                Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Users loginUser = (Users) UserUtils.getUser(authentication);
            if (loginUser == null) {
                response.put("success", false);
                response.put("message", "권한이 없습니다.");
                return response;
            }

            ProductDetailInfoDto dto = new ProductDetailInfoDto();
            dto.setProductId(productId);
            dto.setDetailInfo(detailInfo);

            // 인테리어 게시판 방식으로 파일 처리
            if (files != null && files.length > 0 && !files[0].isEmpty()) {
                handleMultipleFiles(dto, files);
            }

            productDetailService.saveProductDetail(dto, removedImages);
            
            response.put("success", true);
            response.put("message", "저장되었습니다.");
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "저장 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    // 인테리어 게시판과 동일한 파일 처리 메서드
    private void handleMultipleFiles(ProductDetailInfoDto dto, MultipartFile[] files) {
        if (files == null || files.length == 0) return;

        List<String>[] result = handleAndReturnFiles(files);
        dto.setDetailImagePaths(String.join(",", result[0]));
        dto.setDetailImageNames(String.join(",", result[1]));
    }

    // 인테리어 게시판과 동일한 파일 업로드 처리
    private List<String>[] handleAndReturnFiles(MultipartFile[] files) {
        List<String> filePaths = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        String uploadDir = System.getProperty("user.dir") + "/uploads/";

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String originalName = file.getOriginalFilename();
                    String uniqueName = System.currentTimeMillis() + "_" + originalName;
                    File dest = new File(uploadDir + uniqueName);
                    file.transferTo(dest);

                    filePaths.add("/uploads/" + uniqueName);
                    fileNames.add(uniqueName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new List[]{filePaths, fileNames};
    }

    // 상세정보 삭제 (관리자용)
    @PostMapping("/admin/product-details/{productId}/delete")
    @ResponseBody
    public Map<String, Object> deleteDetail(@PathVariable Long productId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Users loginUser = (Users) UserUtils.getUser(authentication);
            if (loginUser == null) {
                response.put("success", false);
                response.put("message", "권한이 없습니다.");
                return response;
            }

            productDetailService.deleteByProductId(productId);
            response.put("success", true);
            response.put("message", "상세정보가 삭제되었습니다.");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "삭제 중 오류가 발생했습니다.");
            return response;
        }
    }
} 