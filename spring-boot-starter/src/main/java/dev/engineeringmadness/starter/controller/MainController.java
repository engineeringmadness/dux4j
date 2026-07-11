package dev.engineeringmadness.starter.controller;

import dev.engineeringmadness.starter.domain.Book;
import dev.engineeringmadness.starter.domain.BookStoreState;
import dev.engineeringmadness.starter.domain.DispatchRequest;
import org.flux.store.api.v2.Slice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private Slice<BookStoreState> bookStoreSlice;

    @PostMapping("/dispatch")
    public ResponseEntity<?> dispatch(@RequestBody DispatchRequest request) {
        try {
            bookStoreSlice.getAction(request.type()).accept(request.payload());
            return ResponseEntity.ok(bookStoreSlice.getState());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookStoreSlice.getState().getBooks();
        return ResponseEntity.ok(books);
    }

}
