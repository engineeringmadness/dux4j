package dev.engineeringmadness.starter.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flux.store.api.v1.State;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookStoreState implements State {

    private List<Book> books = new ArrayList<>();

    @Override
    public BookStoreState clone() {
        try {
            BookStoreState copy = (BookStoreState) super.clone();
            copy.books = new ArrayList<>();
            for (Book book : this.books) {
                Book bookCopy = new Book();
                bookCopy.setTitle(book.getTitle());
                bookCopy.setSummary(book.getSummary());
                bookCopy.setAuthor(book.getAuthor());
                bookCopy.setIsbn(book.getIsbn());
                bookCopy.setNumOfPages(book.getNumOfPages());
                bookCopy.setRatings(new ArrayList<>(book.getRatings()));
                copy.books.add(bookCopy);
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
