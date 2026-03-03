package com.example.footballhub_auth_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
    @Column(name = "refresh_token_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_generator")
    @SequenceGenerator(name = "refresh_token_generator", sequenceName = "refresh_token_seq", allocationSize = 1)
    private Long refreshTokenId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "expired_at")
    private Date expiredAt;
}
