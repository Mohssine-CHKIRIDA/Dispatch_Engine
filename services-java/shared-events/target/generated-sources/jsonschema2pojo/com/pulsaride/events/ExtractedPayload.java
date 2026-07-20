
package com.pulsaride.events;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


/**
 * ExtractedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "raw_text",
    "symptoms",
    "severity_keywords",
    "duration_hint",
    "specialty_hint",
    "location_hint",
    "age_hint",
    "extraction_confidence"
})
@Generated("jsonschema2pojo")
public class ExtractedPayload {

    /**
     * Echo of the original patient text, kept alongside the extraction for traceability and for IA3 embedding fallback.
     * (Required)
     * 
     */
    @JsonProperty("raw_text")
    @JsonPropertyDescription("Echo of the original patient text, kept alongside the extraction for traceability and for IA3 embedding fallback.")
    @NotNull
    private String rawText;
    /**
     * Discrete symptoms/complaints identified in the text, e.g. ['douleur thoracique', 'essoufflement'].
     * (Required)
     * 
     */
    @JsonProperty("symptoms")
    @JsonPropertyDescription("Discrete symptoms/complaints identified in the text, e.g. ['douleur thoracique', 'essoufflement'].")
    @Valid
    @NotNull
    private List<String> symptoms = new ArrayList<String>();
    /**
     * Phrases signalling severity/urgency, used as input signal for IA2, e.g. ['depuis ce matin', 'intense'].
     * 
     */
    @JsonProperty("severity_keywords")
    @JsonPropertyDescription("Phrases signalling severity/urgency, used as input signal for IA2, e.g. ['depuis ce matin', 'intense'].")
    @Valid
    private List<String> severityKeywords = new ArrayList<String>();
    /**
     * How long the patient reports the issue has been present, if stated, e.g. '3 jours'.
     * 
     */
    @JsonProperty("duration_hint")
    @JsonPropertyDescription("How long the patient reports the issue has been present, if stated, e.g. '3 jours'.")
    private String durationHint;
    /**
     * Free-text guess at the kind of professional needed, e.g. 'cardiologue'. This is NOT an exact tag — IA3 matches it semantically against professional profiles rather than doing exact string matching.
     * 
     */
    @JsonProperty("specialty_hint")
    @JsonPropertyDescription("Free-text guess at the kind of professional needed, e.g. 'cardiologue'. This is NOT an exact tag \u2014 IA3 matches it semantically against professional profiles rather than doing exact string matching.")
    private String specialtyHint;
    /**
     * Location mentioned in the text, if any, e.g. a neighborhood or city.
     * 
     */
    @JsonProperty("location_hint")
    @JsonPropertyDescription("Location mentioned in the text, if any, e.g. a neighborhood or city.")
    private String locationHint;
    /**
     * Patient age if mentioned or inferable from text.
     * 
     */
    @JsonProperty("age_hint")
    @JsonPropertyDescription("Patient age if mentioned or inferable from text.")
    @DecimalMin("0")
    private Integer ageHint;
    /**
     * IA1's confidence in this extraction. Low values may warrant fallback handling downstream.
     * (Required)
     * 
     */
    @JsonProperty("extraction_confidence")
    @JsonPropertyDescription("IA1's confidence in this extraction. Low values may warrant fallback handling downstream.")
    @NotNull
    private Double extractionConfidence;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExtractedPayload() {
    }

    /**
     * 
     * @param symptoms
     *     Discrete symptoms/complaints identified in the text, e.g. ['douleur thoracique', 'essoufflement'].
     * @param severityKeywords
     *     Phrases signalling severity/urgency, used as input signal for IA2, e.g. ['depuis ce matin', 'intense'].
     * @param specialtyHint
     *     Free-text guess at the kind of professional needed, e.g. 'cardiologue'. This is NOT an exact tag — IA3 matches it semantically against professional profiles rather than doing exact string matching.
     * @param rawText
     *     Echo of the original patient text, kept alongside the extraction for traceability and for IA3 embedding fallback.
     * @param durationHint
     *     How long the patient reports the issue has been present, if stated, e.g. '3 jours'.
     * @param ageHint
     *     Patient age if mentioned or inferable from text.
     * @param extractionConfidence
     *     IA1's confidence in this extraction. Low values may warrant fallback handling downstream.
     * @param locationHint
     *     Location mentioned in the text, if any, e.g. a neighborhood or city.
     */
    public ExtractedPayload(String rawText, List<String> symptoms, List<String> severityKeywords, String durationHint, String specialtyHint, String locationHint, Integer ageHint, Double extractionConfidence) {
        super();
        this.rawText = rawText;
        this.symptoms = symptoms;
        this.severityKeywords = severityKeywords;
        this.durationHint = durationHint;
        this.specialtyHint = specialtyHint;
        this.locationHint = locationHint;
        this.ageHint = ageHint;
        this.extractionConfidence = extractionConfidence;
    }

    /**
     * Echo of the original patient text, kept alongside the extraction for traceability and for IA3 embedding fallback.
     * (Required)
     * 
     */
    @JsonProperty("raw_text")
    public String getRawText() {
        return rawText;
    }

    /**
     * Echo of the original patient text, kept alongside the extraction for traceability and for IA3 embedding fallback.
     * (Required)
     * 
     */
    @JsonProperty("raw_text")
    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public ExtractedPayload withRawText(String rawText) {
        this.rawText = rawText;
        return this;
    }

    /**
     * Discrete symptoms/complaints identified in the text, e.g. ['douleur thoracique', 'essoufflement'].
     * (Required)
     * 
     */
    @JsonProperty("symptoms")
    public List<String> getSymptoms() {
        return symptoms;
    }

    /**
     * Discrete symptoms/complaints identified in the text, e.g. ['douleur thoracique', 'essoufflement'].
     * (Required)
     * 
     */
    @JsonProperty("symptoms")
    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public ExtractedPayload withSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
        return this;
    }

    /**
     * Phrases signalling severity/urgency, used as input signal for IA2, e.g. ['depuis ce matin', 'intense'].
     * 
     */
    @JsonProperty("severity_keywords")
    public List<String> getSeverityKeywords() {
        return severityKeywords;
    }

    /**
     * Phrases signalling severity/urgency, used as input signal for IA2, e.g. ['depuis ce matin', 'intense'].
     * 
     */
    @JsonProperty("severity_keywords")
    public void setSeverityKeywords(List<String> severityKeywords) {
        this.severityKeywords = severityKeywords;
    }

    public ExtractedPayload withSeverityKeywords(List<String> severityKeywords) {
        this.severityKeywords = severityKeywords;
        return this;
    }

    /**
     * How long the patient reports the issue has been present, if stated, e.g. '3 jours'.
     * 
     */
    @JsonProperty("duration_hint")
    public String getDurationHint() {
        return durationHint;
    }

    /**
     * How long the patient reports the issue has been present, if stated, e.g. '3 jours'.
     * 
     */
    @JsonProperty("duration_hint")
    public void setDurationHint(String durationHint) {
        this.durationHint = durationHint;
    }

    public ExtractedPayload withDurationHint(String durationHint) {
        this.durationHint = durationHint;
        return this;
    }

    /**
     * Free-text guess at the kind of professional needed, e.g. 'cardiologue'. This is NOT an exact tag — IA3 matches it semantically against professional profiles rather than doing exact string matching.
     * 
     */
    @JsonProperty("specialty_hint")
    public String getSpecialtyHint() {
        return specialtyHint;
    }

    /**
     * Free-text guess at the kind of professional needed, e.g. 'cardiologue'. This is NOT an exact tag — IA3 matches it semantically against professional profiles rather than doing exact string matching.
     * 
     */
    @JsonProperty("specialty_hint")
    public void setSpecialtyHint(String specialtyHint) {
        this.specialtyHint = specialtyHint;
    }

    public ExtractedPayload withSpecialtyHint(String specialtyHint) {
        this.specialtyHint = specialtyHint;
        return this;
    }

    /**
     * Location mentioned in the text, if any, e.g. a neighborhood or city.
     * 
     */
    @JsonProperty("location_hint")
    public String getLocationHint() {
        return locationHint;
    }

    /**
     * Location mentioned in the text, if any, e.g. a neighborhood or city.
     * 
     */
    @JsonProperty("location_hint")
    public void setLocationHint(String locationHint) {
        this.locationHint = locationHint;
    }

    public ExtractedPayload withLocationHint(String locationHint) {
        this.locationHint = locationHint;
        return this;
    }

    /**
     * Patient age if mentioned or inferable from text.
     * 
     */
    @JsonProperty("age_hint")
    public Integer getAgeHint() {
        return ageHint;
    }

    /**
     * Patient age if mentioned or inferable from text.
     * 
     */
    @JsonProperty("age_hint")
    public void setAgeHint(Integer ageHint) {
        this.ageHint = ageHint;
    }

    public ExtractedPayload withAgeHint(Integer ageHint) {
        this.ageHint = ageHint;
        return this;
    }

    /**
     * IA1's confidence in this extraction. Low values may warrant fallback handling downstream.
     * (Required)
     * 
     */
    @JsonProperty("extraction_confidence")
    public Double getExtractionConfidence() {
        return extractionConfidence;
    }

    /**
     * IA1's confidence in this extraction. Low values may warrant fallback handling downstream.
     * (Required)
     * 
     */
    @JsonProperty("extraction_confidence")
    public void setExtractionConfidence(Double extractionConfidence) {
        this.extractionConfidence = extractionConfidence;
    }

    public ExtractedPayload withExtractionConfidence(Double extractionConfidence) {
        this.extractionConfidence = extractionConfidence;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ExtractedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("rawText");
        sb.append('=');
        sb.append(((this.rawText == null)?"<null>":this.rawText));
        sb.append(',');
        sb.append("symptoms");
        sb.append('=');
        sb.append(((this.symptoms == null)?"<null>":this.symptoms));
        sb.append(',');
        sb.append("severityKeywords");
        sb.append('=');
        sb.append(((this.severityKeywords == null)?"<null>":this.severityKeywords));
        sb.append(',');
        sb.append("durationHint");
        sb.append('=');
        sb.append(((this.durationHint == null)?"<null>":this.durationHint));
        sb.append(',');
        sb.append("specialtyHint");
        sb.append('=');
        sb.append(((this.specialtyHint == null)?"<null>":this.specialtyHint));
        sb.append(',');
        sb.append("locationHint");
        sb.append('=');
        sb.append(((this.locationHint == null)?"<null>":this.locationHint));
        sb.append(',');
        sb.append("ageHint");
        sb.append('=');
        sb.append(((this.ageHint == null)?"<null>":this.ageHint));
        sb.append(',');
        sb.append("extractionConfidence");
        sb.append('=');
        sb.append(((this.extractionConfidence == null)?"<null>":this.extractionConfidence));
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
        result = ((result* 31)+((this.symptoms == null)? 0 :this.symptoms.hashCode()));
        result = ((result* 31)+((this.severityKeywords == null)? 0 :this.severityKeywords.hashCode()));
        result = ((result* 31)+((this.specialtyHint == null)? 0 :this.specialtyHint.hashCode()));
        result = ((result* 31)+((this.rawText == null)? 0 :this.rawText.hashCode()));
        result = ((result* 31)+((this.durationHint == null)? 0 :this.durationHint.hashCode()));
        result = ((result* 31)+((this.ageHint == null)? 0 :this.ageHint.hashCode()));
        result = ((result* 31)+((this.extractionConfidence == null)? 0 :this.extractionConfidence.hashCode()));
        result = ((result* 31)+((this.locationHint == null)? 0 :this.locationHint.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExtractedPayload) == false) {
            return false;
        }
        ExtractedPayload rhs = ((ExtractedPayload) other);
        return (((((((((this.symptoms == rhs.symptoms)||((this.symptoms!= null)&&this.symptoms.equals(rhs.symptoms)))&&((this.severityKeywords == rhs.severityKeywords)||((this.severityKeywords!= null)&&this.severityKeywords.equals(rhs.severityKeywords))))&&((this.specialtyHint == rhs.specialtyHint)||((this.specialtyHint!= null)&&this.specialtyHint.equals(rhs.specialtyHint))))&&((this.rawText == rhs.rawText)||((this.rawText!= null)&&this.rawText.equals(rhs.rawText))))&&((this.durationHint == rhs.durationHint)||((this.durationHint!= null)&&this.durationHint.equals(rhs.durationHint))))&&((this.ageHint == rhs.ageHint)||((this.ageHint!= null)&&this.ageHint.equals(rhs.ageHint))))&&((this.extractionConfidence == rhs.extractionConfidence)||((this.extractionConfidence!= null)&&this.extractionConfidence.equals(rhs.extractionConfidence))))&&((this.locationHint == rhs.locationHint)||((this.locationHint!= null)&&this.locationHint.equals(rhs.locationHint))));
    }

}
