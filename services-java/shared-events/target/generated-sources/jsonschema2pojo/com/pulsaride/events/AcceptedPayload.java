
package com.pulsaride.events;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;


/**
 * AcceptedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pro_id",
    "ttfa_seconds"
})
@Generated("jsonschema2pojo")
public class AcceptedPayload {

    /**
     * ID of the professional who accepted.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    @JsonPropertyDescription("ID of the professional who accepted.")
    @NotNull
    private String proId;
    /**
     * Time-to-first-assignment: seconds elapsed between request.submitted and this acceptance. One of the key KPIs Evaluation compares across strategies.
     * 
     */
    @JsonProperty("ttfa_seconds")
    @JsonPropertyDescription("Time-to-first-assignment: seconds elapsed between request.submitted and this acceptance. One of the key KPIs Evaluation compares across strategies.")
    private Double ttfaSeconds;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AcceptedPayload() {
    }

    /**
     * 
     * @param ttfaSeconds
     *     Time-to-first-assignment: seconds elapsed between request.submitted and this acceptance. One of the key KPIs Evaluation compares across strategies.
     * @param proId
     *     ID of the professional who accepted.
     */
    public AcceptedPayload(String proId, Double ttfaSeconds) {
        super();
        this.proId = proId;
        this.ttfaSeconds = ttfaSeconds;
    }

    /**
     * ID of the professional who accepted.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public String getProId() {
        return proId;
    }

    /**
     * ID of the professional who accepted.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public void setProId(String proId) {
        this.proId = proId;
    }

    public AcceptedPayload withProId(String proId) {
        this.proId = proId;
        return this;
    }

    /**
     * Time-to-first-assignment: seconds elapsed between request.submitted and this acceptance. One of the key KPIs Evaluation compares across strategies.
     * 
     */
    @JsonProperty("ttfa_seconds")
    public Double getTtfaSeconds() {
        return ttfaSeconds;
    }

    /**
     * Time-to-first-assignment: seconds elapsed between request.submitted and this acceptance. One of the key KPIs Evaluation compares across strategies.
     * 
     */
    @JsonProperty("ttfa_seconds")
    public void setTtfaSeconds(Double ttfaSeconds) {
        this.ttfaSeconds = ttfaSeconds;
    }

    public AcceptedPayload withTtfaSeconds(Double ttfaSeconds) {
        this.ttfaSeconds = ttfaSeconds;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AcceptedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("proId");
        sb.append('=');
        sb.append(((this.proId == null)?"<null>":this.proId));
        sb.append(',');
        sb.append("ttfaSeconds");
        sb.append('=');
        sb.append(((this.ttfaSeconds == null)?"<null>":this.ttfaSeconds));
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
        result = ((result* 31)+((this.proId == null)? 0 :this.proId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AcceptedPayload) == false) {
            return false;
        }
        AcceptedPayload rhs = ((AcceptedPayload) other);
        return (((this.ttfaSeconds == rhs.ttfaSeconds)||((this.ttfaSeconds!= null)&&this.ttfaSeconds.equals(rhs.ttfaSeconds)))&&((this.proId == rhs.proId)||((this.proId!= null)&&this.proId.equals(rhs.proId))));
    }

}
