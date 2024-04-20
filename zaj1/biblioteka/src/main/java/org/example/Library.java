package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Library {
    private final List<Book> books = new ArrayList<Book>();

    public void init(Book[] books) {
        this.books.addAll(Arrays.asList(books));
    }

    public Book borrow(String author, String title) {
        int i = searchForBook(author, title);
        if (i<0) return null;
        Book b = this.books.get(i);
        this.books.remove(i);
        return b;
    }

    private int searchForBook(String author, String title) {
        for (int i=0; i<this.books.size(); ++i) {
            Book b = this.books.get(i);
            if (b.getAuthor() == author && b.getTitle() == b.getTitle()) {
                return i;
            }
        }
        return -1;
    }
}
