package ui.core;

import com.microsoft.playwright.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thread-safe factory for managing Playwright browser lifecycle.
 * Uses ThreadLocal so parallel test runs each get their own isolated instance.
 */
public class PlaywrightFactory {

    private static final Logger log = LogManager.getLogger(PlaywrightFactory.class);

    // ThreadLocal ensures each thread (parallel test) has its own browser stack
    private static final ThreadLocal<Playwright>       playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser>          browser    = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext>   context    = new ThreadLocal<>();
    private static final ThreadLocal<Page>             page       = new ThreadLocal<>();

    // Private constructor – this is a static utility class, not meant to be instantiated
    private PlaywrightFactory() {}

    /**
     * Initialises the full Playwright → Browser → Context → Page chain.
     *
     * @param browserName  "chromium" | "firefox" | "webkit"  (case-insensitive)
     * @param headless     run without a visible window when true
     */
    public static void initBrowser(String browserName, boolean headless) {
        log.info("Initialising '{}' browser (headless={})", browserName, headless);

        playwright.set(Playwright.create());

        // Switch expression – clear, exhaustive, no fall-through risk
        BrowserType browserType = switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.get().firefox();
            case "webkit"  -> playwright.get().webkit();
            default        -> {
                log.warn("Unknown browser '{}' – defaulting to chromium", browserName);
                yield playwright.get().chromium();
            }
        };

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(headless ? 0 : 50); // slight slowdown in headed mode aids debugging

        browser.set(browserType.launch(launchOptions));

        // Full HD viewport; also enable video/trace recording hooks if needed later
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setIgnoreHTTPSErrors(true);   // handy for self-signed certs in CI

        context.set(browser.get().newContext(contextOptions));

        // Start tracing so failures can be investigated with Playwright's trace viewer
        context.get().tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));

        page.set(context.get().newPage());
        log.info("Browser ready – page created");
    }

    /** Returns the Page for the current thread. */
    public static Page getPage() {
        Page p = page.get();
        if (p == null) {
            throw new IllegalStateException(
                "Page is null – did you call PlaywrightFactory.initBrowser() before getPage()?");
        }
        return p;
    }

    /**
     * Closes resources in the correct reverse-creation order:
     * Page → Context (+ trace) → Browser → Playwright
     *
     * NOTE: Playwright must be closed LAST; closing it before the browser
     * causes a native crash on some platforms.
     */
    public static void tearDown() {
        tearDown(null); // no trace output path by default
    }

    /**
     * Overload that also saves the Playwright trace zip on failure.
     *
     * @param tracePath  path to write the .zip, or null to discard
     */
    public static void tearDown(String tracePath) {
        log.info("Tearing down Playwright resources (trace={})", tracePath);

        // Page doesn't need explicit close – it's closed with its context
        try {
            if (context.get() != null) {
                if (tracePath != null) {
                    context.get().tracing().stop(
                        new Tracing.StopOptions().setPath(java.nio.file.Paths.get(tracePath)));
                } else {
                    context.get().tracing().stop();
                }
                context.get().close();
            }
        } catch (Exception e) {
            log.error("Error closing BrowserContext", e);
        } finally {
            context.remove();
            page.remove();
        }

        try {
            if (browser.get() != null) browser.get().close();
        } catch (Exception e) {
            log.error("Error closing Browser", e);
        } finally {
            browser.remove();
        }

        try {
            if (playwright.get() != null) playwright.get().close();
        } catch (Exception e) {
            log.error("Error closing Playwright", e);
        } finally {
            playwright.remove();
        }

        log.info("Playwright teardown complete");
    }
}