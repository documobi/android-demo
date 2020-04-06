package com.brandactif.scandemo.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.brandactif.scandemo.model.MetaData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class UtilsTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        // Context of the app under test.
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getMetaData() {
        MetaData actual = Utils.getMetaData(context);
        assertThat(actual, instanceOf(MetaData.class));
    }
}