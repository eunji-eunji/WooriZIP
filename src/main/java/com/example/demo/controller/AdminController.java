package com.example.demo.controller;

import com.example.demo.dto.CategoryMonthSalesDto;
import com.example.demo.dto.DailyOrderCountDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.Users;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.bind.annotation.ResponseBody;


import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping("/admin/orderCheck")
    public String adminOrderList(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "month") String range,
                                 Model model,
                                 Authentication authentication) throws JsonProcessingException {

        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        Users loginUser = (Users) UserUtils.getUser(authentication);
        model.addAttribute("loginUser", loginUser);

        // 1. 주문 리스트 페이징
        int pageSize = 5;
        Page<Order> pagedOrders = orderService.getPagedOrders(page, pageSize);
        model.addAttribute("orders", pagedOrders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedOrders.getTotalPages());

        // 2. 최근 7일간 주문 수 데이터
        List<DailyOrderCountDto> chartData = orderService.getRecent7DaysOrderCount();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        String labelsJson = mapper.writeValueAsString(
                chartData.stream().map(d -> d.getDate().format(formatter)).toList());
        String countsJson = mapper.writeValueAsString(
                chartData.stream().map(DailyOrderCountDto::getCount).toList());

        model.addAttribute("labelsJson", labelsJson);
        model.addAttribute("countsJson", countsJson);

        // 3. 카테고리별 판매 수
        List<CategoryMonthSalesDto> categoryChartData = range.equals("all")
                ? orderService.getAllCategorySalesByMonth()
                : orderService.getThisMonthCategorySales();
        String categorySalesJson = mapper.writeValueAsString(categoryChartData);

        model.addAttribute("categoryChartData", categorySalesJson);
        model.addAttribute("range", range); // 라디오 버튼 상태 기억용

        return "admin/orderCheck";
    }

    @GetMapping("/admin/orderChartData")
    @ResponseBody
    public List<Map<String, Object>> getCategoryChartData(@RequestParam String range)
    {
        List<Object[]> result;
        if ("all".equals(range)) {
            result = orderRepository.getTotalCategorySales(); // SELECT category, count
            return result.stream()
                    .map(row -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("category", row[0]);
                        map.put("count", ((Number) row[1]).intValue());
                        return map;
                    })
                    .collect(Collectors.toList());
        } else {
            result = orderRepository.getThisMonthCategorySales(); // SELECT month, category,count
            return result.stream()
                    .map(row -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("category", row[1]);
                        map.put("count", ((Number) row[2]).intValue());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }

}
