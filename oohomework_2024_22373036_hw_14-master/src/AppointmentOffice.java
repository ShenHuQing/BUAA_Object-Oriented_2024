import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AppointmentOffice {
    private ArrayList<Information> appointedBooks;
    private HashMap<LibraryBookId,Integer> overduedBook;

    public AppointmentOffice() {
        this.appointedBooks = new ArrayList<>();
        this.overduedBook = new HashMap<>();
    }

    public boolean notSameInformation(Information information) {
        return appointedBooks.contains(information);
    }

    public void addAppointBook(Information information) {
        this.appointedBooks.add(information);
    }

    public void throughOneDay(int differnce) {
        Iterator<Information> iterator = appointedBooks.iterator();
        while (iterator.hasNext()) {
            Information information = iterator.next();
            information.setTerm(information.getTerm() - differnce);
            // 执行图书逾期处理
            if (information.getTerm() <= 0) {
                if (overduedBook.containsKey(information.getBookId())) {
                    overduedBook.put(information.getBookId(),
                            overduedBook.get(information.getBookId()) + 1);
                } else {
                    overduedBook.put(information.getBookId(),1);
                }
                iterator.remove();
            }
        }
    }

    public boolean hasReserved(LibraryRequest request) {
        LibraryBookId bookId = request.getBookId();
        String studentId = request.getStudentId();
        for (Iterator<Information> iterator = appointedBooks.iterator(); iterator.hasNext();) {
            Information info = iterator.next();
            if (info.getBookId().equals(bookId) && info.getStudentId().equals(studentId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean isReserved(LibraryBookId bookId) {
        for (Information information : appointedBooks) {
            if (information.getBookId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    public HashMap<LibraryBookId, Integer> getOverduedBook() {
        return overduedBook;
    }

    public ArrayList<Information> getAppointedBooks() {
        return appointedBooks;
    }
}
