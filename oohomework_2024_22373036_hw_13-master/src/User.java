import com.oocourse.library1.LibraryBookId;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String userId;
    private ArrayList<LibraryBookId> books;

    private HashMap<LibraryBookId,Integer> bookC;
    private int numB;

    public User(String userId) {
        this.userId = userId;
        this.books = new ArrayList<>();
        this.numB = 0;
        this.bookC = new HashMap<>();
    }

    public boolean hasBClassBook() {
        return numB != 0;
    }

    public boolean hasSameCClassBook(LibraryBookId bookId) {
        return bookC.containsKey(bookId);
    }

    public boolean canBorrowOrNot(LibraryBookId bookId) {
        LibraryBookId.Type type = bookId.getType();
        if (hasBClassBook() && type.equals(LibraryBookId.Type.B)) {
            return false;
        }
        // 检查是否已经持有相同书号的 C 类书，如果已持有则不能预约该书号的 C 类书
        if (hasSameCClassBook(bookId) && type.equals(LibraryBookId.Type.C)) {
            return false;
        }
        return true;
    }

    public void addBook(LibraryBookId bookId) {
        LibraryBookId.Type type = bookId.getType();
        if (type.equals(LibraryBookId.Type.B)) {
            numB++;
            books.add(bookId);
        } else if (type.equals(LibraryBookId.Type.C)) {
            books.add(bookId);
            bookC.put(bookId,1);
        }
    }

    public void removeBook(LibraryBookId bookId) {
        LibraryBookId.Type type = bookId.getType();
        if (type.equals(LibraryBookId.Type.B)) {
            numB--;
            books.remove(bookId);
        } else if (type.equals(LibraryBookId.Type.C)) {
            books.remove(bookId);
            bookC.remove(bookId);
        }
    }
}
