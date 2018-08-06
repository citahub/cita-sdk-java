package org.nervos.appchain.utils;

import java.io.IOException;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.nervos.appchain.utils.Version.DEFAULT;
import static org.nervos.appchain.utils.Version.getTimestamp;
import static org.nervos.appchain.utils.Version.getVersion;

public class VersionTest {

    @Test
    public void testGetVersion() throws IOException {
        assertThat(getVersion(), is(DEFAULT));
    }

    @Test
    public void testGetTimestamp() throws IOException {
        assertThat(getTimestamp(), is("2017-01-31 01:21:09.843 UTC"));
    }
}
