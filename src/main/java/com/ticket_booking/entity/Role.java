package com.ticket_booking.entity;

import java.util.HashSet;
import java.util.Set;

import com.ticket_booking.enums.RoleType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "roles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_roles_type", columnNames = "type"),
        @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private RoleType type;

    @Column(name = "name", nullable = false, length = 50)
    @EqualsAndHashCode.Include
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public void addPermission(Permission permission){
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission){
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }
}