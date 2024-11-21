package com.example.usimaps.map;

public class Edge {
    private final Vertex source;
    private final Vertex destination;
    private final double weight;
    private String name = "";

    public Edge(Vertex source, Vertex destination, double weight, String name) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
        this.name = name;
    }

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
