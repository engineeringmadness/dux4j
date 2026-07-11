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
            return (BookStoreState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
