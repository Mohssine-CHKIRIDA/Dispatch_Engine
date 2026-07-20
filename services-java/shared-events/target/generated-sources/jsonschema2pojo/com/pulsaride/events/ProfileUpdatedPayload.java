
package com.pulsaride.events;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;


/**
 * ProfileUpdatedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pro_id",
    "specialty",
    "description"
})
@Generated("jsonschema2pojo")
public class ProfileUpdatedPayload {

    /**
     * Professional's unique ID.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    @JsonPropertyDescription("Professional's unique ID.")
    @NotNull
    private String proId;
    /**
     * Declared specialty/profession, e.g. 'infirmier', 'médecin généraliste'.
     * (Required)
     * 
     */
    @JsonProperty("specialty")
    @JsonPropertyDescription("Declared specialty/profession, e.g. 'infirmier', 'm\u00e9decin g\u00e9n\u00e9raliste'.")
    @NotNull
    private String specialty;
    /**
     * Free-text profile description used as the basis for the semantic embedding (this is what gets compared against request.case_embedded via cosine similarity).
     * (Required)
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Free-text profile description used as the basis for the semantic embedding (this is what gets compared against request.case_embedded via cosine similarity).")
    @NotNull
    private String description;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProfileUpdatedPayload() {
    }

    /**
     * 
     * @param specialty
     *     Declared specialty/profession, e.g. 'infirmier', 'médecin généraliste'.
     * @param proId
     *     Professional's unique ID.
     * @param description
     *     Free-text profile description used as the basis for the semantic embedding (this is what gets compared against request.case_embedded via cosine similarity).
     */
    public ProfileUpdatedPayload(String proId, String specialty, String description) {
        super();
        this.proId = proId;
        this.specialty = specialty;
        this.description = description;
    }

    /**
     * Professional's unique ID.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public String getProId() {
        return proId;
    }

    /**
     * Professional's unique ID.
     * (Required)
     * 
     */
    @JsonProperty("pro_id")
    public void setProId(String proId) {
        this.proId = proId;
    }

    public ProfileUpdatedPayload withProId(String proId) {
        this.proId = proId;
        return this;
    }

    /**
     * Declared specialty/profession, e.g. 'infirmier', 'médecin généraliste'.
     * (Required)
     * 
     */
    @JsonProperty("specialty")
    public String getSpecialty() {
        return specialty;
    }

    /**
     * Declared specialty/profession, e.g. 'infirmier', 'médecin généraliste'.
     * (Required)
     * 
     */
    @JsonProperty("specialty")
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public ProfileUpdatedPayload withSpecialty(String specialty) {
        this.specialty = specialty;
        return this;
    }

    /**
     * Free-text profile description used as the basis for the semantic embedding (this is what gets compared against request.case_embedded via cosine similarity).
     * (Required)
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Free-text profile description used as the basis for the semantic embedding (this is what gets compared against request.case_embedded via cosine similarity).
     * (Required)
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public ProfileUpdatedPayload withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProfileUpdatedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("proId");
        sb.append('=');
        sb.append(((this.proId == null)?"<null>":this.proId));
        sb.append(',');
        sb.append("specialty");
        sb.append('=');
        sb.append(((this.specialty == null)?"<null>":this.specialty));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
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
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.specialty == null)? 0 :this.specialty.hashCode()));
        result = ((result* 31)+((this.proId == null)? 0 :this.proId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProfileUpdatedPayload) == false) {
            return false;
        }
        ProfileUpdatedPayload rhs = ((ProfileUpdatedPayload) other);
        return ((((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description)))&&((this.specialty == rhs.specialty)||((this.specialty!= null)&&this.specialty.equals(rhs.specialty))))&&((this.proId == rhs.proId)||((this.proId!= null)&&this.proId.equals(rhs.proId))));
    }

}
