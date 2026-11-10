package com.ticket_booking.entity;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringSummary;
import org.hibernate.annotations.ManyToAny;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 50)
    private String resoure;

    @Column(nullable = false, length = 50)
    private String action;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore // Tránh vòng lặp json
    @ToString.Exclude // tránh vòng lặp khi log
    @EqualsAndHashCode.Exclude // tránh bàng lăp khi equals
    private Set<Role> roles = new HashSet<>();
}
