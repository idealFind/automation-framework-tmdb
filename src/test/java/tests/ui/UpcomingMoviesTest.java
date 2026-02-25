package tests.ui;

import java.util.Map;

import base.BaseUITest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ui.pages.MovieDetailsPage;
import ui.pages.UpcomingMoviesPage;

/**
 * Tests for the TMDB Upcoming Movies listing and navigation to detail pages.
 *
 * Key improvements vs original: - Removed @BeforeMethod + @BeforeSuite
 * + @BeforeTest imports – none were used. - UpcomingMoviesPage is now created
 * AFTER super.setup() via @BeforeClass with dependsOnMethods – previously both
 * ran at @BeforeClass level with no guaranteed order, meaning the page might
 * not be ready. - movieDetailsPage field removed; the page object is now
 * returned from clickMovie() and used directly in the test – cleaner state
 * management. - Assertions now check the real detail-page title via the getter.
 * - Removed dead commented-out code.
 */
public class UpcomingMoviesTest extends BaseUITest {

	private static final Logger log = LogManager.getLogger(UpcomingMoviesTest.class);

	private UpcomingMoviesPage upcomingPage;
	private MovieDetailsPage detailsPage;

	/**
	 * dependsOnMethods = "setup" guarantees BaseUITest.setup() completes first, so
	 * the Page is ready when we construct UpcomingMoviesPage.
	 */
	@BeforeClass(dependsOnMethods = "setup")
	public void initPages() {
		log.info("Initialising page objects for UpcomingMoviesTest");
		upcomingPage = new UpcomingMoviesPage(getPage());
		detailsPage = new MovieDetailsPage(getPage());
	}

	@Test(priority = 0, description = "Verify the upcoming movies list is populated")
	public void fetchUpcomingMovies() {
		log.info("Running fetchUpcomingMovies");

		Map<String, String> movies = upcomingPage.getUpcomingMovies();

		// Assert the list is not empty – a silent pass with an empty map hides real
		// failures
		Assert.assertFalse(movies.isEmpty(), "Upcoming movies list should not be empty");

		movies.forEach((k, v) -> log.info("{} -> {}", k, v));
		log.info("Total upcoming movies found: {}", movies.size());
	}

	@Test(priority = 1, description = "Verify clicking a movie navigates to the correct detail page", dependsOnMethods = "fetchUpcomingMovies")
	public void goToMovieDetails() {
		log.info("Running goToMovieDetails");

		// Click the 3rd movie (index 2); clickMovie() returns the details page object
		detailsPage = upcomingPage.clickMovie(2);

		String expectedTitle = upcomingPage.getLastClickedMovieName();
		String actualTitle = detailsPage.getMovieTitle();

		log.info("Expected title='{}', Actual title='{}'", expectedTitle, actualTitle);
		detailsPage.logAllDetails();

		Assert.assertEquals(actualTitle, expectedTitle,
				"Movie title on details page should match the title clicked in the upcoming list");
	}
}