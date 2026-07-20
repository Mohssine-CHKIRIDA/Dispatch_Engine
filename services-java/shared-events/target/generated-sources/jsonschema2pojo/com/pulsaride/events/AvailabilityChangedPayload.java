
package com.pulsaride.events;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


/**
 * AvailabilityChangedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pro_id",
    "available",
    "current_load"
})
@Generated("jsonschema2pojo")
public class AvailabilityChangedPayload {

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
     * Whether the professional can currently receive new proposals.
     * (Required)
     * 
     */
    @JsonProperty("available")
    @JsonPropertyDescription("Whether the professional can currently receive new proposals.")
    @NotNull
    private Boolean available;
    /**
     * Number of active cases currently assigned to this professional, if tracked. Used by Dispatch Core to avoid overloading a single pro.
     * 
     */
    @JsonProperty("current_load")
    @JsonPropertyDescription("Number of active cases currently assigned to this professional, if tracked. Used by Dispatch Core to avoid overloading a single pro.")
    @DecimalMin("0")
    private Integer currentLoad;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AvailabilityChangedPayload() {
    }

    /**
     * 
     * @param proId
     *     Professional's unique ID.
     * @param available
     *     Whether the professional can currently receive new proposals.
     * @param currentLoad
     *     Number of active cases currently assigned to this professional, if tracked. Used by Dispatch Core to avoid overloading a single pro.
     */
    public AvailabilityChangedPayload(String proId, Boolean available, Integer currentLoad) {
        super();
        this.proId = proId;
        this.available = available;
        this.currentLoad = currentLoad;
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

    public AvailabilityChangedPayload withProId(String proId) {
        this.proId = proId;
        return this;
    }

    /**
     * Whether the professional can currently receive new proposals.
     * (Required)
     * 
     */
    @JsonProperty("available")
    public Boolean getAvailable() {
        return available;
    }

    /**
     * Whether the professional can currently receive new proposals.
     * (Required)
     * 
     */
    @JsonProperty("available")
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public AvailabilityChangedPayload withAvailable(Boolean available) {
        this.available = available;
        return this;
    }

    /**
     * Number of active cases currently assigned to this professional, if tracked. Used by Dispatch Core to avoid overloading a single pro.
     * 
     */
    @JsonProperty("current_load")
    public Integer getCurrentLoad() {
        return currentLoad;
    }

    /**
     * Number of active cases currently assigned to this professional, if tracked. Used by Dispatch Core to avoid overloading a single pro.
     * 
     */
    @JsonProperty("current_load")
    public void setCurrentLoad(Integer currentLoad) {
        this.currentLoad = currentLoad;
    }

    public AvailabilityChangedPayload withCurrentLoad(Integer currentLoad) {
        this.currentLoad = currentLoad;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AvailabilityChangedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("proId");
        sb.append('=');
        sb.append(((this.proId == null)?"<null>":this.proId));
        sb.append(',');
        sb.append("available");
        sb.append('=');
        sb.append(((this.available == null)?"<null>":this.available));
        sb.append(',');
        sb.append("currentLoad");
        sb.append('=');
        sb.append(((this.currentLoad == null)?"<null>":this.currentLoad));
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
        result = ((result* 31)+((this.available == null)? 0 :this.available.hashCode()));
        result = ((result* 31)+((this.currentLoad == null)? 0 :this.currentLoad.hashCode()));
        result = ((result* 31)+((this.proId == null)? 0 :this.proId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AvailabilityChangedPayload) == false) {
            return false;
        }
        AvailabilityChangedPayload rhs = ((AvailabilityChangedPayload) other);
        return ((((this.available == rhs.available)||((this.available!= null)&&this.available.equals(rhs.available)))&&((this.currentLoad == rhs.currentLoad)||((this.currentLoad!= null)&&this.currentLoad.equals(rhs.currentLoad))))&&((this.proId == rhs.proId)||((this.proId!= null)&&this.proId.equals(rhs.proId))));
    }

}
