// LibraryController.java
package net.assessment.springboot.controller;

import net.assessment.springboot.model.Book;
import net.assessment.springboot.model.Borrower;
import net.assessment.springboot.model.Borrow;
import net.assessment.springboot.repository.BookRepository;
import net.assessment.springboot.repository.BorrowRepository;
import net.assessment.springboot.repository.BorrowerRepository;
import net.assessment.springboot.exception.BookValidationException;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LibraryController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private BorrowRepository borrowRepository;


    // curl -X POST http://localhost:8080/api/borrowers -H "Content-Type: application/json" -d "{\"name\": \"John Doe\", \"email\": \"john@example.com\"}"
    // curl -X POST http://localhost:8080/api/borrowers -H "Content-Type: application/json" -d '{"name": "John Doe", "email": "john@example.com"}'
    @PostMapping("/borrowers")
    public ResponseEntity<?> addBorrower(@RequestBody Borrower borrower) {
        // Pre-checks for existing name
        if (borrowerRepository.existsByName(borrower.getName())) {
            return ResponseEntity.badRequest().body("Borrower with name '" + borrower.getName() + "' already exists.");
        }

        // Pre-checks for existing email
        if (borrowerRepository.existsByEmail(borrower.getEmail())) {
            return ResponseEntity.badRequest().body("Borrower with email '" + borrower.getEmail() + "' already exists.");
        }

        try {
            Borrower savedBorrower = borrowerRepository.save(borrower);
            return ResponseEntity.ok(savedBorrower);

        } catch (DataIntegrityViolationException e) {
            // Catch any unexpected constraint violations - A unique constraint was violated. 
            return ResponseEntity.badRequest().body("A unique constraint was violated. Please check the input data. (hit: same borrower's email or borrower's name)");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("unknown exception: " + e.getMessage());
        } 
    }

    // curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" -d "{\"isbn\": \"1234567890\", \"title\": \"Spring Boot Guide\", \"author\": \"Jane Smith\", \"borrow\": 0}"
    // curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" -d '{"isbn": "1234567890", "title": "Spring Boot Guide", "author": "Jane Smith", "borrow": 0}'
    @PostMapping("/books")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            List<Book> existingBooks = bookRepository.findByIsbn(book.getIsbn());

            // Validate title and author for all existing books with the same ISBN
            for (Book existingBook : existingBooks) {
                if (!existingBook.getTitle().equals(book.getTitle()) || 
                    !existingBook.getAuthor().equals(book.getAuthor())) {
                    throw new BookValidationException(
                        "A book with ISBN '" + book.getIsbn() + "' already exists with a different title or author."
                    );
                }
            }

            // Save the book if no validation issues
            Book savedBook = bookRepository.save(book);
            return ResponseEntity.ok(savedBook);

        } catch (BookValidationException e) {
            return ResponseEntity.badRequest().body("Book Validation: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Data integrity violation: " + e.getRootCause().getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("unknown exception: " + e.getMessage());
        }
    }


    // curl -X GET http://localhost:8080/api/books
    // List all books
    @GetMapping("/books")
    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    // curl -X POST "http://localhost:8080/api/borrow/1?bookId=1&borrowerId=1"
    @Transactional
    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<String> borrowBook(@RequestParam Long bookId, @RequestParam Long borrowerId) {

        try {

            // Lock the book record for update
            Optional<Book> bookOpt = bookRepository.findByIdForUpdate(bookId);
            Optional<Borrower> borrowerOpt = borrowerRepository.findById(borrowerId);

            if (bookOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Book does not exist.");
            }

            if (borrowerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Borrower does not exist.");
            }

            Book book = bookOpt.get();

            // Check if the book is already borrowed
            if (book.getBorrow() == 1) {
                return ResponseEntity.badRequest().body("Book is already borrowed.");
            }

            // Mark the book as borrowed
            book.setBorrow(1);
            bookRepository.save(book);

            // Record the borrowing
            Borrow borrow = new Borrow();
            borrow.setBook(book);
            borrow.setBorrower(borrowerOpt.get());
            borrow.setBorrowTime(LocalDateTime.now());
            borrowRepository.save(borrow);

            return ResponseEntity.ok("Book borrowed successfully.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("unknown exception: " + e.getMessage());
        } 
    }


    // curl -X POST "http://localhost:8080/api/return/1?bookId=1"
    // Return a book
    @Transactional
    @PostMapping("/return/{bookId}")
    public ResponseEntity<String> returnBook(@RequestParam Long bookId) {

        try {

            Optional<Book> bookOpt = bookRepository.findByIdForUpdate(bookId);
            Optional<Borrow> borrowOpt = borrowRepository.findByBookIdAndReturnTimeIsNull(bookId);

            if (bookOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Book does not exist.");
            }

            if (borrowOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Book is not borrowed.");
            }

            Book book = bookOpt.get();
            if (book.getBorrow() == 0) {
                return ResponseEntity.badRequest().body("Book is not borrowed.");
            }

            // Mark the book was returned
            book.setBorrow(0);
            bookRepository.save(book);

            Borrow borrow = borrowOpt.get();
            borrow.setReturnTime(LocalDateTime.now());
            borrowRepository.save(borrow);
            return ResponseEntity.ok("Book returned successfully.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("unknown exception: " + e.getMessage());
        } 
    }
}
