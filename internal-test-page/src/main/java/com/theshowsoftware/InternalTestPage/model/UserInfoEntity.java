package com.theshowsoftware.InternalTestPage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "userinfo")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class UserInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 ID 자동 생성
    private Long userId; // 회원 고유 id

    @Column(unique = true)
    @NotBlank(message = "아이디는 공백일 수 없습니다.")
    private String userName; // 아이디 (유니크 값)

    @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
    private String password; // 비밀번호

    @Column(nullable = true)
    private double userAmount; // 보유 금액 TODO: 추후 관련 기능 개발 시 구현 예정

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate; // 엔티티 생성 시간

    @LastModifiedDate
    private LocalDateTime updatedDate; // 엔티티 수정 시간

    public UserInfoEntity(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}