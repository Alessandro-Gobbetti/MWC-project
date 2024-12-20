package com.example.usimaps.map;

import java.io.Serializable;

/**
 * Edge class
 * Edges connect two vertices and have a weight, which is the distance between the two vertices in meters.
 */
public class Edge implements Serializable {
    // Edge attributes
    private final Vertex source;
    private final Vertex destination;
    private final double weight;
    private String name = "";

    /**
     * Constructor
     * @param source Source vertex
     * @param destination Destination vertex
     * @param weight Weight
     */
    public Edge(Vertex source, Vertex destination, double weight, String name) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
        this.name = name;
    }
    // Getters
    public Vertex getSource() {
        return source;
    }

    public Vertex getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }
}
