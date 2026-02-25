package utils;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;

/**
 * Captures a Playwright screenshot and attaches it to the Allure report.
 *
 * Key improvements vs original:
 *  - Added correct MIME type ("image/png") to the Allure attachment so the report
 *    renders the image inline instead of offering it as a binary download.
 *  - Overloaded to accept an optional label for the attachment name.
 *  - Replaced swallowed exceptions with explicit logging.
 */
public class ScreenshotUtil {

    private static final Logger log = LogManager.getLogger(ScreenshotUtil.class);

    private ScreenshotUtil() {}

    /** Captures and attaches with a generic label. */
    public static void capture(Page page) {
        capture(page, "Failure Screenshot");
    }

    /**
     * Captures a screenshot and attaches it to the current Allure test step.
     *
     * @param page  the Playwright Page (must be non-null and not closed)
     * @param label attachment name shown in the Allure report
     */
    public static void capture(Page page, String label) {
        if (page == null) {
            log.warn("ScreenshotUtil.capture called with null page – skipping");
            return;
        }
        try {
            byte[] screenshot = page.screenshot(
                new Page.ScreenshotOptions().setFullPage(true));   // full-page = more context

            // FIX: original missing MIME type caused Allure to show a download link, not image
            Allure.addAttachment(label, "image/png", new ByteArrayInputStream(screenshot), "png");
            log.info("Screenshot attached to Allure report: '{}'", label);
        } catch (Exception e) {
            log.error("Failed to capture screenshot for '{}': {}", label, e.getMessage(), e);
        }
    }
}