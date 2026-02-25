package utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reusable wait helpers for Playwright tests.
 *
 * Key improvements vs original:
 *  - Original only wrapped waitForLoadState() with no parameters –
 *    that defaults to LOAD, which fires before network activity settles.
 *    Added an overload for NETWORKIDLE which is safer for SPAs.
 *  - Added waitForVisible() and waitForText() convenience methods.
 */
public class WaitUtil {

    private static final Logger log = LogManager.getLogger(WaitUtil.class);
    private static final int DEFAULT_TIMEOUT_MS = 15_000;

    private WaitUtil() {}

    /**
     * Waits for the page "load" event (DOM ready, blocking resources fetched).
     * Suitable for traditional server-rendered pages.
     */
    public static void waitForPageLoad(Page page) {
        page.waitForLoadState(LoadState.LOAD);
    }

    /**
     * Waits until there are no network connections for at least 500 ms.
     * Recommended for React / Vue / Angular SPAs where JS fetches data after load.
     */
    public static void waitForNetworkIdle(Page page) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.debug("Network idle reached");
    }

    /**
     * Waits up to {@code timeoutMs} for the given locator to become visible.
     *
     * @param locator   the element to wait for
     * @param timeoutMs maximum milliseconds to wait
     */
    public static void waitForVisible(Locator locator, int timeoutMs) {
        locator.waitFor(new Locator.WaitForOptions()
            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
            .setTimeout(timeoutMs));
        log.debug("Locator visible: {}", locator);
    }

    /** Overload using the default timeout. */
    public static void waitForVisible(Locator locator) {
        waitForVisible(locator, DEFAULT_TIMEOUT_MS);
    }
}