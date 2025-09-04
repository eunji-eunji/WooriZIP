package com.example.demo.interceptor;

import com.example.demo.entity.Users;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.RecommendLogService;
import com.example.demo.utils.SpringContextUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class RecommendInterceptor implements HandlerInterceptor {

    ThreadLocal<StopWatch> stopWatchLocal = new ThreadLocal<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        StopWatch stopWatch = new StopWatch(handler.toString());
        stopWatch.start(handler.toString());
        stopWatchLocal.set(stopWatch);

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 사용자 인증 정보에서 userId만 추출
        Long userId = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            Users user = userDetails.getUser();
            userId = user.getId();
        }

        RecommendLogService recommendLogService = SpringContextUtils.getBean(RecommendLogService.class);

        // Ajax 로그 요청 (POST /recommend/log)
        if ("/recommend/log".equals(uri) && "POST".equalsIgnoreCase(method)) {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            Map<String, Object> json = objectMapper.readValue(sb.toString(), new TypeReference<>() {});
            Long productId = Long.parseLong(json.get("productId").toString());
            Long modelId = json.get("modelId") != null ? Long.parseLong(json.get("modelId").toString()) : null;
            String actionType = json.get("actionType").toString();
            int weight = Integer.parseInt(json.get("weight").toString());

            recommendLogService.saveLog(userId, productId, modelId, actionType, weight);

            log.info("[RECOMMEND] Ajax → userId={}, action={}, productId={}, modelId={}, weight={}",
                    userId, actionType, productId, modelId, weight);
            log.info("[RAW JSON] {}", json);
            log.info("modelId: {}", json.get("modelId"));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("logged");
            return false; // 컨트롤러로 전달하지 않음
        } else if (uri.equals("/cart/add") && "POST".equalsIgnoreCase(method)) {
            // form에서 넘어온 값 파싱
            String modelIdStr = request.getParameter("items[0].modelId");
            String referer = request.getHeader("referer"); // productId 유추용
            String productIdStr = extractProductIdFromReferer(referer);

            Long productId = productIdStr != null ? Long.parseLong(productIdStr) : null;
            Long modelId = modelIdStr != null ? Long.parseLong(modelIdStr) : null;

            // 로그 저장
            recommendLogService.saveLog(userId, productId, modelId, "CART", 3);

            log.info("[RECOMMEND] Cart → userId={}, productId={}, modelId={}, action=CART, weight=3",
                    userId, productId, modelId);
        }

        // /products/{id} 접속 로그 (VIEW 기본 로그)
        String productIdStr = extractProductId(uri);
        if (productIdStr != null) {
            Long productId = Long.parseLong(productIdStr);
            recommendLogService.saveLog(userId, productId, null, "VIEW", 1);

            log.info("[RECOMMEND] PageView → userId={}, productId={}, action=VIEW", userId, productId);
        }

        return true;
    }

    private String extractProductIdFromReferer(String referer) {
        if (referer != null && referer.matches(".*?/products/\\d+")) {
            return referer.substring(referer.lastIndexOf("/") + 1);
        }
        return null;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        StopWatch stopWatch = stopWatchLocal.get();
        if (stopWatch != null) {
            stopWatch.stop();
            stopWatchLocal.remove();
        }
    }

    private String extractProductId(String uri) {
        // 예: /products/123 → "123"
        if (uri != null && uri.matches("^/products/\\d+$")) {
            return uri.substring(uri.lastIndexOf("/") + 1);
        }
        return null;
    }
}
