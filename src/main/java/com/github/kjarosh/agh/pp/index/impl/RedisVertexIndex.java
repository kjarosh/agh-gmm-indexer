package com.github.kjarosh.agh.pp.index.impl;

import com.github.kjarosh.agh.pp.graph.model.VertexId;
import com.github.kjarosh.agh.pp.index.EffectiveVertex;
import com.github.kjarosh.agh.pp.index.VertexIndex;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public class RedisVertexIndex implements VertexIndex {
    private final RedissonClient redisson;

    private final String prefix;

    public RedisVertexIndex(RedissonClient redisson, String prefix) {
        this.redisson = redisson;
        this.prefix = prefix;
    }

    private String keyEffectiveParents() {
        return prefix + "/effective-parents";
    }

    private String keyEffectiveParents(VertexId vertex) {
        return keyEffectiveParents() + "/" + vertex;
    }

    private String keyEffectiveChildren() {
        return prefix + "/effective-children";
    }

    private String keyEffectiveChildren(VertexId vertex) {
        return keyEffectiveChildren() + "/" + vertex;
    }

    @Override
    public RSet<VertexId> getEffectiveChildrenSet() {
        return redisson.getSet(keyEffectiveChildren());
    }

    @Override
    public RSet<VertexId> getEffectiveParentsSet() {
        return redisson.getSet(keyEffectiveParents());
    }

    @Override
    public EffectiveVertex getOrAddEffectiveChild(VertexId id, Runnable createListener) {
        boolean created = getEffectiveChildrenSet().tryAdd(id);
        if (created) {
            createListener.run();
        }
        return new RedisEffectiveVertex(redisson, keyEffectiveChildren(id));
    }

    @Override
    public EffectiveVertex getOrAddEffectiveParent(VertexId id, Runnable createListener) {
        boolean created = getEffectiveParentsSet().tryAdd(id);
        if (created) {
            createListener.run();
        }
        return new RedisEffectiveVertex(redisson, keyEffectiveParents(id));
    }

    @Override
    public Optional<EffectiveVertex> getEffectiveParent(VertexId id) {
        if (getEffectiveParentsSet().contains(id)) {
            return Optional.of(new RedisEffectiveVertex(redisson, keyEffectiveParents(id)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<EffectiveVertex> getEffectiveChild(VertexId id) {
        if (getEffectiveChildrenSet().contains(id)) {
            return Optional.of(new RedisEffectiveVertex(redisson, keyEffectiveChildren(id)));
        }
        return Optional.empty();
    }

    @Override
    public void removeEffectiveParent(VertexId subjectId) {
        getEffectiveParentsSet().remove(subjectId);
        redisson.getBucket(keyEffectiveParents(subjectId)).delete();
    }

    @Override
    public void removeEffectiveChild(VertexId subjectId) {
        getEffectiveChildrenSet().remove(subjectId);
        redisson.getBucket(keyEffectiveChildren(subjectId)).delete();
    }

    @Override
    public Map<VertexId, EffectiveVertex> getEffectiveChildren() {
        RMap<VertexId, EffectiveVertex> map = redisson.getMap(keyEffectiveChildren());
        map.loadAll(true, 2);
        return map;
    }

    @Override
    public Map<VertexId, EffectiveVertex> getEffectiveParents() {
        RMap<VertexId, EffectiveVertex> map = redisson.getMap(keyEffectiveParents());
        map.loadAll(true, 2);
        return map;
    }
}
