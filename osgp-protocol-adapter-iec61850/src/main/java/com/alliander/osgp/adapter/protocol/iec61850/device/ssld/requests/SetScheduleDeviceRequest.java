/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests;

import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceRequest;
import com.alliander.osgp.dto.valueobjects.RelayTypeDto;
import com.alliander.osgp.dto.valueobjects.ScheduleMessageDataContainerDto;

public class SetScheduleDeviceRequest extends DeviceRequest {

    private ScheduleMessageDataContainerDto scheduleMessageDataContainer;
    private RelayTypeDto relayType;

    public SetScheduleDeviceRequest(final Builder deviceRequest,
            final ScheduleMessageDataContainerDto scheduleMessageDataContainer, final RelayTypeDto relayType) {
        super(deviceRequest);
        this.scheduleMessageDataContainer = scheduleMessageDataContainer;
        this.relayType = relayType;
    }

    public RelayTypeDto getRelayType() {
        return this.relayType;
    }

    public ScheduleMessageDataContainerDto getScheduleMessageDataContainer() {
        return this.scheduleMessageDataContainer;
    }
}
