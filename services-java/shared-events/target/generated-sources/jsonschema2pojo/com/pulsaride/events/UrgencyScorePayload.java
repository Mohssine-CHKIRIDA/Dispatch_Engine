
package com.pulsaride.events;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


/**
 * UrgencyScorePayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "score",
    "rationale",
    "confidence"
})
@Generated("jsonschema2pojo")
public class UrgencyScorePayload {

    /**
     *  0 = non-urgent, 3 = critical/emergency. Drives Dispatch Core's prioritization within the queue.
     * (Required)
     * 
     */
    @JsonProperty("score")
    @JsonPropertyDescription("0 = non-urgent, 3 = critical/emergency. Drives Dispatch Core's prioritization within the queue.")
    @DecimalMin("0")
    @DecimalMax("3")
    @NotNull
    private Integer score;
    /**
     * Short human-readable explanation for the score, useful for debugging and for the Evaluation report.
     * 
     */
    @JsonProperty("rationale")
    @JsonPropertyDescription("Short human-readable explanation for the score, useful for debugging and for the Evaluation report.")
    private String rationale;
    /**
     * IA2's confidence in this score.
     * (Required)
     * 
     */
    @JsonProperty("confidence")
    @JsonPropertyDescription("IA2's confidence in this score.")
    @NotNull
    private Double confidence;

    /**
     * No args constructor for use in serialization
     * 
     */
    public UrgencyScorePayload() {
    }

    /**
     * 
     * @param score
     *      0 = non-urgent, 3 = critical/emergency. Drives Dispatch Core's prioritization within the queue.
     * @param confidence
     *     IA2's confidence in this score.
     * @param rationale
     *     Short human-readable explanation for the score, useful for debugging and for the Evaluation report.
     */
    public UrgencyScorePayload(Integer score, String rationale, Double confidence) {
        super();
        this.score = score;
        this.rationale = rationale;
        this.confidence = confidence;
    }

    /**
     *  0 = non-urgent, 3 = critical/emergency. Drives Dispatch Core's prioritization within the queue.
     * (Required)
     * 
     */
    @JsonProperty("score")
    public Integer getScore() {
        return score;
    }

    /**
     *  0 = non-urgent, 3 = critical/emergency. Drives Dispatch Core's prioritization within the queue.
     * (Required)
     * 
     */
    @JsonProperty("score")
    public void setScore(Integer score) {
        this.score = score;
    }

    public UrgencyScorePayload withScore(Integer score) {
        this.score = score;
        return this;
    }

    /**
     * Short human-readable explanation for the score, useful for debugging and for the Evaluation report.
     * 
     */
    @JsonProperty("rationale")
    public String getRationale() {
        return rationale;
    }

    /**
     * Short human-readable explanation for the score, useful for debugging and for the Evaluation report.
     * 
     */
    @JsonProperty("rationale")
    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public UrgencyScorePayload withRationale(String rationale) {
        this.rationale = rationale;
        return this;
    }

    /**
     * IA2's confidence in this score.
     * (Required)
     * 
     */
    @JsonProperty("confidence")
    public Double getConfidence() {
        return confidence;
    }

    /**
     * IA2's confidence in this score.
     * (Required)
     * 
     */
    @JsonProperty("confidence")
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public UrgencyScorePayload withConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(UrgencyScorePayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("score");
        sb.append('=');
        sb.append(((this.score == null)?"<null>":this.score));
        sb.append(',');
        sb.append("rationale");
        sb.append('=');
        sb.append(((this.rationale == null)?"<null>":this.rationale));
        sb.append(',');
        sb.append("confidence");
        sb.append('=');
        sb.append(((this.confidence == null)?"<null>":this.confidence));
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
        result = ((result* 31)+((this.score == null)? 0 :this.score.hashCode()));
        result = ((result* 31)+((this.rationale == null)? 0 :this.rationale.hashCode()));
        result = ((result* 31)+((this.confidence == null)? 0 :this.confidence.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UrgencyScorePayload) == false) {
            return false;
        }
        UrgencyScorePayload rhs = ((UrgencyScorePayload) other);
        return ((((this.score == rhs.score)||((this.score!= null)&&this.score.equals(rhs.score)))&&((this.rationale == rhs.rationale)||((this.rationale!= null)&&this.rationale.equals(rhs.rationale))))&&((this.confidence == rhs.confidence)||((this.confidence!= null)&&this.confidence.equals(rhs.confidence))));
    }

}
