package com.example.demo.service;

import com.example.demo.constant.Role;
import com.example.demo.entity.QnaAnswer;
import com.example.demo.entity.QnaPost;
import com.example.demo.entity.Users;
import com.example.demo.repository.QnaAnswerRepository;
import com.example.demo.repository.QnaPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QnaAnswerService {

    private final QnaAnswerRepository answerRepository;
    private final QnaPostRepository postRepository;

    // 답변 등록 (관리자만)
    @Transactional
    public void saveAnswer(Long postId, String content, Users user) {
        validateAdmin(user);

        QnaPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("질문글이 존재하지 않습니다."));

        if (answerRepository.existsByQnaPost(post)) {
            throw new IllegalStateException("이미 해당 질문에는 답변이 작성되어 있습니다.");
        }

        QnaAnswer answer = QnaAnswer.builder()
                .qnaPost(post)
                .content(content)
                .build();

        answerRepository.save(answer);
    }

    // 답변 수정 (관리자만)
    @Transactional
    public void updateAnswer(Long answerId, String content, Users user) {
        validateAdmin(user);

        QnaAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답변이 존재하지 않습니다."));

        answer.setContent(content);
    }

    // 답변 삭제 (관리자만)
    @Transactional
    public void deleteAnswer(Long answerId, Users user) {
        validateAdmin(user);

        QnaAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답변이 존재하지 않습니다."));

        answerRepository.delete(answer);
    }

    // 관리자 권한 확인
    private void validateAdmin(Users user) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new SecurityException("관리자만 가능합니다.");
        }
    }
}
