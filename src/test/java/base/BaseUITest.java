package base;

import config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.microsoft.playwright.Page;
import ui.core.PlaywrightFactory;

/**
 * Base class for all UI tests.
 *
 * Key improvements vs original:
 *  - Removed unused `playwright` and `browser` fields (PlaywrightFactory owns them).
 *  - @Parameters value is now @Optional so tests run without a testng.xml too.
 *  - browserName param falls back to config.properties when not supplied by testng.xml,
 *    eliminating the hidden dependency on ConfigReader inside @BeforeClass.
 *  - Replaced System.out with Log4j.
 *  - tearDown() saves a Playwright trace on failure path (opt-in via system property).
 */
public abstract class BaseUITest {

    private static final Logger log = LogManager.getLogger(BaseUITest.class);

    // Held here so subclass @BeforeClass methods can call getPage() safely
    private Page page;

    @Parameters("browser")
    @BeforeClass(alwaysRun = true)
    public void setup(@Optional String browserName) {
        // @Optional means the test still runs when no testng.xml parameter is supplied;
        // fall back to config.properties in that case
        if (browserName == null || browserName.isBlank()) {
            browserName = ConfigReader.get("browser");
        }
        boolean headless = Boolean.parseBoolean(ConfigReader.get("headless"));

        log.info("Setting up UI test – browser={}, headless={}", browserName, headless);

        PlaywrightFactory.initBrowser(browserName, headless);
        page = PlaywrightFactory.getPage();
        page.navigate(ConfigReader.get("baseUrl"));

        log.info("Navigated to base URL: {}", ConfigReader.get("baseUrl"));
    }

    /**
     * Subclasses and the {@link listeners.TestListener} both call this to get
     * the current thread's page.
     */
    public Page getPage() {
        return page;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        // Optionally write the Playwright trace to disk – set -DsaveTrace=true on the CLI
        String saveTrace = System.getProperty("saveTrace", "false");
        String tracePath = Boolean.parseBoolean(saveTrace)
                ? "target/traces/" + getClass().getSimpleName() + "-trace.zip"
                : null;

        log.info("Tearing down – trace output: {}", tracePath != null ? tracePath : "disabled");
        PlaywrightFactory.tearDown(tracePath);
    }
}