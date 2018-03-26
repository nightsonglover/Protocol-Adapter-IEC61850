/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.device;

public class DeviceRequest {

    private final String organisationIdentification;
    private final String deviceIdentification;
    private final String correlationUid;
    private final String domain;
    private final String domainVersion;
    private final String messageType;
    private final String ipAddress;
    private final int retryCount;
    private final boolean isScheduled;

    public DeviceRequest(final Builder builder) {
        this.organisationIdentification = builder.organisationIdentification;
        this.deviceIdentification = builder.deviceIdentification;
        this.correlationUid = builder.correlationUid;
        this.domain = builder.domain;
        this.domainVersion = builder.domainVersion;
        this.messageType = builder.messageType;
        this.ipAddress = builder.ipAddress;
        this.retryCount = builder.retryCount;
        this.isScheduled = builder.isScheduled;
    }

    public static class Builder {
        private String organisationIdentification = null;
        private String deviceIdentification = null;
        private String correlationUid = null;
        private String domain = null;
        private String domainVersion = null;
        private String messageType = null;
        private String ipAddress = null;
        private int retryCount = 0;
        private boolean isScheduled = false;

        public Builder withOrganisationIdentification(final String organisationIdentification) {
            this.organisationIdentification = organisationIdentification;
            return this;
        }

        public Builder withDeviceIdentification(final String deviceIdentification) {
            this.deviceIdentification = deviceIdentification;
            return this;
        }

        public Builder withCorrelationUid(final String correlationUid) {
            this.correlationUid = correlationUid;
            return this;
        }

        public Builder withDomain(final String domain) {
            this.domain = domain;
            return this;
        }

        public Builder withDomainVersion(final String domainVersion) {
            this.domainVersion = domainVersion;
            return this;
        }

        public Builder withMessageType(final String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder withIpAddress(final String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder withRetryCount(final int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder withIsScheduled(final boolean isScheduled) {
            this.isScheduled = isScheduled;
            return this;
        }

        public DeviceRequest build() {
            return new DeviceRequest(this);
        }

    }

    public static Builder newDeviceRequestBuilder() {
        return new Builder();
    }

    public String getOrganisationIdentification() {
        return this.organisationIdentification;
    }

    public String getDeviceIdentification() {
        return this.deviceIdentification;
    }

    public String getCorrelationUid() {
        return this.correlationUid;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getDomainVersion() {
        return this.domainVersion;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public boolean isScheduled() {
        return this.isScheduled;
    }
}
