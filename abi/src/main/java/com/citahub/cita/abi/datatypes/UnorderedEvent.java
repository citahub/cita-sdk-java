package com.citahub.cita.abi.datatypes;

import java.util.ArrayList;
import java.util.List;

import com.citahub.cita.abi.TypeReference;

public class UnorderedEvent {
    private class EventType {
        public boolean indexed;
        public TypeReference<Type> type;
        public int seqNum;
    }

    private String name;
    private List<EventType> params;

    public UnorderedEvent(String name) {
        this.name = name;
        this.params = new ArrayList<>();
    }

    public void add(boolean indexed, TypeReference type) {
        EventType eventType = new EventType();
        eventType.indexed = indexed;
        eventType.type = type;
        eventType.seqNum = this.params.size();
        this.params.add(eventType);
    }

    public String getName() {
        return name;
    }

    public List<TypeReference<Type>> getParameters() {
        List<TypeReference<Type>> list = new ArrayList<>();
        for (EventType eventType : this.params) {
            list.add(eventType.type);
        }
        return list;
    }

    public List<TypeReference<Type>> getIndexedParameters() {
        List<TypeReference<Type>> list = new ArrayList<>();
        for (EventType eventType : this.params) {
            if (eventType.indexed) {
                list.add(eventType.type);
            }
        }
        return list;
    }

    public List<Integer> getIndexedParametersSeq() {
        List<Integer> list = new ArrayList<>();
        for (EventType eventType : this.params) {
            if (eventType.indexed) {
                list.add(eventType.seqNum);
            }
        }
        return list;
    }

    public List<TypeReference<Type>> getNonIndexedParameters() {
        List<TypeReference<Type>> list = new ArrayList<>();
        for (EventType eventType : this.params) {
            if (!eventType.indexed) {
                list.add(eventType.type);
            }
        }
        return list;
    }

    public List<Integer> getNonIndexedParametersSeq() {
        List<Integer> list = new ArrayList<>();
        for (EventType eventType : this.params) {
            if (!eventType.indexed) {
                list.add(eventType.seqNum);
            }
        }
        return list;
    }
}
