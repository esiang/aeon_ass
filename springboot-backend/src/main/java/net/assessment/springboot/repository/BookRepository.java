package net.assessment.springboot.repository;
import net.assessment.springboot.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;


public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByIsbn(String isbn);
    
    // Optional: Check if a book exists by ISBN
    boolean existsByIsbn(String isbn);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :bookId")
    Optional<Book> findByIdForUpdate(@Param("bookId") Long bookId);

}