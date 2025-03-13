// Borrower.java
//package com.example.library.model;
package net.assessment.springboot.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
    name = "borrower",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "email")
    }
)
public class Borrower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;
}
