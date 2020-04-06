package com.brandactif.scandemo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    private Context context;

    @Rule
    public ActivityTestRule<MainActivity> activityRule
            = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
        // Context of the app under test.
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void changeTextOnClickLeftButton() {
        // Press the left button
        onView(withId(R.id.btnLeft))
                .perform(click());
        // Check that the text was changed
        onView(withId(R.id.btnLeft))
                .check(matches(withText(R.string.switch_to_scanner)));
        // Press the left button
        onView(withId(R.id.btnLeft))
                .perform(click());
        // Check that the text was changed back
        onView(withId(R.id.btnLeft))
                .check(matches(withText(R.string.switch_to_tv)));
    }

    @Test
    public void changeTextOnClickRightButton() {
        // Press the right button
        onView(withId(R.id.btnRight))
                .perform(click());
        // Check that the text was changed
        onView(withId(R.id.btnRight))
                .check(matches(withText(R.string.switch_to_scanner)));
        // Press the right button again
        onView(withId(R.id.btnRight))
                .perform(click());
        // Check that the text was changed back
        onView(withId(R.id.btnRight))
                .check(matches(withText(R.string.switch_to_radio)));
    }

}