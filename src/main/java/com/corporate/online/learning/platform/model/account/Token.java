package com.corporate.online.learning.platform.model.account;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private Boolean expired;
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
