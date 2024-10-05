import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class Bookshelf {
    private HashMap<LibraryBookId, Integer> booksOnShelf; // 存放在书架上的书籍

    public Bookshelf(HashMap<LibraryBookId, Integer> booksOnShelf) {
        this.booksOnShelf = booksOnShelf;
    }

    public void addBook(LibraryBookId bookId) {
        if (booksOnShelf.containsKey(bookId)) {

            booksOnShelf.put(bookId, booksOnShelf.get(bookId) + 1);
        } else {
            booksOnShelf.put(bookId,1);
        }
    }

    // 从书架移除书籍
    public void removeBook(LibraryBookId bookId) {
        if (booksOnShelf.containsKey(bookId)) {
            booksOnShelf.put(bookId, booksOnShelf.get(bookId) - 1);
            if (booksOnShelf.get(bookId) == 0) {
                booksOnShelf.remove(bookId);
            }
        }
    }

    public int getBookCount(LibraryBookId bookNumber) {
        return booksOnShelf.getOrDefault(bookNumber, 0);
    }

    public HashMap<LibraryBookId, Integer> getBooksOnShelf() {
        return booksOnShelf;
    }

    public void setBooksOnShelf(HashMap<LibraryBookId, Integer> booksOnShelf) {
        this.booksOnShelf = booksOnShelf;
    }
}
