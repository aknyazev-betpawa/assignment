package config;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class Log4jTestWatcher extends TestWatcher {

    private final Logger logger;

    public Log4jTestWatcher() {
        logger = LogManager.getLogger("Log4jTestWatcher");
    }

    public Log4jTestWatcher(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void failed(Throwable e, org.junit.runner.Description description) {
        logger.error(String.format("Failed: %s", description), e.getCause());
    }


    @Override
    protected void succeeded(Description description) {
        logger.info(String.format("Success: %s", description));
    }
}
