package ui.pages;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Page Object for a TMDB Movie Details page.
 *
 * Key improvements vs original:
 *  - movieDetailsPage() printed data but returned void; tests couldn't assert on it.
 *    Replaced with individual getters so each field can be independently asserted.
 *  - public mutable moviename_DetailsPage field replaced with a private field + getter.
 *  - Stale commented-out code removed (was dead weight increasing cognitive load).
 *  - Replaced System.out with Log4j.
 *  - Locators resolved lazily (when first accessed) rather than all up-front in the
 *    constructor – the page may still be loading when the constructor runs.
 */
public class MovieDetailsPage {

    private static final Logger log = LogManager.getLogger(MovieDetailsPage.class);

    private static final String MOVIE_TITLE_XPATH   = "//div[@class='single_column']//h2/a";
    private static final String LANGUAGE_CSS        = "p:has(bdi:text('Original Language'))";
    private static final String OVERVIEW_XPATH      = "//*[@class='overview']/p";
    private static final String RELEASE_DATE_XPATH  = "//*[@class='release']";
    private static final String GENRES_XPATH        = "//span[@class='genres']/a";

    private final Page page;

    // FIX: was public mutable String – now private with a getter
    private String movieTitle = "";

    public MovieDetailsPage(Page page) {
        this.page = page;
        log.info("MovieDetailsPage created");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public accessors – each one resolves its locator fresh to avoid staleness
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns the movie title shown in the hero header.
     * Result is cached after the first call; call {@link #refresh()} if the page changes.
     */
    public String getMovieTitle() {
        if (movieTitle.isEmpty()) {
            movieTitle = page.locator(MOVIE_TITLE_XPATH).innerText().trim();
            log.info("Movie title on details page: '{}'", movieTitle);
        }
        return movieTitle;
    }

    /** Returns the original language value (e.g. "English"). */
    public String getOriginalLanguage() {
        Locator languageBlock = page.locator(LANGUAGE_CSS);
        // Strip the label text; textContent() includes all descendant text
        String raw = languageBlock.textContent().replace("Original Language", "").trim();
        log.debug("Original language: {}", raw);
        return raw;
    }

    /** Returns the plot overview text. */
    public String getOverview() {
        String text = page.locator(OVERVIEW_XPATH).innerText().trim();
        log.debug("Overview (truncated): {}", text.length() > 80 ? text.substring(0, 80) + "…" : text);
        return text;
    }

    /** Returns the release date string as displayed on the page. */
    public String getReleaseDate() {
        String date = page.locator(RELEASE_DATE_XPATH).innerText().trim();
        log.debug("Release date: {}", date);
        return date;
    }

    /** Returns the list of genre names (may be empty, never null). */
    public List<String> getGenres() {
        List<String> genres = page.locator(GENRES_XPATH).allInnerTexts();
        log.debug("Genres ({}): {}", genres.size(), genres);
        return genres;
    }

    /**
     * Convenience: logs all details page fields at INFO level.
     * Useful for exploratory runs; in production prefer individual getters for assertions.
     */
    public void logAllDetails() {
        log.info("=== Movie Details ===");
        log.info("Title    : {}", getMovieTitle());
        log.info("Language : {}", getOriginalLanguage());
        log.info("Released : {}", getReleaseDate());
        log.info("Genres   : {}", getGenres());
        log.info("Overview : {}", getOverview());
    }

    /** Clears cached values – call if the page navigates or re-renders. */
    public void refresh() {
        movieTitle = "";
    }
}