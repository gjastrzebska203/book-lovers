package com.booklovers.community;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.repository.BookRepository;

@SpringBootApplication
public class CommunityApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

	@Bean
    CommandLineRunner init(AuthorRepository authorRepository, BookRepository bookRepository) {
        return args -> {
            if (bookRepository.count() > 0) return; // Nie dodawaj jeśli baza pełna

            // 1. Dodaj Autora
            Author author = new Author();
            author.setFirstName("Andrzej");
            author.setLastName("Sapkowski");
            author.setBio("Mistrz polskiej fantastyki");
            authorRepository.save(author);

            // 2. Dodaj 15 książek w pętli (do testu paginacji)
            for (int i = 1; i <= 15; i++) {
                Book book = Book.builder()
                        .title("Wiedźmin Tom " + i)
                        .isbn("978-83-" + (1000 + i))
                        .description("Przygody Geralta z Rivii. Część " + i)
                        .author(author)
                        .build();
                bookRepository.save(book);
            }
            System.out.println(">>> Dodano dane testowe!");
        };
    }

}
