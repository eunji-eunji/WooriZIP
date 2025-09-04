package com.example.demo.repository;

import com.example.demo.entity.QnaAnswer;
import com.example.demo.entity.QnaPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
    Optional<QnaAnswer> findByQnaPost(QnaPost qnaPost); // QnA 게시글 기준으로 답변 찾기
    boolean existsByQnaPost(QnaPost qnaPost);
}
