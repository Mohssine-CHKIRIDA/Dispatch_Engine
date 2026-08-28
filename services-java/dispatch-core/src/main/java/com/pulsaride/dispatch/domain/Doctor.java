package com.pulsaride.dispatch.domain;

public record Doctor(
                String id,
                String specialty,
                String description,
                boolean available,
                double load,
                double[] embedding) {
}