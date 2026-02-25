package ui.pages;

import java.util.LinkedHashMap;
import java.util.Map;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Page Object for the TMDB Upcoming Movies listing page.
 *
 * Key fixes vs original:
 *  - movieTitles was public static – exposed internal state to tests and
 *    broke thread-safety.  Changed to private instance field.
 *  - moviename was a public mutable field used across classes (tight coupling).
 *    Now private with a getter so tests read a proper value.
 *  - Navigation + locator init separated from constructor via a dedicated
 *    navigate() helper (constructor should not perform I/O-heavy work).
 *  - Replaced System.out with Log4j.
 */
public class UpcomingMoviesPage {

    private static final Logger log = LogManager.getLogger(UpcomingMoviesPage.class);

    // XPath selectors kept as constants – easy to update and self-documenting
    private static final String UPCOMING_LINK  = "//a[@aria-label='Upcoming']";
    private static final String MOVIE_ID_XPATH = "//*[@id='media_results']//div[@data-id]";
    private static final String TITLE_XPATH    = "//*[@id='media_results']//h2";
    private static final String DATE_XPATH     = "//*[@id='media_results']//p";
    private static final String MOVIE_LINK_TPL = "//*[@id='media_results']//h2/a[normalize-space()='%s']";

    private final Page page;

    // FIX: was public static – that shares state across all test instances/threads
    private Locator movieTitles;
    private Locator movieDates;
    private Locator movieID;

    // FIX: was public mutable String – tests read it directly, creating coupling
    private String lastClickedMovieName = "";

    public UpcomingMoviesPage(Page page) {
        this.page = page;
        navigate();
    }

    /**
     * Navigates from the home page to the Upcoming Movies list.
     * Waits for at least one title to appear before returning.
     */
    private void navigate() {
        log.info("Navigating to Upcoming Movies page");
        page.getByLabel("Movies").click();
        page.locator(UPCOMING_LINK).click();

        // Initialise locators AFTER navigation so they resolve against the correct DOM
        movieID     = page.locator(MOVIE_ID_XPATH);
        movieTitles = page.locator(TITLE_XPATH);
        movieDates  = page.locator(DATE_XPATH);

        // Explicit wait – waits up to 10 s for the first title (replaces implicit polling)
        movieTitles.first().waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        log.info("Upcoming movies page loaded – {} titles visible", movieTitles.count());
    }

    /**
     * Returns a map of (id + " - " + title) → release-date for every movie on the page.
     */
    public Map<String, String> getUpcomingMovies() {
        int count = movieTitles.count();
        log.info("Fetching {} upcoming movies", count);

        Map<String, String> movieMap = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            String id   = movieID.nth(i).getAttribute("data-id");
            String name = movieTitles.nth(i).innerText();
            String date = movieDates.nth(i).innerText();
            movieMap.put(id + " - " + name, date);
        }

        log.debug("Movie map: {}", movieMap);
        return movieMap;
    }

    /**
     * Clicks the movie at position {@code index} (0-based) in the upcoming list
     * and returns a {@link MovieDetailsPage} for the opened detail view.
     *
     * @param index 0-based position in the list
     */
    public MovieDetailsPage clickMovie(int index) {
        lastClickedMovieName = movieTitles.nth(index).innerText().trim();
        log.info("Clicking movie [{}]: '{}'", index, lastClickedMovieName);

        // Build a tight locator using the actual title text; normalize-space handles whitespace
        String selector = String.format(MOVIE_LINK_TPL, lastClickedMovieName);
        page.locator(selector).click();

        log.info("Navigated to details page for '{}'", lastClickedMovieName);
        return new MovieDetailsPage(page);
    }

    /** The title of the last movie that was clicked via {@link #clickMovie(int)}. */
    public String getLastClickedMovieName() {
        return lastClickedMovieName;
    }
}