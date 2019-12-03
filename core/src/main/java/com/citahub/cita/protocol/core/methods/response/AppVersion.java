package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * Created by duanyytop on 2019-04-09.
 * Copyright © 2018 Cryptape. All rights reserved.
 */
public class AppVersion extends Response<AppVersion.Version> {

    public Version getVersion() {
        return getResult();
    }

    public static class Version {
        public String softwareVersion;
    }

}
