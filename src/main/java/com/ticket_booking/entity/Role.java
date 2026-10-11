package com.ticket_booking.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class Role {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (nullable = false, length = 50)
    private String name;
    @OneToMany(mappedBy = "role")
    private Set<User> users = new HashSet<>();
    
    public Role(String name){
        this.name = name;
    }
    
}
