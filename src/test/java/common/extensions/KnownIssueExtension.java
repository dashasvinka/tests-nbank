package common.extensions;

import common.annotations.KnownIssue;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.util.Arrays;
import java.util.List;

public class KnownIssueExtension implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        KnownIssue knownIssue = context.getRequiredTestMethod().getAnnotation(KnownIssue.class);

        if (knownIssue == null) {
            throw throwable;
        }

        String displayName = context.getDisplayName();
        List<String> onlyFor = Arrays.asList(knownIssue.onlyForArgs());

        boolean match = onlyFor.isEmpty() ||
                onlyFor.stream().anyMatch(displayName::contains);

        if (match) {
            System.err.printf(
                    "%n[KNOWN ISSUE] %s — %s%nReason: %s%nDisplayName: %s%n%n",
                    knownIssue.ticket(),
                    knownIssue.description(),
                    throwable.getMessage(),
                    displayName
            );

            if (!knownIssue.ignoreFailure()) {
                throw throwable; // если явно сказано не игнорировать — падаем
            }
            return;
        }
        throw throwable;
    }
}