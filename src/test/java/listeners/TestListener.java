package listeners;

import base.BaseUITest;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ScreenshotUtil;

/**
 * TestNG listener that captures a screenshot on test failure and attaches it
 * to the Allure report.
 *
 * Key improvements vs original:
 *  - Safer cast: checks instanceof before casting to avoid ClassCastException
 *    if the listener is ever registered on a non-UI test class.
 *  - Logs pass/skip/fail events for better traceability in CI logs.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ START  : {}", formatName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✔ PASSED : {}", formatName(result));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⏭ SKIPPED: {}", formatName(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("✘ FAILED : {}", formatName(result));

        // FIX: guard against non-UI tests being covered by this listener
        Object instance = result.getInstance();
        if (instance instanceof BaseUITest uiTest) {
            Page page = uiTest.getPage();
            if (page != null) {
                ScreenshotUtil.capture(page, formatName(result));
            }
        } else {
            log.warn("TestListener.onTestFailure – test instance is not a BaseUITest, skipping screenshot");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static String formatName(ITestResult r) {
        return r.getTestClass().getRealClass().getSimpleName() + "#" + r.getMethod().getMethodName();
    }
}