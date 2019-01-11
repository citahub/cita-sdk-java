package com.cryptape.cita.infura;

import java.util.Collections;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InfuraHttpServiceTest {

    @Test
    public void testBuildHeader() {
        assertTrue(InfuraHttpService.buildClientVersionHeader("", false).isEmpty());
        assertTrue(InfuraHttpService.buildClientVersionHeader(null, false).isEmpty());

        assertThat(InfuraHttpService.buildClientVersionHeader("geth 1.4.19", true),
                equalTo(Collections.singletonMap(
                        "Infura-Ethereum-Preferred-Client",
                        "geth 1.4.19")));

        assertThat(InfuraHttpService.buildClientVersionHeader("geth 1.4.19", false),
                is(Collections.singletonMap(
                        "Infura-Ethereum-Preferred-Client",
                        "geth 1.4.19; required=false")));
    }
}
