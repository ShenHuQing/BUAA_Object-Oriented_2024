import com.oocourse.library2.LibraryBookId;

public class Information {
    private final LibraryBookId bookId;
    private final String studentId;

    private boolean isAppointed;

    private boolean isArrived;
    private int term;

    public Information(LibraryBookId bookId, String studentId, boolean isAppointed, int term) {
        this.bookId = bookId;
        this.studentId = studentId;
        this.isAppointed = isAppointed;
        this.isArrived = false;
        this.term = term;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean isAppointed() {
        return isAppointed;
    }

    public void setAppointed(boolean appointed) {
        isAppointed = appointed;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public boolean isArrived() {
        return isArrived;
    }

    public void setArrived(boolean arrived) {
        isArrived = arrived;
    }
}
