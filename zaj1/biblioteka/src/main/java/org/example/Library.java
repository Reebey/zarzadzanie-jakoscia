package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Library {
    private final List<Book> books = new ArrayList<Book>();
    private final List<Book> borrowed = new ArrayList<Book>();

    public void init(Book[] books) {
        this.books.addAll(Arrays.asList(books));
    }

    public Book borrow(String author, String title) {
        int i = searchForBook(this.books, author, title);
        if (i<0) return null;
        Book b = this.books.get(i);
        this.books.remove(i);
        this.borrowed.add(b);
        return b;
    }

    public boolean giveBack(Book book) {
        int i = searchForBook(this.borrowed, book.getAuthor(), book.getTitle());
        if (i<0) return false;
        Book b = this.borrowed.get(i);
        this.borrowed.remove(i);
        this.books.add(b);
        return true;
    }

    private int searchForBook(List<Book> books, String author, String title) {
        for (int i=0; i<books.size(); ++i) {
            Book b = books.get(i);
            if (b.getAuthor() == author && b.getTitle() == title) {
                return i;
            }
        }
        return -1;
    }
}
