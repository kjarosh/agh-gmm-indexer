package com.github.kjarosh.agh.pp.graph.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.kjarosh.agh.pp.index.VertexIndex;
import com.github.kjarosh.agh.pp.persistence.PersistenceFactory;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Kamil Jarosz
 */
@EqualsAndHashCode
@ToString
public class Vertex implements Comparable<Vertex> {
    @JsonProperty("id")
    private final VertexId id;
    @JsonProperty("type")
    private final Type type;

    @JsonIgnore
    private final VertexIndex index;

    public Vertex(
            @JsonProperty("id") VertexId id,
            @JsonProperty("type") Type type) {
        this.id = id;
        this.type = type;
        this.index = PersistenceFactory.getInstance()
                .createIndex(id.toString());
    }

    public VertexId id() {
        return id;
    }

    public Type type() {
        return type;
    }

    public VertexIndex index() {
        return index;
    }

    @Override
    public int compareTo(Vertex o) {
        return id.compareTo(o.id);
    }

    public enum Type {
        @JsonProperty("provider")
        PROVIDER,
        @JsonProperty("space")
        SPACE,
        @JsonProperty("group")
        GROUP,
        @JsonProperty("user")
        USER,
    }
}
