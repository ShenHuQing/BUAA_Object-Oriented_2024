import com.oocourse.library3.LibraryBookId;

import java.util.HashMap;

public class BorrowingAndReturningOffice {
    private HashMap<LibraryBookId, Integer> books;

    public BorrowingAndReturningOffice() {
        this.books = new HashMap<>();
    }

    public void addBook(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            books.put(bookId, books.get(bookId) + 1);
        } else {
            books.put(bookId,1);
        }
    }

    public void removeBook(Book book) {
        if (books.containsKey(book.getBookID())) {
            books.put(book.getBookID(), books.get(book.getBookID()) - 1);
            if (books.get(book.getBookID()) == 0) {
                books.remove(book.getBookID());
            }
        }
    }

    public HashMap<LibraryBookId, Integer> getBooks() {
        return books;
    }

    public void moveBook(){
    }
}
