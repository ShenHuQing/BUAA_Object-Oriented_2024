import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;
import com.oocourse.library1.LibrarySystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Manager {
    private Library library;

    public Manager(Library library) {
        this.library = library;
    }

    public boolean canAppointOrNot(LibraryRequest request) {
        // 获取请求中的图书信息
        LibraryBookId bookId = request.getBookId();
        LibraryBookId.Type type = bookId.getType();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        // 检查是否为 A 类书，A 类书不能预约
        if (type.equals(LibraryBookId.Type.A)) {
            return false;
        }
        //用户不存在，没约过书
        if (!users.containsKey(studentId)) {
            library.addUser(studentId);
            return true;
        }
        User user = users.get(studentId);
        // 检查是否已经持有 B 类书，如果已持有则不能再预约 B 类书
        if (user.hasBClassBook() && type.equals(LibraryBookId.Type.B)) {
            return false;
        }
        // 检查是否已经持有相同书号的 C 类书，如果已持有则不能预约该书号的 C 类书
        if (user.hasSameCClassBook(bookId) && type.equals(LibraryBookId.Type.C)) {
            return false;
        }
        return true;
    }

    public void borrowBook(LibraryRequest request, LocalDate date) {
        // 获取请求中的图书信息
        LibraryBookId bookId = request.getBookId();
        LibraryBookId.Type type = bookId.getType();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        // 检查是否为 A 类书或者书架上没有
        if (type.equals(LibraryBookId.Type.A) || library.getBookshelf().getBookCount(bookId) == 0) {
            LibrarySystem.PRINTER.reject(date, request);
            return;
        }
        //用户不存在，没约过书
        if (!users.containsKey(studentId)) {
            library.addUser(studentId);
        }
        User user = users.get(studentId);
        if (user.canBorrowOrNot(bookId)) {
            user.addBook(bookId);
            LibrarySystem.PRINTER.accept(date, request);
            library.getBookshelf().removeBook(bookId);
        } else {
            library.getBorrowingAndReturningOffice().addBook(bookId);
            library.getBookshelf().removeBook(bookId);
            LibrarySystem.PRINTER.reject(date, request);
        }
    }

    public void returnBook(LibraryRequest request, LocalDate date) {
        LibraryBookId bookId = request.getBookId();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        User user = users.get(studentId);
        library.getBorrowingAndReturningOffice().addBook(bookId);
        user.removeBook(bookId);
        LibrarySystem.PRINTER.accept(date, request);
    }

    public void queryBook(LibraryRequest request, LocalDate date) {
        int num = library.getBookshelf().getBookCount(request.getBookId());
        LibraryBookId bookId = request.getBookId();
        LibrarySystem.PRINTER.info(date, bookId, num);

    }

    public void pickBook(LibraryRequest request, LocalDate date) {
        LibraryBookId bookId = request.getBookId();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        User user = users.get(studentId);
        if (user.canBorrowOrNot(bookId)) {
            if (library.getAppointmentOffice().hasReserved(request)) {
                LibrarySystem.PRINTER.accept(date, request);
                user.addBook(bookId);
                return;
            }
        }
        LibrarySystem.PRINTER.reject(date, request);
    }

    public void openDoor(LocalDate date) {
        //过期的书移动到书架
        ArrayList<LibraryMoveInfo> libraryMoveInfos = new ArrayList<>();
        if (!library.getAppointmentOffice().getOverduedBook().isEmpty()) {
            HashMap<LibraryBookId,Integer> overduedBooks =
                    library.getAppointmentOffice().getOverduedBook();
            for (LibraryBookId bookId : overduedBooks.keySet()) {
                for (int i = 0; i < overduedBooks.get(bookId); i++) {
                    LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(bookId, "ao", "bs");
                    libraryMoveInfos.add(libraryMoveInfo);
                    library.getBookshelf().addBook(bookId);
                }
            }
            library.getAppointmentOffice().getOverduedBook().clear();;
        }
        //归还的书移到书架
        if (!library.getBorrowingAndReturningOffice().getBooks().isEmpty()) {
            returnBooksForLibrary(libraryMoveInfos);
            ;
        }
        LibrarySystem.PRINTER.move(date, libraryMoveInfos);

    }

    public void closeDoor(LocalDate date,ArrayList<LibraryMoveInfo> libraryMoveInfos) {
        //归还的书移到书架
        if (!library.getBorrowingAndReturningOffice().getBooks().isEmpty()) {
            returnBooksForLibrary(libraryMoveInfos);
        }
        // 预约
        if (!library.getAppointedInformation().isEmpty()) {
            Iterator<Information> iterator = library.getAppointedInformation().iterator();
            while (iterator.hasNext()) {
                Information information = iterator.next();
                if (!library.getAppointmentOffice().notSameInformation(information)) {

                    if (library.getBookshelf().getBooksOnShelf().
                            containsKey(information.getBookId())) {
                        library.getAppointmentOffice().addAppointBook(information);
                        LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(information.
                                getBookId(), "bs", "ao", information.getStudentId());
                        libraryMoveInfos.add(libraryMoveInfo);
                        library.getBookshelf().removeBook(information.getBookId());
                    }
                }
                iterator.remove();
            }
        }
        LibrarySystem.PRINTER.move(date, libraryMoveInfos);
        libraryMoveInfos.clear();
    }

    private void returnBooksForLibrary(ArrayList<LibraryMoveInfo> libraryMoveInfos) {
        HashMap<LibraryBookId,Integer> books = library.getBorrowingAndReturningOffice().getBooks();
        for (LibraryBookId bookId : books.keySet()) {
            for (int i = 0; i < books.get(bookId); i++) {
                LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(bookId, "bro", "bs");
                libraryMoveInfos.add(libraryMoveInfo);
                library.getBookshelf().addBook(bookId);
            }
        }
        library.getBorrowingAndReturningOffice().getBooks().clear();
    }

}
