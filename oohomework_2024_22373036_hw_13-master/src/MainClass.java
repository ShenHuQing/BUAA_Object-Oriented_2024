import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;
import com.oocourse.library1.LibrarySystem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        HashMap<LibraryBookId, Integer> allBooks;
        HashMap<String,User> users = new HashMap<>();
        allBooks = (HashMap<LibraryBookId, Integer>) LibrarySystem.SCANNER.getInventory();
        Bookshelf bookshelf = new Bookshelf(allBooks);
        BorrowingAndReturningOffice borrowingAndReturningOffice = new BorrowingAndReturningOffice();
        AppointmentOffice appointmentOffice = new AppointmentOffice();
        ArrayList<Information> appointedInformation = new ArrayList<>();
        Library library = new Library(bookshelf, appointmentOffice,
                borrowingAndReturningOffice,allBooks,users,appointedInformation);
        Manager manager = new Manager(library);
        ArrayList<LibraryMoveInfo> libraryMoveInfos = new ArrayList<>();
        LocalDate prevDate = null;
        int daysDifference = 0;
        while (true) {
            LibraryCommand<?> command = LibrarySystem.SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            LocalDate date = command.getDate();
            if (prevDate != null) {
                daysDifference = (int) ChronoUnit.DAYS.between(prevDate, date);
            }
            if (command.getCmd().equals("OPEN")) {
                library.getAppointmentOffice().throughOneDay(daysDifference);
                manager.openDoor(date);
            } else if (command.getCmd().equals("CLOSE")) {
                manager.closeDoor(date, libraryMoveInfos);
                libraryMoveInfos.clear();

                prevDate = date;
            } else {
                LibraryRequest request = (LibraryRequest) command.getCmd();
                LibraryRequest.Type type = ((LibraryRequest) command.getCmd()).getType();
                switch (type) {
                    case QUERIED:
                        manager.queryBook(request, date);
                        break;
                    case BORROWED:
                        manager.borrowBook(request, date);
                        break;
                    case ORDERED:
                        if (manager.canAppointOrNot(request)) {
                            LibrarySystem.PRINTER.accept(date, request);
                            library.addappointedInformation(request);
                        } else {
                            LibrarySystem.PRINTER.reject(date, request);
                        }
                        break;
                    case RETURNED:
                        manager.returnBook(request, date);
                        break;
                    case PICKED:
                        manager.pickBook(request, date);
                        break;
                    default:
                        System.out.println("error");
                }
            }
        }
    }

}
