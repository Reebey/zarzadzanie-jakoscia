package library;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {

    private Library lib;

    @BeforeEach
    void setUp() {
        lib = new Library();
    }

    @AfterEach
    void tearDown() {
        lib = null;
    }

    @Test
    void init() {
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmarillion"),
                new Book("J. Joyce", "Ulysses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        lib.init(books);
        //*** THEN ***
        try {
            List<Book> listBooks;
            Field f = lib.getClass().getDeclaredField("books");
            f.setAccessible(true);
            listBooks = (List<Book>) f.get(lib);
            f.setAccessible(false);
            assertEquals(3, listBooks.size());
            assertEquals("Silmarillion", listBooks.get(0).getTitle());
        } catch(Exception e){
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void successfulBorrow() {
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmarillion"),
                new Book("J. Joyce", "Ulysses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        lib.init(books);
        User user = new User("John Doe");
        //*** WHEN ***
        Book b = lib.borrow(user, "J. Joyce", "Ulysses");
        //*** THEN ***
        assertNotNull(b);
        assertEquals("J. Joyce", b.getAuthor());
        try {
            Field f = lib.getClass().getDeclaredField("borrowedBooks");
            f.setAccessible(true);
            Map<User, List<Book>> borrowedBooks = (Map<User, List<Book>>) f.get(lib);
            f.setAccessible(false);
            assertTrue(borrowedBooks.get(user).contains(b));
        } catch(Exception e){
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void unsuccessfulBorrow(){
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmarillion"),
                new Book("J. Joyce", "Ulysses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        lib.init(books);
        User user = new User("John Doe");
        //*** WHEN ***
        Book b = lib.borrow(user, "J.R.R. Tolkien", "The Two Towers");
        //*** THEN ***
        assertNull(b, "This book should not exist");
        try {
            Field f = lib.getClass().getDeclaredField("borrowedBooks");
            f.setAccessible(true);
            Map<User, List<Book>> borrowedBooks = (Map<User, List<Book>>) f.get(lib);
            f.setAccessible(false);
            assertFalse(borrowedBooks.containsKey(user));
        } catch(Exception e){
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void successfulReturn() {
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmarillion"),
                new Book("J. Joyce", "Ulysses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        lib.init(books);
        User user = new User("John Doe");
        lib.borrow(user, "J. Joyce", "Ulysses");
        //*** WHEN ***
        boolean returned = lib.returnBook(user, "J. Joyce", "Ulysses");
        //*** THEN ***
        assertTrue(returned);
        try {
            Field f = lib.getClass().getDeclaredField("borrowedBooks");
            f.setAccessible(true);
            Map<User, List<Book>> borrowedBooks = (Map<User, List<Book>>) f.get(lib);
            f.setAccessible(false);
            assertFalse(borrowedBooks.get(user).contains(new Book("J. Joyce", "Ulysses")));
        } catch(Exception e){
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void unsuccessfulReturn() {
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmarillion"),
                new Book("J. Joyce", "Ulysses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        lib.init(books);
        User user = new User("John Doe");
        lib.borrow(user, "J. Joyce", "Ulysses");
        //*** WHEN ***
        boolean returned = lib.returnBook(user, "J.R.R. Tolkien", "The Two Towers");
        //*** THEN ***
        assertFalse(returned);
        try {
            Field f = lib.getClass().getDeclaredField("borrowedBooks");
            f.setAccessible(true);
            Map<User, List<Book>> borrowedBooks = (Map<User, List<Book>>) f.get(lib);
            f.setAccessible(false);
            assertTrue(borrowedBooks.get(user).contains(new Book("J. Joyce", "Ulysses")));
        } catch(Exception e){
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void saveAndLoadData() {
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmarillion"),
                new Book("J. Joyce", "Ulysses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        lib.init(books);
        User user = new User("John Doe");
        lib.borrow(user, "J. Joyce", "Ulysses");
        String filePath = "library_data.ser";
        try {
            lib.saveData(filePath);
            Library newLib = new Library();
            newLib.loadData(filePath);
            //*** THEN ***
            Field f = newLib.getClass().getDeclaredField("borrowedBooks");
            f.setAccessible(true);
            Map<User, List<Book>> borrowedBooks = (Map<User, List<Book>>) f.get(newLib);
            f.setAccessible(false);
            assertTrue(borrowedBooks.get(user).contains(new Book("J. Joyce", "Ulysses")));
        } catch(Exception e){
            fail("Exception occurred: " + e.getMessage());
        }
    }
}
