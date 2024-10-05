import com.oocourse.library2.LibraryBookId;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class User {
    private final String userId;
    private HashMap<LibraryBookId,Integer> books;
    private HashMap<LibraryBookId,Integer> bookC;
    private int numB;

    private int numBu;

    public User(String userId) {
        this.userId = userId;
        this.books = new HashMap<>();
        this.numB = 0;
        this.bookC = new HashMap<>();
        this.numBu = 0;
    }

    public boolean hasBClassBook() {
        return numB != 0;
    }

    public boolean hasBuClassBook() {
        return numBu != 0;
    }

    public boolean hasSameCClassBook(LibraryBookId bookId) {
        return bookC.containsKey(bookId);
    }

    public boolean canBorrowOrNot(LibraryBookId bookId) {
        LibraryBookId.Type type = bookId.getType();
        if (hasBClassBook() && type.equals(LibraryBookId.Type.B)) {
            return false;
        }
        if (hasBuClassBook() && type.equals(LibraryBookId.Type.BU)) {
            return false;
        }
        // 检查是否已经持有相同书号的 C 类书，如果已持有则不能预约该书号的 C 类书
        if (hasSameCClassBook(bookId) && type.equals(LibraryBookId.Type.C)) {
            return false;
        }
        if (hasSameCClassBook(bookId) && type.equals(LibraryBookId.Type.CU)) {
            return false;
        }
        return true;
    }

    public void addBook(LibraryBookId bookId) {
        LibraryBookId.Type type = bookId.getType();
        if (type.equals(LibraryBookId.Type.B)) {
            numB++;
            books.put(bookId,30);
        } else if (type.equals(LibraryBookId.Type.C)) {
            books.put(bookId,60);
            bookC.put(bookId,1);
        } else if (type.equals(LibraryBookId.Type.CU)) {
            books.put(bookId,14);
            bookC.put(bookId,1);
        }  else if (type.equals(LibraryBookId.Type.BU)) {
            books.put(bookId,7);
            numBu++;
        }
    }

    public void removeBook(LibraryBookId bookId) {
        LibraryBookId.Type type = bookId.getType();
        if (type.equals(LibraryBookId.Type.B)) {
            numB--;
            books.remove(bookId);
        } else if (type.equals(LibraryBookId.Type.C) || type.equals(LibraryBookId.Type.CU)) {
            books.remove(bookId);
            bookC.remove(bookId);
        } else if (type.equals(LibraryBookId.Type.BU)) {
            numBu--;
            books.remove(bookId);
        }
    }

    public void throughDays(int differnce) {
        Iterator<Map.Entry<LibraryBookId, Integer>> iterator = books.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LibraryBookId, Integer> entry = iterator.next();
            Integer value = entry.getValue();
            entry.setValue(value - differnce);
        }
    }

    public boolean canRenew(LibraryBookId bookId) {
        return books.containsKey(bookId) && 0 <= books.get(bookId)
                && 4 >= books.get(bookId) && (bookId.isTypeB() || bookId.isTypeC());
    }

    public void renew(LibraryBookId bookId) {
        books.put(bookId,books.get(bookId) + 30);
    }

    public int getDays(LibraryBookId bookId) {
        return books.get(bookId);
    }

}
