import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryCloseCmd;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.oocourse.library2.LibrarySystem.PRINTER;
import static com.oocourse.library2.LibrarySystem.SCANNER;

public class MainClass {
    public static void main(String[] args) {
        HashMap<LibraryBookId, Integer> allBooks;
        HashMap<String,User> users = new HashMap<>();
        allBooks = (HashMap<LibraryBookId, Integer>) SCANNER.getInventory();
        for (Map.Entry<LibraryBookId, Integer> entry : allBooks.entrySet()) {
            Book book = new Book(entry.getKey(),entry.getKey().getType());
            StateMachine.iniToBs(book);
        } Bookshelf bookshelf = new Bookshelf(allBooks);
        BorrowingAndReturningOffice borrowingAndReturningOffice = new BorrowingAndReturningOffice();
        AppointmentOffice appointmentOffice = new AppointmentOffice();
        ArrayList<Information> appointedInformation = new ArrayList<>();
        BookDriftCorner bookDriftCorner = new BookDriftCorner();
        Library library = new Library(bookshelf, appointmentOffice,
                borrowingAndReturningOffice,allBooks,users,appointedInformation);
        Manager manager = new Manager(library,bookDriftCorner);
        ArrayList<LibraryMoveInfo> libraryMoveInfos = new ArrayList<>();
        LocalDate prevDate = null;
        int daysDifference = 0;
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            LocalDate today = command.getDate(); // 今天的日期
            if (prevDate != null) {
                daysDifference = (int) ChronoUnit.DAYS.between(prevDate, today);
            } if (command instanceof LibraryOpenCmd) {
                manager.openDoor(today,daysDifference,users);
            } else if (command instanceof LibraryCloseCmd) {
                manager.closeDoor(today, libraryMoveInfos);
                libraryMoveInfos.clear();
                prevDate = today;
            } else {
                LibraryRequest request = ((LibraryReqCmd) command).getRequest();
                LibraryRequest.Type type = request.getType(); // 指令对应的类型（查询/借阅/预约/还书/取书/续借/捐赠）
                switch (type) {
                    case QUERIED: manager.queryBook(request, today);
                        break;
                    case BORROWED: manager.borrowBook(request, today);
                        break;
                    case ORDERED:
                        if (manager.canAppointOrNot(request)) {
                            LibraryReqCmd req = new LibraryReqCmd(today,request);
                            PRINTER.accept(req);
                            library.addappointedInformation(request);
                        } else {
                            LibraryReqCmd req = new LibraryReqCmd(today,request);
                            PRINTER.reject(req);
                        } break;
                    case RETURNED: manager.returnBook(request, today);
                        break;
                    case PICKED: manager.pickBook(request, today);
                        break;
                    case DONATED: manager.donatedBook(request,today);
                        break;
                    case RENEWED: manager.renewBook(request,today);
                        break;
                    default: System.out.println("error1");
                }
            }
        }
    }
}
