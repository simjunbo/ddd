package book.chapter4;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EmailSet {
    private Set<Email> emails = new HashSet<>();

    private EmailSet() {
    }

    public EmailSet(Set<Email> emails) {
        this.emails.addAll(emails);
    }

    public Set<Email> getEmails() {
        return Collections.unmodifiableSet(emails);
    }
}
