package org.example;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {
    private Library lib;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        lib = new Library();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        lib = null;
    }

    @org.junit.jupiter.api.Test
    void init() {
        // *** GIVEN ***
        // class is empty
        // *** WHEN ***
        Book[] books = new Book[] {
                new Book("J.R.R. Tolkien", "Silmallirion"),
                new Book("James Joice", "Ulisses"),
                new Book("Mark Twain", "The adventures of Tom Sawyer")
        };
        this.lib.init(books);
        // *** THEN ***
        List<Book> listBooks;
        try {
            Field f = lib.getClass().getDeclaredField("books");
            f.setAccessible(true);
            listBooks = (List<Book>) f.get(lib);
            f.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertArrayEquals(books, listBooks.toArray());
        assertEquals("Silmallirion", listBooks.get(0).getTitle());
    }

    @org.junit.jupiter.api.Test
    void borrow() {
        // *** GIVEN ***
        // class has books
        Book[] books = new Book[] {
                new Book("J.R.R. Tolkien", "Silmallirion"),
                new Book("James Joice", "Ulisses"),
                new Book("Mark Twain", "The adventures of Tom Sawyer")
        };
        this.lib.init(books);

        List<Book> listBooks;
        try {
            Field f = lib.getClass().getDeclaredField("books");
            f.setAccessible(true);
            listBooks = (List<Book>) f.get(lib);
            f.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // *** WHEN ***
        Book notFound = this.lib.borrow("J.R.R. Tolkien", "Two towers");
        Book b = this.lib.borrow("J.R.R. Tolkien", "Silmallirion");

        // *** THEN ***
        assertNull(notFound, "This book should not exists.");
        assertEquals(b.getId(), 1);
        assertEquals(2, listBooks.size());
    }
}