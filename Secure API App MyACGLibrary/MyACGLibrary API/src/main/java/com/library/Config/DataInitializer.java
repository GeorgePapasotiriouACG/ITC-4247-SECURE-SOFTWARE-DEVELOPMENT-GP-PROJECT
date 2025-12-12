/*
 * DataInitializer - Database Initialization
 * Created by: George Papasotiriou
 * Date: 2024-01-15
 *
 * Initializes database with sample data on application startup
 */

package com.library.Config;

import com.library.Model.User;
import com.library.Model.Book;
import com.library.Model.BorrowRecord;
import com.library.Repository.UserRepository;
import com.library.Repository.BookRepository;
import com.library.Repository.BorrowRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BookRepository bookRepository,
                           BorrowRecordRepository borrowRecordRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n📊 Initializing database with sample data...");

        // Clear existing data
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        User admin = new User("admin", passwordEncoder.encode("password123"), "ROLE_ADMIN");
        User alice = new User("alice", passwordEncoder.encode("password123"), "ROLE_USER");
        User bob = new User("bob", passwordEncoder.encode("password123"), "ROLE_USER");
        User charlie = new User("charlie", passwordEncoder.encode("password123"), "ROLE_USER");

        userRepository.save(admin);
        userRepository.save(alice);
        userRepository.save(bob);
        userRepository.save(charlie);

        // Create books
        Book book1 = new Book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565");
        Book book2 = new Book("To Kill a Mockingbird", "Harper Lee", "9780061120084");
        Book book3 = new Book("1984", "George Orwell", "9780451524935");
        Book book4 = new Book("Pride and Prejudice", "Jane Austen", "9780141439518");
        Book book5 = new Book("The Hobbit", "J.R.R. Tolkien", "9780547928227");
        Book book6 = new Book("Harry Potter and the Philosopher's Stone", "J.K. Rowling", "9780747532743");
        Book book7 = new Book("The Catcher in the Rye", "J.D. Salinger", "9780316769488");
        Book book8 = new Book("The Lord of the Rings", "J.R.R. Tolkien", "9780544003415");

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
        bookRepository.save(book4);
        bookRepository.save(book5);
        bookRepository.save(book6);
        bookRepository.save(book7);
        bookRepository.save(book8);

        // Create borrow records
        BorrowRecord record1 = new BorrowRecord(book1, alice);
        record1.setBorrowDate(LocalDate.now().minusDays(10));

        BorrowRecord record2 = new BorrowRecord(book3, bob);
        record2.setBorrowDate(LocalDate.now().minusDays(5));

        borrowRecordRepository.save(record1);
        borrowRecordRepository.save(record2);

        // Update book availability
        book1.setAvailable(false);
        book3.setAvailable(false);
        bookRepository.save(book1);
        bookRepository.save(book3);

        System.out.println("✅ Database initialized with:");
        System.out.println("   👥 " + userRepository.count() + " users");
        System.out.println("   📚 " + bookRepository.count() + " books");
        System.out.println("   📝 " + borrowRecordRepository.count() + " borrow records");
    }
}