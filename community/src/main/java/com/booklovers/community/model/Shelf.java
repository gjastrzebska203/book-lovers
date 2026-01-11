package com.booklovers.community.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shelves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shelf {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // np. "Przeczytane", "Fantastyka"
    
    // czy to półka systemowa (nieusuwalna) czy użytkownika?
    private boolean isSystemShelf; 

    // wiele półek -> jeden użytkownik
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // relacja wiele do wielu: półka <-> książki
    // spełnia wymaganie: "@ManyToMany z @JoinTable"
    @ManyToMany
    @JoinTable(
        name = "shelf_books", // nazwa tabeli łączącej w bazie
        joinColumns = @JoinColumn(name = "shelf_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> books;

}
