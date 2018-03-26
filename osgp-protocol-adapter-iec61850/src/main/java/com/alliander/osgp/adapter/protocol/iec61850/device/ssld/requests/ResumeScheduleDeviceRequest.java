/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests;

import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceRequest;
import com.alliander.osgp.dto.valueobjects.ResumeScheduleMessageDataContainerDto;

public class ResumeScheduleDeviceRequest extends DeviceRequest {

    private ResumeScheduleMessageDataContainerDto resumeScheduleContainer;

    public ResumeScheduleDeviceRequest(final Builder deviceRequest,
            final ResumeScheduleMessageDataContainerDto resumeScheduleContainer) {
        super(deviceRequest);
        this.resumeScheduleContainer = resumeScheduleContainer;
    }

    public ResumeScheduleMessageDataContainerDto getResumeScheduleContainer() {
        return this.resumeScheduleContainer;
    }
}
