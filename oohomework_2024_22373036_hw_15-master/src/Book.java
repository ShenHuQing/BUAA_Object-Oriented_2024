import com.oocourse.library3.LibraryBookId;

public class Book {
    private LibraryBookId bookID;
    private LibraryBookId.Type category;
    private StateMachine.State statement;

    public Book(LibraryBookId bookID, LibraryBookId.Type category) {
        this.bookID = bookID;
        this.category = category;
    }

    public LibraryBookId getBookID() {
        return bookID;
    }

    public void setBookID(LibraryBookId bookID) {
        this.bookID = bookID;
    }

    public LibraryBookId.Type getCategory() {
        return category;
    }

    public void setCategory(LibraryBookId.Type category) {
        this.category = category;
    }

    public void setState(StateMachine.State state) {
        this.statement = state;
    }

}
