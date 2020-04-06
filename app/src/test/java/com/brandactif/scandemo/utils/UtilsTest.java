package com.brandactif.scandemo.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class UtilsTest {

    @Test
    public void testGetIso8601Date() {
        String actual = Utils.getIso8601Date();
        assertThat(actual, instanceOf(String.class));
    }

}
