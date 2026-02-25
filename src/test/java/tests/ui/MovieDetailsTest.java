package tests.ui;

import base.BaseUITest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ui.pages.MovieDetailsPage;
import ui.pages.UpcomingMoviesPage;

/**
 * Tests focused on the Movie Details page content.
 *
 * Key improvements vs original:
 *  - Was using @BeforeMethod to instantiate pages on every test method – expensive
 *    (re-navigates the full upcoming flow for each test).  Changed to @BeforeClass.
 *  - goToMovieDetails() called movieDetailsPage.movieDetailsPage() which only
 *    printed values and returned void – tests couldn't assert anything.
 *    Now each field is individually asserted.
 *  - Removed unused upcomingPage field in test; navigation logic stays in @BeforeClass.
 */
public class MovieDetailsTest extends BaseUITest {

    private static final Logger log = LogManager.getLogger(MovieDetailsTest.class);

    private MovieDetailsPage detailsPage;
    private String           expectedTitle;

    @BeforeClass(dependsOnMethods = "setup")
    public void initPages() {
        log.info("Navigating to a movie detail page for MovieDetailsTest");

        // Navigate through the upcoming list to land on the first movie's detail page
        UpcomingMoviesPage upcomingPage = new UpcomingMoviesPage(getPage());
        detailsPage   = upcomingPage.clickMovie(0);
        expectedTitle = upcomingPage.getLastClickedMovieName();

        log.info("Landed on detail page for: '{}'", expectedTitle);
    }

    @Test(description = "Verify movie title on detail page matches the listing")
    public void verifyMovieTitle() {
        String actualTitle = detailsPage.getMovieTitle();
        log.info("Title check – expected='{}', actual='{}'", expectedTitle, actualTitle);
        Assert.assertEquals(actualTitle, expectedTitle,
            "Title on detail page must match the title shown in the upcoming list");
    }

    @Test(description = "Verify original language is not blank")
    public void verifyOriginalLanguage() {
        String lang = detailsPage.getOriginalLanguage();
        log.info("Original language: '{}'", lang);
        Assert.assertFalse(lang.isBlank(), "Original Language should be present on the details page");
    }

    @Test(description = "Verify overview text is present")
    public void verifyOverview() {
        String overview = detailsPage.getOverview();
        Assert.assertFalse(overview.isBlank(), "Overview should not be empty");
    }

    @Test(description = "Verify release date is present")
    public void verifyReleaseDate() {
        String date = detailsPage.getReleaseDate();
        Assert.assertFalse(date.isBlank(), "Release date should be displayed");
    }

    @Test(description = "Verify at least one genre is listed")
    public void verifyGenres() {
        Assert.assertFalse(detailsPage.getGenres().isEmpty(),
            "At least one genre should be listed on the details page");
    }
}