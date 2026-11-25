package com.ticket_booking.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "user_security")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")                                     
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserSecurity {

    @Id
    @Column(name = "user_id")
    @EqualsAndHashCode.Include                                    
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @Column(name = "window_started_at")
    private Instant windowStartedAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_failed_at")
    private Instant lastFailedAt;

    @Column(name = "last_failed_ip", length = 45)
    private String lastFailedIp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();

        if(this.failedAttempts == null){
            this.failedAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getUserSecurity() != this) {
            user.setUserSecurity(this);
        }
    }

    public boolean isLocked(){
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public long getRemainingLockMinutes(){
        if(!isLocked()) return 0;
        Duration remaining = Duration.between(Instant.now(), lockedUntil);
        return remaining.toMinutes() + 1;
    }
}