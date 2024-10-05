import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.oocourse.library2.LibrarySystem.PRINTER;

public class Manager {
    private Library library;

    private BookDriftCorner bookDriftCorner;

    public Manager(Library library,BookDriftCorner bookDriftCorner) {

        this.library = library;
        this.bookDriftCorner = bookDriftCorner;
    }

    public boolean canAppointOrNot(LibraryRequest request) {
        // 获取请求中的图书信息
        LibraryBookId bookId = request.getBookId();
        LibraryBookId.Type type = bookId.getType();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        // 检查是否为 A 类书，A 类书不能预约
        if (type.equals(LibraryBookId.Type.A) || type.equals(LibraryBookId.Type.AU)
            || type.equals(LibraryBookId.Type.BU) || type.equals(LibraryBookId.Type.CU)) {
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
        if (type.equals(LibraryBookId.Type.A) || type.equals(LibraryBookId.Type.AU)) {
            LibraryReqCmd req = new LibraryReqCmd(date, request);
            PRINTER.reject(req);
            return;
        }
        if ((library.getBookshelf().getBookCount(bookId) == 0
                && bookDriftCorner.getBookCount(bookId) == 0)) {
            LibraryReqCmd req = new LibraryReqCmd(date, request);
            PRINTER.reject(req);
            return;
        }
        //用户不存在，没约过书
        if (!users.containsKey(studentId)) {
            library.addUser(studentId);
        }
        User user = users.get(studentId);
        if (user.canBorrowOrNot(bookId)) {
            user.addBook(bookId);
            LibraryReqCmd req = new LibraryReqCmd(date, request);
            PRINTER.accept(req);
            if (type.equals(LibraryBookId.Type.B) || type.equals(LibraryBookId.Type.C)) {
                library.getBookshelf().removeBook(bookId);
                Book book = new Book(bookId,request.getBookId().getType());
                StateMachine.bsToUser(book);
            } else {
                bookDriftCorner.recordTimes(bookId);
                bookDriftCorner.removeBook(bookId);
                Book book = new Book(bookId,request.getBookId().getType());
                StateMachine.bdcToUser(book);
            }
        } else {
            library.getBorrowingAndReturningOffice().addBook(bookId);
            if (type.equals(LibraryBookId.Type.B) || type.equals(LibraryBookId.Type.C)) {
                library.getBookshelf().removeBook(bookId);
                Book book = new Book(bookId,request.getBookId().getType());
                StateMachine.bsToBro(book);
            } else {
                bookDriftCorner.removeBook(bookId);
                Book book = new Book(bookId,request.getBookId().getType());
                StateMachine.bdcToBro(book);
            }
            LibraryReqCmd req = new LibraryReqCmd(date, request);
            PRINTER.reject(req);
        }
    }

    public void returnBook(LibraryRequest request, LocalDate date) {
        LibraryBookId bookId = request.getBookId();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        User user = users.get(studentId);
        library.getBorrowingAndReturningOffice().addBook(bookId);
        int days = user.getDays(bookId);
        user.removeBook(bookId);
        LibraryReqCmd req = new LibraryReqCmd(date, request);
        if (days >= 0) {
            PRINTER.accept(req, "not overdue");
        } else {
            PRINTER.accept(req, "overdue");
        }
        Book book = new Book(bookId,request.getBookId().getType());
        StateMachine.userToBro(book);
    }

    public void queryBook(LibraryRequest request, LocalDate date) {
        LibraryBookId bookId = request.getBookId();
        LibraryBookId.Type type = bookId.getType();
        int num;
        if (type == LibraryBookId.Type.AU ||
                type == LibraryBookId.Type.BU || type == LibraryBookId.Type.CU) {
            num = bookDriftCorner.getBookCount(request.getBookId());
        } else {
            num = library.getBookshelf().getBookCount(request.getBookId());
        }
        LibraryReqCmd req = new LibraryReqCmd(date, request);
        PRINTER.info(req, num);

    }

    public void pickBook(LibraryRequest request, LocalDate date) {
        LibraryBookId bookId = request.getBookId();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        User user = users.get(studentId);
        if (user.canBorrowOrNot(bookId)) {
            if (library.getAppointmentOffice().hasReserved(request)) {
                LibraryReqCmd req = new LibraryReqCmd(date, request);
                PRINTER.accept(req);
                user.addBook(bookId);
                Book book = new Book(bookId,request.getBookId().getType());
                StateMachine.aoToUser(book);
                return;
            }
        }
        LibraryReqCmd req = new LibraryReqCmd(date, request);
        PRINTER.reject(req);
    }

    public void openDoor(LocalDate date, int daysDifference, HashMap<String,User> users) {
        library.getAppointmentOffice().throughOneDay(daysDifference);
        for (Map.Entry<String, User> entry : users.entrySet()) {
            User value = entry.getValue();
            value.throughDays(daysDifference);
        }
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
                    Book book = new Book(bookId,bookId.getType());
                    StateMachine.aoToBs(book);
                }
            }
            library.getAppointmentOffice().getOverduedBook().clear();;
        }
        //归还的书移到书架
        if (!library.getBorrowingAndReturningOffice().getBooks().isEmpty()) {
            returnBooksForLibrary(libraryMoveInfos);
        }
        PRINTER.move(date, libraryMoveInfos);
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
                        iterator.remove();
                        Book book = new Book(information.getBookId(),
                                information.getBookId().getType());
                        StateMachine.bsToAo(book);
                    }
                }
            }
        }
        PRINTER.move(date, libraryMoveInfos);
        libraryMoveInfos.clear();
    }

    private void returnBooksForLibrary(ArrayList<LibraryMoveInfo> libraryMoveInfos) {
        HashMap<LibraryBookId,Integer> books = library.getBorrowingAndReturningOffice().getBooks();
        for (LibraryBookId bookId : books.keySet()) {
            for (int i = 0; i < books.get(bookId); i++) {
                LibraryMoveInfo libraryMoveInfo;
                if (bookId.isTypeBU() || bookId.isTypeCU()) {
                    if (bookDriftCorner.isDoubleBook(bookId)) {
                        libraryMoveInfo = new LibraryMoveInfo(bookId, "bro", "bs");
                        LibraryBookId newBookId = bookId.toFormal();
                        library.getBookshelf().addBook(newBookId);
                        Book book = new Book(bookId,bookId.getType());
                        StateMachine.broToBs(book);
                    } else {
                        libraryMoveInfo = new LibraryMoveInfo(bookId, "bro", "bdc");
                        bookDriftCorner.addBook(bookId);
                        Book book = new Book(bookId,bookId.getType());
                        StateMachine.broToBdc(book);
                    }
                } else {
                    libraryMoveInfo = new LibraryMoveInfo(bookId, "bro", "bs");
                    library.getBookshelf().addBook(bookId);
                    Book book = new Book(bookId,bookId.getType());
                    StateMachine.broToBs(book);
                }
                libraryMoveInfos.add(libraryMoveInfo);
            }
        }
        library.getBorrowingAndReturningOffice().getBooks().clear();
    }

    public void donatedBook(LibraryRequest request, LocalDate date) {
        LibraryBookId bookId = request.getBookId();
        LibraryBookId.Type type = bookId.getType();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        if (!users.containsKey(studentId)) {
            library.addUser(studentId);
        }
        bookDriftCorner.addBook(bookId);
        LibraryReqCmd req = new LibraryReqCmd(date, request);
        PRINTER.accept(req);
        Book book = new Book(bookId,bookId.getType());
        StateMachine.userToBdc(book);
    }

    public void renewBook(LibraryRequest request, LocalDate date) {
        LibraryBookId bookId = request.getBookId();
        LibraryBookId.Type type = bookId.getType();
        String studentId = request.getStudentId();
        HashMap<String, User> users = library.getUsers();
        User user = users.get(studentId);
        LibraryReqCmd req = new LibraryReqCmd(date, request);
        if (user.canRenew(bookId)) {
            if (!library.getAppointmentOffice().isReserved(bookId) && !library.isReserved(bookId)) {
                user.renew(bookId);
                PRINTER.accept(req);
                return;
            } else {
                if (library.getBookshelf().getBookCount(bookId) > 0) {
                    user.renew(bookId);
                    PRINTER.accept(req);
                    return;
                }
            }
        }
        PRINTER.reject(req);
    }

}
