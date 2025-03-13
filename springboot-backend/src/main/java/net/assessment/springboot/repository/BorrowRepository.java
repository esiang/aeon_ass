package net.assessment.springboot.repository;

import net.assessment.springboot.model.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    // Check if the book is already borrowed and not yet returned
    boolean existsByBookIdAndReturnTimeIsNull(Long bookId);

    // Find an active borrow record for a specific book and borrower
    Optional<Borrow> findByBookIdAndReturnTimeIsNull(Long bookId);
}