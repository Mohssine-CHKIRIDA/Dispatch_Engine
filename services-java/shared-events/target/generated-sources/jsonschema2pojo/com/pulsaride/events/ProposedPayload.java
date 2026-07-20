
package com.pulsaride.events;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;


/**
 * ProposedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pro_id",
    "strategy",
    "match_score",
    "expires_at"
})
@Generated("jsonschema2pojo")
public class ProposedPayload {

    /**
     * ID of the professional being proposed, from Pro Registry.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    @JsonPropertyDescription("ID of the professional being proposed, from Pro Registry.")
    @NotNull
    private String proId;
    /**
     * Which matching strategy produced this proposal, so Evaluation can attribute outcomes to a strategy. S1/S2 = classic tag-based matching, S3/S4 = semantic embedding-based matching.
     * (Required)
     * 
     */
    @JsonProperty("strategy")
    @JsonPropertyDescription("Which matching strategy produced this proposal, so Evaluation can attribute outcomes to a strategy. S1/S2 = classic tag-based matching, S3/S4 = semantic embedding-based matching.")
    @NotNull
    private ProposedPayload.Strategy strategy;
    /**
     * Cosine similarity or ranking score behind this choice, if the strategy produces one (null for purely rule-based strategies).
     * 
     */
    @JsonProperty("match_score")
    @JsonPropertyDescription("Cosine similarity or ranking score behind this choice, if the strategy produces one (null for purely rule-based strategies).")
    private Double matchScore;
    /**
     * Deadline by which the pro must respond before Dispatch Core times the proposal out and tries the next candidate.
     * 
     */
    @JsonProperty("expires_at")
    @JsonPropertyDescription("Deadline by which the pro must respond before Dispatch Core times the proposal out and tries the next candidate.")
    private OffsetDateTime expiresAt;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProposedPayload() {
    }

    /**
     * 
     * @param matchScore
     *     Cosine similarity or ranking score behind this choice, if the strategy produces one (null for purely rule-based strategies).
     * @param proId
     *     ID of the professional being proposed, from Pro Registry.
     * @param strategy
     *     Which matching strategy produced this proposal, so Evaluation can attribute outcomes to a strategy. S1/S2 = classic tag-based matching, S3/S4 = semantic embedding-based matching.
     * @param expiresAt
     *     Deadline by which the pro must respond before Dispatch Core times the proposal out and tries the next candidate.
     */
    public ProposedPayload(String proId, ProposedPayload.Strategy strategy, Double matchScore, OffsetDateTime expiresAt) {
        super();
        this.proId = proId;
        this.strategy = strategy;
        this.matchScore = matchScore;
        this.expiresAt = expiresAt;
    }

    /**
     * ID of the professional being proposed, from Pro Registry.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public String getProId() {
        return proId;
    }

    /**
     * ID of the professional being proposed, from Pro Registry.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public void setProId(String proId) {
        this.proId = proId;
    }

    public ProposedPayload withProId(String proId) {
        this.proId = proId;
        return this;
    }

    /**
     * Which matching strategy produced this proposal, so Evaluation can attribute outcomes to a strategy. S1/S2 = classic tag-based matching, S3/S4 = semantic embedding-based matching.
     * (Required)
     * 
     */
    @JsonProperty("strategy")
    public ProposedPayload.Strategy getStrategy() {
        return strategy;
    }

    /**
     * Which matching strategy produced this proposal, so Evaluation can attribute outcomes to a strategy. S1/S2 = classic tag-based matching, S3/S4 = semantic embedding-based matching.
     * (Required)
     * 
     */
    @JsonProperty("strategy")
    public void setStrategy(ProposedPayload.Strategy strategy) {
        this.strategy = strategy;
    }

    public ProposedPayload withStrategy(ProposedPayload.Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    /**
     * Cosine similarity or ranking score behind this choice, if the strategy produces one (null for purely rule-based strategies).
     * 
     */
    @JsonProperty("match_score")
    public Double getMatchScore() {
        return matchScore;
    }

    /**
     * Cosine similarity or ranking score behind this choice, if the strategy produces one (null for purely rule-based strategies).
     * 
     */
    @JsonProperty("match_score")
    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }

    public ProposedPayload withMatchScore(Double matchScore) {
        this.matchScore = matchScore;
        return this;
    }

    /**
     * Deadline by which the pro must respond before Dispatch Core times the proposal out and tries the next candidate.
     * 
     */
    @JsonProperty("expires_at")
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Deadline by which the pro must respond before Dispatch Core times the proposal out and tries the next candidate.
     * 
     */
    @JsonProperty("expires_at")
    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public ProposedPayload withExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProposedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("proId");
        sb.append('=');
        sb.append(((this.proId == null)?"<null>":this.proId));
        sb.append(',');
        sb.append("strategy");
        sb.append('=');
        sb.append(((this.strategy == null)?"<null>":this.strategy));
        sb.append(',');
        sb.append("matchScore");
        sb.append('=');
        sb.append(((this.matchScore == null)?"<null>":this.matchScore));
        sb.append(',');
        sb.append("expiresAt");
        sb.append('=');
        sb.append(((this.expiresAt == null)?"<null>":this.expiresAt));
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
        result = ((result* 31)+((this.matchScore == null)? 0 :this.matchScore.hashCode()));
        result = ((result* 31)+((this.strategy == null)? 0 :this.strategy.hashCode()));
        result = ((result* 31)+((this.expiresAt == null)? 0 :this.expiresAt.hashCode()));
        result = ((result* 31)+((this.proId == null)? 0 :this.proId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProposedPayload) == false) {
            return false;
        }
        ProposedPayload rhs = ((ProposedPayload) other);
        return (((((this.matchScore == rhs.matchScore)||((this.matchScore!= null)&&this.matchScore.equals(rhs.matchScore)))&&((this.strategy == rhs.strategy)||((this.strategy!= null)&&this.strategy.equals(rhs.strategy))))&&((this.expiresAt == rhs.expiresAt)||((this.expiresAt!= null)&&this.expiresAt.equals(rhs.expiresAt))))&&((this.proId == rhs.proId)||((this.proId!= null)&&this.proId.equals(rhs.proId))));
    }


    /**
     * Which matching strategy produced this proposal, so Evaluation can attribute outcomes to a strategy. S1/S2 = classic tag-based matching, S3/S4 = semantic embedding-based matching.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Strategy {

        S_1("S1"),
        S_2("S2"),
        S_3("S3"),
        S_4("S4");
        private final String value;
        private final static Map<String, ProposedPayload.Strategy> CONSTANTS = new HashMap<String, ProposedPayload.Strategy>();

        static {
            for (ProposedPayload.Strategy c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Strategy(String value) {
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
        public static ProposedPayload.Strategy fromValue(String value) {
            ProposedPayload.Strategy constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
