package dev.engineeringmadness.starter.logic;

import dev.engineeringmadness.starter.domain.AddRatingRequest;
import dev.engineeringmadness.starter.domain.Book;
import dev.engineeringmadness.starter.domain.BookEntity;
import dev.engineeringmadness.starter.domain.BookStoreState;
import dev.engineeringmadness.starter.repository.BookRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.flux.store.api.v2.Slice;
import org.flux.store.main.v2.DuxSliceBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class BookStore {

    private static final Logger log = LoggerFactory.getLogger(BookStore.class);

    private final BookRepository bookRepository;
    private Slice<BookStoreState> slice;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ModelMapper modelMapper = new ModelMapper();

    @Bean
    public Slice<BookStoreState> bookStoreSlice() {
        List<Book> persistedBooks = bookRepository.findAll().stream()
                .map(x -> modelMapper.map(x, Book.class))
                .toList();
        log.info("Loaded {} books from database on startup", persistedBooks.size());

        BookStoreState initialState = new BookStoreState();
        initialState.setBooks(new ArrayList<>(persistedBooks));

        this.slice = new DuxSliceBuilder<BookStoreState>()
                .setInitialState(initialState)
                .addReducer("addBook", (action, state) -> {
                    Book book = objectMapper.convertValue(action.getPayload(), Book.class);
                    state.getBooks().add(book);
                    log.info("Added book: {}", book.getTitle());
                    return state;
                })
                .addReducer("removeBook", (action, state) -> {
                    String isbn = action.getPayload().toString();
                    state.getBooks().removeIf(book -> book.getIsbn().equals(isbn));
                    log.info("Removed book with ISBN: {}", isbn);
                    return state;
                })
                .addReducer("addRating", (action, state) -> {
                    AddRatingRequest request = objectMapper.convertValue(action.getPayload(), AddRatingRequest.class);
                    final String isbnToFind = request.isbn();
                    int rating = request.rating();
                    if (rating < 1 || rating > 5) {
                        log.warn("Invalid rating {} for ISBN {}. Must be 1-5.", rating, isbnToFind);
                        return state;
                    }
                    state.getBooks().stream()
                            .filter(book -> book.getIsbn().equals(isbnToFind))
                            .findFirst()
                            .ifPresent(book -> {
                                book.setRating(rating);
                                log.info("Added rating {} to book: {}", rating, book.getTitle());
                            });
                    return state;
                })
                .addSubscriber(state -> {
                    log.info("Book store updated. Syncing {} books to database.", state.getBooks().size());
                    syncToDatabase(state);
                })
                .build();

        return this.slice;
    }

    private void syncToDatabase(BookStoreState state) {
        List<BookEntity> entities = state.getBooks().stream()
                .map(x -> bookRepository.findById(x.getIsbn()).isPresent() ?
                        bookRepository.findById(x.getIsbn()).get() : modelMapper.map(x, BookEntity.class))
                .toList();
        bookRepository.deleteAll();
        bookRepository.saveAll(entities);
        log.info("Synced {} books to database", entities.size());
    }
}
