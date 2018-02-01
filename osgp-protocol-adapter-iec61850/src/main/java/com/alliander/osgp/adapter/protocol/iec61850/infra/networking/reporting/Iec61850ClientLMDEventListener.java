/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.networking.reporting;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openmuc.openiec61850.BdaBoolean;
import org.openmuc.openiec61850.BdaReasonForInclusion;
import org.openmuc.openiec61850.DataSet;
import org.openmuc.openiec61850.FcModelNode;
import org.openmuc.openiec61850.HexConverter;
import org.openmuc.openiec61850.ObjectReference;
import org.openmuc.openiec61850.Report;

import com.alliander.osgp.adapter.protocol.iec61850.application.services.DeviceManagementService;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.ProtocolAdapterException;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.LogicalNode;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.SubDataAttribute;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.Iec61850BdaOptFldsHelper;
import com.alliander.osgp.core.db.api.iec61850.entities.LightMeasurementDevice;
import com.alliander.osgp.dto.valueobjects.EventNotificationDto;
import com.alliander.osgp.dto.valueobjects.EventTypeDto;

public class Iec61850ClientLMDEventListener extends Iec61850ClientBaseEventListener {

    public Iec61850ClientLMDEventListener(final String deviceIdentification,
            final DeviceManagementService deviceManagementService) throws ProtocolAdapterException {
        super(deviceIdentification, deviceManagementService, Iec61850ClientLMDEventListener.class);
    }

    @Override
    public void newReport(final Report report) {

        final DateTime timeOfEntry = this.getTimeOfEntry(report);

        final String reportDescription = this.getReportDescription(report, timeOfEntry);

        this.logger.info("newReport for {}", reportDescription);
        boolean skipRecordBecauseOfOldSqNum = false;
        if (report.isBufOvfl()) {
            this.logger.warn("Buffer Overflow reported for {} - entries within the buffer may have been lost.",
                    reportDescription);
        }
        if (this.firstNewSqNum != null && report.getSqNum() != null && report.getSqNum() < this.firstNewSqNum) {
            skipRecordBecauseOfOldSqNum = true;
            this.logger.warn("Unused boolean skipRecordBecauseOfOldSqNum is set to {}!", skipRecordBecauseOfOldSqNum);
        }
        this.logReportDetails(report);

        final DataSet dataSet = report.getDataSet();
        if (dataSet == null) {
            this.logger.warn("No DataSet available for {}", reportDescription);
            return;
        }

        final Map<LightMeasurementDevice, FcModelNode> reportMemberPerDevice = this
                .processReportedDataForLightMeasurementDevices(dataSet.getMembers());

        for (final LightMeasurementDevice lmd : reportMemberPerDevice.keySet()) {
            this.logger.debug("Add event notification for lmd {}", lmd);
            final String deviceIdentification = lmd.getDeviceIdentification();
            final Short index = lmd.getDigitalInput();
            final FcModelNode member = reportMemberPerDevice.get(lmd);
            final EventNotificationDto eventNotification = this.getEventNotificationForReportedData(member, timeOfEntry,
                    reportDescription, deviceIdentification, index.intValue());

            try {
                this.deviceManagementService.addEventNotifications(deviceIdentification,
                        Arrays.asList(eventNotification));
            } catch (final ProtocolAdapterException pae) {
                this.logger.error("Error adding device notifications for device: " + deviceIdentification, pae);
            }
        }

    }

    private Map<LightMeasurementDevice, FcModelNode> processReportedDataForLightMeasurementDevices(
            final List<FcModelNode> dataSetMembers) {
        final Map<LightMeasurementDevice, FcModelNode> result = new HashMap<>();

        this.logger.debug("DataSet members of the received report: {}", dataSetMembers.stream()
                .map(FcModelNode::getReference).map(ObjectReference::toString).collect(Collectors.toList()));

        final List<LightMeasurementDevice> lmds = this.deviceManagementService.findAllLightMeasurementDevices();
        this.logger.debug("Found light measurement devices: {}",
                lmds.stream().map(LightMeasurementDevice::getCode).collect(Collectors.toList()));

        for (final LightMeasurementDevice lmd : lmds) {
            this.logger.debug("Process reported data for lmd {}", lmd);
            final String nodeName = LogicalNode.getSpggioByIndex(lmd.getDigitalInput()).getDescription().concat(".");
            this.logger.debug("Nodename: {}", nodeName);

            for (final FcModelNode member : dataSetMembers) {
                if (member.getReference().toString().contains(nodeName)) {
                    this.logger.debug("Match found: {} contains {}", member.getReference().toString(), nodeName);
                    result.put(lmd, member);
                }
            }
        }

        this.logger.debug("Found matches: {}", result.size());
        return result;
    }

    private DateTime getTimeOfEntry(final Report report) {
        return report.getTimeOfEntry() == null ? DateTime.now(DateTimeZone.UTC)
                : new DateTime(report.getTimeOfEntry().getTimestampValue() + IEC61850_ENTRY_TIME_OFFSET);
    }

    private String getReportDescription(final Report report, final DateTime timeOfEntry) {
        return String.format("reportId: %s, timeOfEntry: %s, sqNum: %s%s%s", report.getRptId(),
                timeOfEntry == null ? "-" : timeOfEntry, report.getSqNum(),
                report.getSubSqNum() == null ? "" : " subSqNum: " + report.getSubSqNum(),
                report.isMoreSegmentsFollow() ? " (more segments follow for this sqNum)" : "");
    }

    private EventNotificationDto getEventNotificationForReportedData(final FcModelNode evnRpn,
            final DateTime timeOfEntry, final String reportDescription, final String deviceIdentification,
            final Integer index) {
        EventTypeDto eventType;
        final boolean lightSensorValue = this.determineLightSensorValue(evnRpn, reportDescription);
        //@formatter:off
        /*
         * 0 -> false -> NIGHT_DAY --> LIGHT_SENSOR_REPORTS_LIGHT
         * 1 -> true  -> DAY_NIGHT --> LIGHT_SENSOR_REPORTS_DARK
         */
        //@formatter:on
        if (lightSensorValue) {
            eventType = EventTypeDto.LIGHT_SENSOR_REPORTS_DARK;
        } else {
            eventType = EventTypeDto.LIGHT_SENSOR_REPORTS_LIGHT;
        }
        return new EventNotificationDto(deviceIdentification, timeOfEntry, eventType, reportDescription, index);
    }

    private boolean determineLightSensorValue(final FcModelNode evnRpn, final String reportDescription) {
        final String dataObjectName = SubDataAttribute.STATE.getDescription();
        final BdaBoolean stVal = (BdaBoolean) evnRpn.getChild(dataObjectName);
        if (stVal == null) {
            throw this.childNodeNotAvailableException(evnRpn, dataObjectName, reportDescription);
        }
        return stVal.getValue();
    }

    private IllegalArgumentException childNodeNotAvailableException(final FcModelNode evnRpn,
            final String childNodeName, final String reportDescription) {
        return new IllegalArgumentException("No '" + childNodeName + "' child in DataSet member "
                + evnRpn.getReference() + " from " + reportDescription);
    }

    private void logReportDetails(final Report report) {
        final StringBuilder sb = new StringBuilder("Report details for device ").append(this.deviceIdentification)
                .append(System.lineSeparator());
        sb.append("\t             RptId:\t").append(report.getRptId()).append(System.lineSeparator());
        sb.append("\t        DataSetRef:\t").append(report.getDataSetRef()).append(System.lineSeparator());
        sb.append("\t           ConfRev:\t").append(report.getConfRev()).append(System.lineSeparator());
        sb.append("\t           BufOvfl:\t").append(report.isBufOvfl()).append(System.lineSeparator());
        sb.append("\t           EntryId:\t").append(report.getEntryId()).append(System.lineSeparator());
        sb.append("\tInclusionBitString:\t").append(Arrays.toString(report.getInclusionBitString()))
                .append(System.lineSeparator());
        sb.append("\tMoreSegmentsFollow:\t").append(report.isMoreSegmentsFollow()).append(System.lineSeparator());
        sb.append("\t             SqNum:\t").append(report.getSqNum()).append(System.lineSeparator());
        sb.append("\t          SubSqNum:\t").append(report.getSubSqNum()).append(System.lineSeparator());
        sb.append("\t       TimeOfEntry:\t").append(report.getTimeOfEntry()).append(System.lineSeparator());
        if (report.getTimeOfEntry() != null) {
            sb.append("\t                   \t(")
                    .append(new DateTime(report.getTimeOfEntry().getTimestampValue() + IEC61850_ENTRY_TIME_OFFSET))
                    .append(')').append(System.lineSeparator());
        }
        final List<BdaReasonForInclusion> reasonCodes = report.getReasonCodes();
        if (reasonCodes != null && !reasonCodes.isEmpty()) {
            sb.append("\t       ReasonCodes:").append(System.lineSeparator());
            for (final BdaReasonForInclusion reasonCode : reasonCodes) {
                sb.append("\t                   \t")
                        .append(reasonCode.getReference() == null ? HexConverter.toHexString(reasonCode.getValue())
                                : reasonCode)
                        .append("\t(").append(new Iec61850BdaReasonForInclusionHelper(reasonCode).getInfo()).append(')')
                        .append(System.lineSeparator());
            }
        }
        sb.append("\t           optFlds:").append(report.getOptFlds()).append("\t(")
                .append(new Iec61850BdaOptFldsHelper(report.getOptFlds()).getInfo()).append(')')
                .append(System.lineSeparator());
        final DataSet dataSet = report.getDataSet();
        if (dataSet == null) {
            sb.append("\t           DataSet:\tnull").append(System.lineSeparator());
        } else {
            this.appendDataSet(dataSet, sb);
        }
        this.logger.info(sb.append(System.lineSeparator()).toString());
    }

    private void appendDataSet(final DataSet dataSet, final StringBuilder sb) {
        sb.append("\t           DataSet:\t").append(dataSet.getReferenceStr()).append(System.lineSeparator());
        final List<FcModelNode> members = dataSet.getMembers();
        if (members != null && !members.isEmpty()) {
            sb.append("\t   DataSet members:\t").append(members.size()).append(System.lineSeparator());
            for (final FcModelNode member : members) {
                sb.append("\t            member:\t").append(member).append(System.lineSeparator());
                if (member.getReference().toString().contains("CSLC.EvnRpn")) {
                    sb.append(this.evnRpnInfo("\t                   \t\t", member));
                }
            }
        }
    }

    private String evnRpnInfo(final String linePrefix, final FcModelNode evnRpn) {
        final String dataObjectName = SubDataAttribute.STATE.getDescription();

        final StringBuilder sb = new StringBuilder();
        final BdaBoolean stValNode = (BdaBoolean) evnRpn.getChild(dataObjectName);
        sb.append(linePrefix).append(dataObjectName).append(": ");
        if (stValNode == null) {
            sb.append("null");
        } else {
            final boolean stVal = stValNode.getValue();
            sb.append(stVal).append(" = ").append(stVal ? "true" : "false");
        }
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmuc.openiec61850.ClientEventListener#associationClosed(java.io
     * .IOException)
     */
    @Override
    public void associationClosed(final IOException e) {
        this.logger.info("associationClosed() for device: {}, {}", this.deviceIdentification,
                e.getMessage() == null ? "no IOException" : "IOException: " + e.getMessage());
    }
}
