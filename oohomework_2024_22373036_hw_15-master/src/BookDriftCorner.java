import com.oocourse.library3.LibraryBookId;

import java.util.HashMap;

public class BookDriftCorner {
    private HashMap<LibraryBookId, Integer> books;

    private HashMap<LibraryBookId, Integer> onceBooks;

    private HashMap<LibraryBookId, Integer> twiceBooks;

    public BookDriftCorner() {

        this.books = new HashMap<>();
        this.onceBooks = new HashMap<>();
        this.twiceBooks = new HashMap<>();
    }

    public void addBook(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            books.put(bookId, books.get(bookId) + 1);
        } else {
            books.put(bookId,1);
        }
    }

    public void removeAllBook(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            books.remove(bookId);
        }
    }

    public void removeBook(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            books.put(bookId, books.get(bookId) - 1);
            if (books.get(bookId) == 0) {
                books.remove(bookId);
            }
        }
    }

    public void recordTimes(LibraryBookId bookId) {
        if (onceBooks.containsKey(bookId)) {
            twiceBooks.put(bookId,1);
        } else {
            onceBooks.put(bookId, 1);
        }
    }

    public boolean isDoubleBook(LibraryBookId bookId) {
        return twiceBooks.containsKey(bookId);
    }

    public HashMap<LibraryBookId, Integer> getTwiceBooks() {
        return twiceBooks;
    }

    public HashMap<LibraryBookId, Integer> getBooks() {
        return books;
    }

    public int getBookCount(LibraryBookId bookNumber) {
        return books.getOrDefault(bookNumber, 0);
    }
}
