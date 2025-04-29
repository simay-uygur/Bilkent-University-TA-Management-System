package com.example.entity.General;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AcademicLevelType {
    BS,
    PHD,
    MS,
    UNKNOWN;

    @JsonCreator
    public static AcademicLevelType fromString(String key) {
        if (key == null) {
            return UNKNOWN;
        }
        try {
            return AcademicLevelType.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}

//more flexible bs, ms unknown can be written (with lowercase)
