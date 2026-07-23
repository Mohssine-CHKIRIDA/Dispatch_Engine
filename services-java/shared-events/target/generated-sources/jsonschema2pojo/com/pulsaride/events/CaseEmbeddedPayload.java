
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
import jakarta.validation.constraints.Size;


/**
 * CaseEmbeddedPayload
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "embedding",
    "dimension",
    "embedding_model"
})
@Generated("jsonschema2pojo")
public class CaseEmbeddedPayload {

    /**
     * The embedding vector for this request's case. Length must match `dimension` and the pgvector column dimension in matching_db.
     * (Required)
     * 
     */
    @JsonProperty("embedding")
    @JsonPropertyDescription("The embedding vector for this request's case. Length must match `dimension` and the pgvector column dimension in matching_db.")
    @Size(min = 1)
    @Valid
    @NotNull
    private List<Double> embedding = new ArrayList<Double>();
    /**
     * Length of the embedding vector, e.g. 384 for all-MiniLM-L6-v2.
     * (Required)
     * 
     */
    @JsonProperty("dimension")
    @JsonPropertyDescription("Length of the embedding vector, e.g. 384 for all-MiniLM-L6-v2.")
    @DecimalMin("1")
    @NotNull
    private Integer dimension;
    /**
     * Identifier of the sentence-transformers model used, e.g. 'sentence-transformers/all-MiniLM-L6-v2'. Needed so Evaluation/Dispatch never compare vectors produced by two different models.
     * (Required)
     * 
     */
    @JsonProperty("embedding_model")
    @JsonPropertyDescription("Identifier of the sentence-transformers model used, e.g. 'sentence-transformers/all-MiniLM-L6-v2'. Needed so Evaluation/Dispatch never compare vectors produced by two different models.")
    @NotNull
    private String embeddingModel;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CaseEmbeddedPayload() {
    }

    /**
     * 
     * @param embedding
     *     The embedding vector for this request's case. Length must match `dimension` and the pgvector column dimension in matching_db.
     * @param embeddingModel
     *     Identifier of the sentence-transformers model used, e.g. 'sentence-transformers/all-MiniLM-L6-v2'. Needed so Evaluation/Dispatch never compare vectors produced by two different models.
     * @param dimension
     *     Length of the embedding vector, e.g. 384 for all-MiniLM-L6-v2.
     */
    public CaseEmbeddedPayload(List<Double> embedding, Integer dimension, String embeddingModel) {
        super();
        this.embedding = embedding;
        this.dimension = dimension;
        this.embeddingModel = embeddingModel;
    }

    /**
     * The embedding vector for this request's case. Length must match `dimension` and the pgvector column dimension in matching_db.
     * (Required)
     * 
     */
    @JsonProperty("embedding")
    public List<Double> getEmbedding() {
        return embedding;
    }

    /**
     * The embedding vector for this request's case. Length must match `dimension` and the pgvector column dimension in matching_db.
     * (Required)
     * 
     */
    @JsonProperty("embedding")
    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public CaseEmbeddedPayload withEmbedding(List<Double> embedding) {
        this.embedding = embedding;
        return this;
    }

    /**
     * Length of the embedding vector, e.g. 384 for all-MiniLM-L6-v2.
     * (Required)
     * 
     */
    @JsonProperty("dimension")
    public Integer getDimension() {
        return dimension;
    }

    /**
     * Length of the embedding vector, e.g. 384 for all-MiniLM-L6-v2.
     * (Required)
     * 
     */
    @JsonProperty("dimension")
    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public CaseEmbeddedPayload withDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    /**
     * Identifier of the sentence-transformers model used, e.g. 'sentence-transformers/all-MiniLM-L6-v2'. Needed so Evaluation/Dispatch never compare vectors produced by two different models.
     * (Required)
     * 
     */
    @JsonProperty("embedding_model")
    public String getEmbeddingModel() {
        return embeddingModel;
    }

    /**
     * Identifier of the sentence-transformers model used, e.g. 'sentence-transformers/all-MiniLM-L6-v2'. Needed so Evaluation/Dispatch never compare vectors produced by two different models.
     * (Required)
     * 
     */
    @JsonProperty("embedding_model")
    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public CaseEmbeddedPayload withEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CaseEmbeddedPayload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("embedding");
        sb.append('=');
        sb.append(((this.embedding == null)?"<null>":this.embedding));
        sb.append(',');
        sb.append("dimension");
        sb.append('=');
        sb.append(((this.dimension == null)?"<null>":this.dimension));
        sb.append(',');
        sb.append("embeddingModel");
        sb.append('=');
        sb.append(((this.embeddingModel == null)?"<null>":this.embeddingModel));
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
        result = ((result* 31)+((this.embeddingModel == null)? 0 :this.embeddingModel.hashCode()));
        result = ((result* 31)+((this.dimension == null)? 0 :this.dimension.hashCode()));
        result = ((result* 31)+((this.embedding == null)? 0 :this.embedding.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CaseEmbeddedPayload) == false) {
            return false;
        }
        CaseEmbeddedPayload rhs = ((CaseEmbeddedPayload) other);
        return ((((this.embeddingModel == rhs.embeddingModel)||((this.embeddingModel!= null)&&this.embeddingModel.equals(rhs.embeddingModel)))&&((this.dimension == rhs.dimension)||((this.dimension!= null)&&this.dimension.equals(rhs.dimension))))&&((this.embedding == rhs.embedding)||((this.embedding!= null)&&this.embedding.equals(rhs.embedding))));
    }

}
