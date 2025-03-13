package net.assessment.springboot.repository;

import net.assessment.springboot.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    // Check if a borrower exists by name
    boolean existsByName(String name);

    // Optional: Check if a borrower exists by email (if needed)
    boolean existsByEmail(String email);

    // Optional: Check if a borrower exists by both name and email
    boolean existsByNameAndEmail(String name, String email);
}