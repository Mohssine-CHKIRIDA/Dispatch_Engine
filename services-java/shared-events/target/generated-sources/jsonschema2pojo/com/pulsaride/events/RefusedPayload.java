
package com.pulsaride.events;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


/**
 * RefusedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pro_id",
    "reason",
    "retry_count"
})
@Generated("jsonschema2pojo")
public class RefusedPayload {

    /**
     * ID of the professional who refused (or failed to respond in time).
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    @JsonPropertyDescription("ID of the professional who refused (or failed to respond in time).")
    @NotNull
    private String proId;
    /**
     * Why the proposal didn't result in acceptance.
     * 
     */
    @JsonProperty("reason")
    @JsonPropertyDescription("Why the proposal didn't result in acceptance.")
    private RefusedPayload.Reason reason;
    /**
     * How many professionals have already been tried for this request_id, including this refusal.
     * (Required)
     * 
     */
    @JsonProperty("retry_count")
    @JsonPropertyDescription("How many professionals have already been tried for this request_id, including this refusal.")
    @DecimalMin("0")
    @NotNull
    private Integer retryCount;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RefusedPayload() {
    }

    /**
     * 
     * @param reason
     *     Why the proposal didn't result in acceptance.
     * @param retryCount
     *     How many professionals have already been tried for this request_id, including this refusal.
     * @param proId
     *     ID of the professional who refused (or failed to respond in time).
     */
    public RefusedPayload(String proId, RefusedPayload.Reason reason, Integer retryCount) {
        super();
        this.proId = proId;
        this.reason = reason;
        this.retryCount = retryCount;
    }

    /**
     * ID of the professional who refused (or failed to respond in time).
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public String getProId() {
        return proId;
    }

    /**
     * ID of the professional who refused (or failed to respond in time).
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public void setProId(String proId) {
        this.proId = proId;
    }

    public RefusedPayload withProId(String proId) {
        this.proId = proId;
        return this;
    }

    /**
     * Why the proposal didn't result in acceptance.
     * 
     */
    @JsonProperty("reason")
    public RefusedPayload.Reason getReason() {
        return reason;
    }

    /**
     * Why the proposal didn't result in acceptance.
     * 
     */
    @JsonProperty("reason")
    public void setReason(RefusedPayload.Reason reason) {
        this.reason = reason;
    }

    public RefusedPayload withReason(RefusedPayload.Reason reason) {
        this.reason = reason;
        return this;
    }

    /**
     * How many professionals have already been tried for this request_id, including this refusal.
     * (Required)
     * 
     */
    @JsonProperty("retry_count")
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * How many professionals have already been tried for this request_id, including this refusal.
     * (Required)
     * 
     */
    @JsonProperty("retry_count")
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public RefusedPayload withRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RefusedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("proId");
        sb.append('=');
        sb.append(((this.proId == null)?"<null>":this.proId));
        sb.append(',');
        sb.append("reason");
        sb.append('=');
        sb.append(((this.reason == null)?"<null>":this.reason));
        sb.append(',');
        sb.append("retryCount");
        sb.append('=');
        sb.append(((this.retryCount == null)?"<null>":this.retryCount));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.reason == null)? 0 :this.reason.hashCode()));
        result = ((result* 31)+((this.retryCount == null)? 0 :this.retryCount.hashCode()));
        result = ((result* 31)+((this.proId == null)? 0 :this.proId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RefusedPayload) == false) {
            return false;
        }
        RefusedPayload rhs = ((RefusedPayload) other);
        return ((((this.reason == rhs.reason)||((this.reason!= null)&&this.reason.equals(rhs.reason)))&&((this.retryCount == rhs.retryCount)||((this.retryCount!= null)&&this.retryCount.equals(rhs.retryCount))))&&((this.proId == rhs.proId)||((this.proId!= null)&&this.proId.equals(rhs.proId))));
    }


    /**
     * Why the proposal didn't result in acceptance.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Reason {

        DECLINED("declined"),
        TIMEOUT("timeout"),
        UNAVAILABLE("unavailable");
        private final String value;
        private final static Map<String, RefusedPayload.Reason> CONSTANTS = new HashMap<String, RefusedPayload.Reason>();

        static {
            for (RefusedPayload.Reason c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Reason(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static RefusedPayload.Reason fromValue(String value) {
            RefusedPayload.Reason constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
