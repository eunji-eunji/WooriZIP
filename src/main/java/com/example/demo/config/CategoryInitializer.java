package com.example.demo.config;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            // ==================== 1. 대분류 + 그 하위 소/세분류 정의 =====================

            Map<String, Map<String, List<String>>> categoryTree = Map.of(
                    // 가구
                    "가구", Map.of(
                            "침대", List.of("침대프레임", "침대+매트리스", "침대부속가구"),
                            "테이블.식탁.책상", List.of("식탁", "사무용책상", "좌식책상"),
                            "소파", List.of("일반소파", "좌식소파", "리클라이너"),
                            "서랍.수납장", List.of("서랍", "수납장", "협탁"),
                            "진열장.책장.선반", List.of("진열장", "책장", "선반"),
                            "의자", List.of("학생.사무용의자", "식탁의자", "스툴", "좌식의자"),
                            "행거.붙박이장", List.of("행거", "붙박이장"),
                            "거울", List.of("전신거울", "벽거울", "탁상거울")
                    ),
                    // 조명
                    "조명", Map.of(
                            "스탠드", List.of("장스탠드", "단스탠드"),
                            "천장등", List.of("펜던트등", "LED등"),
                            "무드등", List.of("USB무드등", "캔들형무드등")
                    ),
                    // 패브릭
                    "패브릭", Map.of(
                            "커튼", List.of("암막커튼", "레이스커튼"),
                            "러그", List.of("주방러그", "거실러그"),
                            "침구", List.of("이불", "베개커버", "패드")
                    ),
                    // 수납/정리
                    "수납/정리", Map.of(
                            "정리함", List.of("서랍형", "뚜껑형"),
                            "옷걸이", List.of("문걸이", "다용도걸이")
                    ),
                    // 주방용품
                    "주방용품", Map.of(
                            "식기", List.of("접시", "그릇", "컵"),
                            "조리도구", List.of("프라이팬", "냄비", "국자")
                    ),
                    // 생활용품
                    "생활용품", Map.of(
                            "욕실용품", List.of("샤워커튼", "디스펜서"),
                            "청소용품", List.of("빗자루", "밀대", "청소기부속")
                    ),
                    // 인테리어소품
                    "인테리어소품", Map.of(
                            "액자", List.of("벽걸이액자", "탁상액자"),
                            "시계", List.of("벽시계", "탁상시계"),
                            "디퓨저", List.of("스틱형", "자동분사형")
                    )
            );

            // ==================== 2. 저장 =====================
            for (String topName : categoryTree.keySet()) {
                Category top = Category.builder()
                        .name(topName)
                        .depth(0)
                        .parent(null)
                        .build();
                categoryRepository.save(top);

                Map<String, List<String>> midMap = categoryTree.get(topName);
                for (String midName : midMap.keySet()) {
                    Category mid = Category.builder()
                            .name(midName)
                            .depth(1)
                            .parent(top)
                            .build();
                    categoryRepository.save(mid);

                    for (String leafName : midMap.get(midName)) {
                        Category leaf = Category.builder()
                                .name(leafName)
                                .depth(2)
                                .parent(mid)
                                .build();
                        categoryRepository.save(leaf);
                    }
                }
            }
        }
    }


}

