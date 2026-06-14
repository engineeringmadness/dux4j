package org.flux.store.tests.utils;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v1.Reducer;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.utils.Utilities;
import org.flux.store.tests.domain.Author;
import org.flux.store.tests.domain.Book;
import org.flux.store.tests.domain.CombinedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CombinedReducerTest {

    public static final String INITIAL_BOOK = "The Making of a manager";
    public static final String INITIAL_AUTHOR = "Julie Zhou";
    public static final String ACTION_SET_BOOK = "SET_BOOK";
    public static final String ACTION_SET_AUTHOR = "SET_AUTHOR";

    private DuxStore<CombinedState> myStore;

    @BeforeEach
    public void init() {

        CombinedState initialState = new CombinedState();
        initialState.setBook(new Book(INITIAL_BOOK));
        initialState.setAuthor(new Author(INITIAL_AUTHOR));

        Reducer<CombinedState> authorReducer = (action, state) -> {
            switch (action.getType()) {
                case ACTION_SET_AUTHOR:
                    Author author = (Author) action.getPayload();
                    state.setAuthor(author);
                    break;
            }
            return state;
        };

        Reducer<CombinedState> bookReducer = (action, state) -> {
            switch (action.getType()) {
                case ACTION_SET_BOOK:
                    Book book = (Book) action.getPayload();
                    state.setBook(book);
                    break;
            }
            return state;
        };

        Reducer<CombinedState> reducer = Utilities.combineReducer(authorReducer, bookReducer);

        myStore = new DuxStore<>(initialState, reducer);
    }

    @Test
    public void separateReducersCanUpdateSeparateSlices() {
        String newBook = "Invent and Wander";
        String newAuthor = "Jeff Bezos";
        Action<Author> action1 = Utilities.actionCreator(ACTION_SET_AUTHOR, new Author(newAuthor));
        Action<Book> action2 = Utilities.actionCreator(ACTION_SET_BOOK, new Book(newBook));
        System.out.println(myStore.getState());
        myStore.dispatch(action1);
        System.out.println(myStore.getState());
        myStore.dispatch(action2);
        System.out.println(myStore.getState());
        assertEquals(newAuthor, myStore.getState().getAuthor().getName());
        assertEquals(newBook, myStore.getState().getBook().getName());
    }
}
