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
    private int numOfPages;

    @Column(name = "rating")
    private int rating;
}
