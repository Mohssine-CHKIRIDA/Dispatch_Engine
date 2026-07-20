
package com.pulsaride.events;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * SubmittedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "patient_id",
    "text"
})
@Generated("jsonschema2pojo")
public class SubmittedPayload {

    /**
     * Identifier for the patient submitting the request. In this prototype this is a synthetic/simulated ID (no real health data — out of scope per the doc).
     * (Required)
     * 
     */
    @JsonProperty("patient_id")
    @JsonPropertyDescription("Identifier for the patient submitting the request. In this prototype this is a synthetic/simulated ID (no real health data \u2014 out of scope per the doc).")
    @NotNull
    private String patientId;
    /**
     * Raw free-text description of the patient's situation, exactly as submitted. This is the only input IA1 works from.
     * (Required)
     * 
     */
    @JsonProperty("text")
    @JsonPropertyDescription("Raw free-text description of the patient's situation, exactly as submitted. This is the only input IA1 works from.")
    @Size(min = 1)
    @NotNull
    private String text;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SubmittedPayload() {
    }

    /**
     * 
     * @param patientId
     *     Identifier for the patient submitting the request. In this prototype this is a synthetic/simulated ID (no real health data — out of scope per the doc).
     * @param text
     *     Raw free-text description of the patient's situation, exactly as submitted. This is the only input IA1 works from.
     */
    public SubmittedPayload(String patientId, String text) {
        super();
        this.patientId = patientId;
        this.text = text;
    }

    /**
     * Identifier for the patient submitting the request. In this prototype this is a synthetic/simulated ID (no real health data — out of scope per the doc).
     * (Required)
     * 
     */
    @JsonProperty("patient_id")
    public String getPatientId() {
        return patientId;
    }

    /**
     * Identifier for the patient submitting the request. In this prototype this is a synthetic/simulated ID (no real health data — out of scope per the doc).
     * (Required)
     * 
     */
    @JsonProperty("patient_id")
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public SubmittedPayload withPatientId(String patientId) {
        this.patientId = patientId;
        return this;
    }

    /**
     * Raw free-text description of the patient's situation, exactly as submitted. This is the only input IA1 works from.
     * (Required)
     * 
     */
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    /**
     * Raw free-text description of the patient's situation, exactly as submitted. This is the only input IA1 works from.
     * (Required)
     * 
     */
    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    public SubmittedPayload withText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SubmittedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("patientId");
        sb.append('=');
        sb.append(((this.patientId == null)?"<null>":this.patientId));
        sb.append(',');
        sb.append("text");
        sb.append('=');
        sb.append(((this.text == null)?"<null>":this.text));
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
        result = ((result* 31)+((this.patientId == null)? 0 :this.patientId.hashCode()));
        result = ((result* 31)+((this.text == null)? 0 :this.text.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubmittedPayload) == false) {
            return false;
        }
        SubmittedPayload rhs = ((SubmittedPayload) other);
        return (((this.patientId == rhs.patientId)||((this.patientId!= null)&&this.patientId.equals(rhs.patientId)))&&((this.text == rhs.text)||((this.text!= null)&&this.text.equals(rhs.text))));
    }

}
