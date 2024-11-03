package com.example.usimaps.map;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

import kotlin.Pair;

/**
 * Graph class
 * Represents a graph with vertices and edges
 */
public class Graph {
    // map of vertices to edges
    private Map<Vertex, Set<Edge>> map;

    /**
     * Constructor for Graph
     */
    public Graph() {
        this.map = new HashMap<>();
    }

    /**
     * Add a vertex to the graph
     * @param v: the vertex to add
     */
    public void addVertex(final Vertex v) {
        map.put(v, new HashSet<>());
    }

    /**
     * Add an undirected edge to the graph
     * @param source: the source vertex
     * @param destination: the destination vertex
     * @param weight: the weight of the edge
     */
    public void addEdge(final Vertex source, final Vertex destination, final int weight) {
        Edge e = new Edge(source, destination, weight);
        Objects.requireNonNull(map.get(source)).add(e);
        Edge e2 = new Edge(destination, source, weight);
        Objects.requireNonNull(map.get(destination)).add(e2);
    }

    /**
     * Get the edges of a vertex
     * @param v: the vertex
     * @return the set of edges
     */
    public Set<Edge> getEdges(final Vertex v) {
        return map.get(v);
    }

    /**
     * Get the vertices of the graph
     * @return the list of vertices
     */
    public List<Vertex> getVertices() {
        return new ArrayList<>(map.keySet());
    }

    /**
     * Get the weight of an edge
     * @param source: the source vertex
     * @param destination: the destination vertex
     * @return the weight of the edge
     */
    public int getWeight(final Vertex source, final Vertex destination) {
        Set<Edge> edges = map.get(source);
        for (Edge e : edges) {
            if (e.getDestination().equals(destination)) {
                return e.getWeight();
            }
        }
        return -1;
    }

    /**
     * Get the shortest path between two vertices
     * Returns the shortest path and the weight of the path
     *
     * @param source: the source vertex
     * @param destination: the destination vertex
     * @return the shortest path and the weight of the path
     */
    public Pair<List<Vertex>, Integer> getShortestPath(final Vertex source, final Vertex destination) {
         // create vertex priority queue
        Map<Vertex, Integer> distance = new HashMap<>();
        Map<Vertex, Vertex> previous = new HashMap<>();
        PriorityQueue<Vertex> pq = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        distance.put(source, 0);
        pq.add(source);

        for (Vertex v : map.keySet()) {
            if (!v.equals(source)) {
                previous.put(v, null);
                distance.put(v, Integer.MAX_VALUE);
            }
        }

        while (!pq.isEmpty()) {
            Vertex current = pq.poll();
            if (current.equals(destination)) {
                break;
            }
            // check if current vertex has edges
            if (map.get(current) == null || map.get(current).isEmpty()) {
                continue;
            }

            for (Edge e : map.get(current)) {
                int newDist = distance.get(current) + e.getWeight();
                if (newDist < distance.get(e.getDestination())) {
                    distance.put(e.getDestination(), newDist);
                    previous.put(e.getDestination(), current);
                    pq.add(e.getDestination());
                }
            }
        }

        // check if destination is reachable
        if (!distance.containsKey(destination) || distance.get(destination) == Integer.MAX_VALUE) {
            return new Pair<>(new ArrayList<>(), -1);
        }

        // construct path
        List<Vertex> path = new ArrayList<>();
        Vertex current = destination;
        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        // reverse the path
        List<Vertex> reversed = new LinkedList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            reversed.add(path.get(i));
        }
        return new Pair<>(reversed, distance.get(destination));
    }

    public void print() {
        for (Vertex v : map.keySet()) {
            System.out.println("Vertex: " + v.getName());
            for (Edge e : map.get(v)) {
                System.out.println("\t-> " + e.getDestination().getName() + "\tWeight: " + e.getWeight());
            }
        }
    }
}



