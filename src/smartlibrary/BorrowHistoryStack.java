package smartlibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack that stores borrowing history for one student.
 * New loan records are pushed to the top, so history is displayed with the most
 * recent activity first.
 */
public class BorrowHistoryStack {
    private Node top;

    /**
     * One linked-list node used by the stack.
     */
    private static class Node {
        private final LoanRecord loanRecord;
        private final Node next;

        private Node(LoanRecord loanRecord, Node next) {
            this.loanRecord = loanRecord;
            this.next = next;
        }
    }

    /**
     * Adds a new loan record to the top of the stack.
     */
    public void push(LoanRecord loanRecord) {
        top = new Node(loanRecord, top);
    }

    /**
     * Checks whether the student has any stored loan records.
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Finds the student's active, unreturned loan for the requested ISBN.
     */
    public LoanRecord findActiveLoanByIsbn(long isbn) {
        Node current = top;

        while (current != null) {
            LoanRecord loanRecord = current.loanRecord;
            if (!loanRecord.isReturned() && loanRecord.getBook().getIsbn() == isbn) {
                return loanRecord;
            }
            current = current.next;
        }

        return null;
    }

    /**
     * Converts the stack to a list while keeping the newest record first.
     */
    public List<LoanRecord> toList() {
        List<LoanRecord> loanRecords = new ArrayList<>();
        Node current = top;

        while (current != null) {
            loanRecords.add(current.loanRecord);
            current = current.next;
        }

        return loanRecords;
    }
}
