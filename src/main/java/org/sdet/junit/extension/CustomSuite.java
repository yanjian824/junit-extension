package org.sdet.junit.extension;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;
import org.sdet.junit.extension.annotation.AuthorFilter;
import org.sdet.junit.extension.annotation.PriorityFilter;
import org.sdet.junit.extension.report.CustomRunListener;

public class CustomSuite extends Suite {
	
    public CustomSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        try {
            filter(new CustomFilter(klass.getAnnotation(AuthorFilter.class), klass.getAnnotation(PriorityFilter.class)));
        } catch (NoTestsRemainException e) {
            ;
        }
    }

    @Override
    public void run(RunNotifier notifier) {
    	Description description = getDescription();
    	
        notifier.addListener(new CustomRunListener());
        notifier.fireTestRunStarted(description);

        EachTestNotifier testNotifier = new EachTestNotifier(notifier, description);
        Statement statement = null;
        try {
            statement = classBlock(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.fireTestIgnored();
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }
    }

}

