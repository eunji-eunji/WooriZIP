package com.example.demo.service;

import com.example.demo.entity.RecommendLog;
import com.example.demo.repository.RecommendLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecommendLogService {

    private final RecommendLogRepository recommendLogRepository;

    public void saveLog(Long userId, Long productId, Long modelId, String actionType, int weight) {
        RecommendLog log = RecommendLog.builder()
                .userId(userId)
                .productId(productId)
                .modelId(modelId)
                .actionType(actionType)
                .weight(weight)
                .timestamp(LocalDateTime.now())
                .build();
        recommendLogRepository.save(log);
    }

}
