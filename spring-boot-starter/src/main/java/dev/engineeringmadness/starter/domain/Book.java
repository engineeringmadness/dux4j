package dev.engineeringmadness.starter.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String title;
    private String summary;
    private String author;
    private String isbn;
    private int numOfPages;
    private List<Integer> ratings = new ArrayList<>();
}
