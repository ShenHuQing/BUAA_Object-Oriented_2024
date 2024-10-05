import com.oocourse.library3.annotation.Trigger;

public class StateMachine {
    enum State {
        InitState,
        AppointmentOffice,
        BookDriftCorner,
        Bookshelf,
        BorrowingAndReturningOffice,
        User
    }

    @Trigger(from = "InitState", to = "Bookshelf")
    public static void iniToBs(Book book) {
        book.setState(State.Bookshelf);
    }

    @Trigger(from = "AppointmentOffice", to = "Bookshelf")
    public static void aoToBs(Book book) {
        book.setState(State.Bookshelf);
    }

    @Trigger(from = "AppointmentOffice", to = "User")
    public static void aoToUser(Book book) {
        book.setState(State.User);
    }

    @Trigger(from = "Bookshelf", to = "User")
    public static void bsToUser(Book book) {
        book.setState(State.User);
    }

    @Trigger(from = "Bookshelf", to = "AppointmentOffice")
    public static void bsToAo(Book book) {
        book.setState(State.AppointmentOffice);
    }

    @Trigger(from = "Bookshelf", to = "BorrowingAndReturningOffice")
    public static void bsToBro(Book book) {
        book.setState(State.BorrowingAndReturningOffice);
    }

    @Trigger(from = "BookDriftCorner", to = "User")
    public static void bdcToUser(Book book) {
        book.setState(State.User);
    }

    @Trigger(from = "BookDriftCorner", to = "BorrowingAndReturningOffice")
    public static void bdcToBro(Book book) {
        book.setState(State.BorrowingAndReturningOffice);
    }

    @Trigger(from = "BorrowingAndReturningOffice", to = "BookDriftCorner")
    public static void broToBdc(Book book) {
        book.setState(State.BookDriftCorner);
    }

    @Trigger(from = "BorrowingAndReturningOffice", to = "Bookshelf")
    public static void broToBs(Book book) {
        book.setState(State.Bookshelf);
    }

    @Trigger(from = "User", to = "BorrowingAndReturningOffice")
    public static void userToBro(Book book) {
        book.setState(State.BorrowingAndReturningOffice);
    }

    @Trigger(from = "User", to = "BookDriftCorner")
    public static void userToBdc(Book book) {
        book.setState(State.BookDriftCorner);
    }
}