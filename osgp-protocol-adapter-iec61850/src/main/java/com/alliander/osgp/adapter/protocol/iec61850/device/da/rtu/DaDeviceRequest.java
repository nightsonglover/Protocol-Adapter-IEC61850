/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.device.da.rtu;

import java.io.Serializable;

import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceRequest;

public class DaDeviceRequest extends DeviceRequest {

    private Serializable request;

    public DaDeviceRequest(final Builder deviceRequest, final Serializable request) {
        super(deviceRequest);
        this.request = request;
    }

    public Serializable getRequest() {
        return this.request;
    }
}
