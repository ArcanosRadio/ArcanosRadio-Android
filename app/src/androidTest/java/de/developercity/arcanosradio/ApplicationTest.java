package de.developercity.arcanosradio;

import android.app.Application;
import android.test.ApplicationTestCase;

import org.junit.ClassRule;
import org.junit.Test;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    @ClassRule public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void takeScreenshots() {
        Screengrab.screenshot("name_of_screenshot_here");
    }
}