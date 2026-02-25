package ui.core;

import com.microsoft.playwright.Page;

/**
 * Thin convenience wrapper around PlaywrightFactory.
 *
 * Keeping DriverManager separate from PlaywrightFactory follows the
 * Single-Responsibility Principle: PlaywrightFactory owns lifecycle,
 * DriverManager exposes access.  Test classes and page-objects depend on
 * DriverManager, so swapping the underlying factory never forces changes
 * throughout the codebase.
 */
public class DriverManager {

    // Static-only class – no instantiation
    private DriverManager() {}

    /**
     * Returns the {@link Page} for the current thread.
     * Delegates to PlaywrightFactory which validates the page is non-null.
     */
    public static Page getPage() {
        return PlaywrightFactory.getPage();
    }
}