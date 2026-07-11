package dev.engineeringmadness.starter.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class BookEntity {

    @Id
    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", length = 2000)
    private String summary;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "pages")
    private int pages;

    @Column(name = "ratings", length = 1000)
    private String ratingsCsv;

    public List<Integer> getRatings() {
        if (ratingsCsv == null || ratingsCsv.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(ratingsCsv.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public void setRatings(List<Integer> ratings) {
        this.ratingsCsv = ratings.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    public static BookEntity fromBook(Book book) {
        BookEntity entity = new BookEntity();
        entity.setIsbn(book.getIsbn());
        entity.setTitle(book.getTitle());
        entity.setSummary(book.getSummary());
        entity.setAuthor(book.getAuthor());
        entity.setPages(book.getNumOfPages());
        entity.setRatings(book.getRatings());
        return entity;
    }

    public Book toBook() {
        Book book = new Book();
        book.setIsbn(this.isbn);
        book.setTitle(this.title);
        book.setSummary(this.summary);
        book.setAuthor(this.author);
        book.setNumOfPages(this.pages);
        book.setRatings(getRatings());
        return book;
    }
}
