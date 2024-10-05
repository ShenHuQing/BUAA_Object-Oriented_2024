import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryRequest;
import java.util.ArrayList;
import java.util.HashMap;

public class Library {
    private Bookshelf bookshelf;
    private AppointmentOffice appointmentOffice;
    private BorrowingAndReturningOffice borrowingAndReturningOffice;
    private HashMap<LibraryBookId,Integer> allBooksInLibrary;

    private ArrayList<Information> appointedInformation;
    private HashMap<String,User> users;

    public Library(Bookshelf bookshelf, AppointmentOffice appointmentOffice,
                   BorrowingAndReturningOffice borrowingAndReturningOffice,
                   HashMap<LibraryBookId,Integer> allBooksInLibrary,
                   HashMap<String,User> users,ArrayList<Information> appointedInformation) {
        this.bookshelf = bookshelf;
        this.appointmentOffice = appointmentOffice;
        this.borrowingAndReturningOffice = borrowingAndReturningOffice;
        this.allBooksInLibrary = allBooksInLibrary;
        this.users = users;
        this.appointedInformation = appointedInformation;
    }

    public HashMap<LibraryBookId, Integer> getAllBooksInLibrary() {
        return allBooksInLibrary;
    }

    public void addappointedInformation(LibraryRequest request) {
        Information information = new Information(request.getBookId(),
                request.getStudentId(),true,6);
        this.appointedInformation.add(information);
    }

    public Bookshelf getBookshelf() {
        return bookshelf;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    public void addUser(String studentId) {
        User user = new User(studentId);
        getUsers().put(studentId,user);
    }

    public AppointmentOffice getAppointmentOffice() {
        return appointmentOffice;
    }

    public BorrowingAndReturningOffice getBorrowingAndReturningOffice() {
        return borrowingAndReturningOffice;
    }

    public ArrayList<Information> getAppointedInformation() {
        return appointedInformation;
    }

    public void clearAppointedInformation() {
        appointedInformation.clear();
    }
}
