
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
 * ClosedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "outcome",
    "pro_id",
    "ttfa_seconds",
    "total_refusals"
})
@Generated("jsonschema2pojo")
public class ClosedPayload {

    /**
     * Terminal outcome of the request.
     * (Required)
     * 
     */
    @JsonProperty("outcome")
    @JsonPropertyDescription("Terminal outcome of the request.")
    @NotNull
    private ClosedPayload.Outcome outcome;
    /**
     * Professional ultimately assigned, if outcome is 'dispatched'; null otherwise.
     * 
     */
    @JsonProperty("pro_id")
    @JsonPropertyDescription("Professional ultimately assigned, if outcome is 'dispatched'; null otherwise.")
    private String proId;
    /**
     * Final time-to-first-assignment for this request, for KPI aggregation.
     * 
     */
    @JsonProperty("ttfa_seconds")
    @JsonPropertyDescription("Final time-to-first-assignment for this request, for KPI aggregation.")
    private Double ttfaSeconds;
    /**
     * Total number of professionals who refused/timed-out before closure.
     * (Required)
     * 
     */
    @JsonProperty("total_refusals")
    @JsonPropertyDescription("Total number of professionals who refused/timed-out before closure.")
    @DecimalMin("0")
    @NotNull
    private Integer totalRefusals;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClosedPayload() {
    }

    /**
     * 
     * @param ttfaSeconds
     *     Final time-to-first-assignment for this request, for KPI aggregation.
     * @param proId
     *     Professional ultimately assigned, if outcome is 'dispatched'; null otherwise.
     * @param totalRefusals
     *     Total number of professionals who refused/timed-out before closure.
     * @param outcome
     *     Terminal outcome of the request.
     */
    public ClosedPayload(ClosedPayload.Outcome outcome, String proId, Double ttfaSeconds, Integer totalRefusals) {
        super();
        this.outcome = outcome;
        this.proId = proId;
        this.ttfaSeconds = ttfaSeconds;
        this.totalRefusals = totalRefusals;
    }

    /**
     * Terminal outcome of the request.
     * (Required)
     * 
     */
    @JsonProperty("outcome")
    public ClosedPayload.Outcome getOutcome() {
        return outcome;
    }

    /**
     * Terminal outcome of the request.
     * (Required)
     * 
     */
    @JsonProperty("outcome")
    public void setOutcome(ClosedPayload.Outcome outcome) {
        this.outcome = outcome;
    }

    public ClosedPayload withOutcome(ClosedPayload.Outcome outcome) {
        this.outcome = outcome;
        return this;
    }

    /**
     * Professional ultimately assigned, if outcome is 'dispatched'; null otherwise.
     * 
     */
    @JsonProperty("pro_id")
    public String getProId() {
        return proId;
    }

    /**
     * Professional ultimately assigned, if outcome is 'dispatched'; null otherwise.
     * 
     */
    @JsonProperty("pro_id")
    public void setProId(String proId) {
        this.proId = proId;
    }

    public ClosedPayload withProId(String proId) {
        this.proId = proId;
        return this;
    }

    /**
     * Final time-to-first-assignment for this request, for KPI aggregation.
     * 
     */
    @JsonProperty("ttfa_seconds")
    public Double getTtfaSeconds() {
        return ttfaSeconds;
    }

    /**
     * Final time-to-first-assignment for this request, for KPI aggregation.
     * 
     */
    @JsonProperty("ttfa_seconds")
    public void setTtfaSeconds(Double ttfaSeconds) {
        this.ttfaSeconds = ttfaSeconds;
    }

    public ClosedPayload withTtfaSeconds(Double ttfaSeconds) {
        this.ttfaSeconds = ttfaSeconds;
        return this;
    }

    /**
     * Total number of professionals who refused/timed-out before closure.
     * (Required)
     * 
     */
    @JsonProperty("total_refusals")
    public Integer getTotalRefusals() {
        return totalRefusals;
    }

    /**
     * Total number of professionals who refused/timed-out before closure.
     * (Required)
     * 
     */
    @JsonProperty("total_refusals")
    public void setTotalRefusals(Integer totalRefusals) {
        this.totalRefusals = totalRefusals;
    }

    public ClosedPayload withTotalRefusals(Integer totalRefusals) {
        this.totalRefusals = totalRefusals;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ClosedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("outcome");
        sb.append('=');
        sb.append(((this.outcome == null)?"<null>":this.outcome));
        sb.append(',');
        sb.append("proId");
        sb.append('=');
        sb.append(((this.proId == null)?"<null>":this.proId));
        sb.append(',');
        sb.append("ttfaSeconds");
        sb.append('=');
        sb.append(((this.ttfaSeconds == null)?"<null>":this.ttfaSeconds));
        sb.append(',');
        sb.append("totalRefusals");
        sb.append('=');
        sb.append(((this.totalRefusals == null)?"<null>":this.totalRefusals));
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
        result = ((result* 31)+((this.ttfaSeconds == null)? 0 :this.ttfaSeconds.hashCode()));
        result = ((result* 31)+((this.totalRefusals == null)? 0 :this.totalRefusals.hashCode()));
        result = ((result* 31)+((this.outcome == null)? 0 :this.outcome.hashCode()));
        result = ((result* 31)+((this.proId == null)? 0 :this.proId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClosedPayload) == false) {
            return false;
        }
        ClosedPayload rhs = ((ClosedPayload) other);
        return (((((this.ttfaSeconds == rhs.ttfaSeconds)||((this.ttfaSeconds!= null)&&this.ttfaSeconds.equals(rhs.ttfaSeconds)))&&((this.totalRefusals == rhs.totalRefusals)||((this.totalRefusals!= null)&&this.totalRefusals.equals(rhs.totalRefusals))))&&((this.outcome == rhs.outcome)||((this.outcome!= null)&&this.outcome.equals(rhs.outcome))))&&((this.proId == rhs.proId)||((this.proId!= null)&&this.proId.equals(rhs.proId))));
    }


    /**
     * Terminal outcome of the request.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Outcome {

        DISPATCHED("dispatched"),
        CANCELLED("cancelled"),
        NO_PROFESSIONAL_AVAILABLE("no_professional_available"),
        TIMEOUT("timeout");
        private final String value;
        private final static Map<String, ClosedPayload.Outcome> CONSTANTS = new HashMap<String, ClosedPayload.Outcome>();

        static {
            for (ClosedPayload.Outcome c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Outcome(String value) {
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
        public static ClosedPayload.Outcome fromValue(String value) {
            ClosedPayload.Outcome constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
