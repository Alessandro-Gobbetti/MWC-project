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
     * Returns all the names of vertices in the graph that are not connections
     */
    public List<String> getSearchableNames() {
        List<String> roomNames = new ArrayList<>();
        for (Vertex v : map.keySet()) {
            if (v.getType() != VertexType.CONNECTION) {
                roomNames.add(v.getName());
            }
        }
        return roomNames;
    }

    /**
     * Returns the vertex with the given name, or null if the vertex does not exist
     * @param name: the name of the vertex
     * @return the vertex with the given name, or null if the vertex does not exist
     */
    public Vertex getVertexByName(String name) {
        for (Vertex v : map.keySet()) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
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
    public void addEdge(final Vertex source, final Vertex destination, final double weight, String name) {
        Edge e = new Edge(source, destination, weight, name);
        Objects.requireNonNull(map.get(source)).add(e);
        Edge e2 = new Edge(destination, source, weight, name);
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
    public double getWeight(final Vertex source, final Vertex destination) {
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
    public Pair<List<Vertex>, Double> getShortestPath(final Vertex source, final Vertex destination) {
         // create vertex priority queue
        Map<Vertex, Double> distance = new HashMap<>();
        Map<Vertex, Vertex> previous = new HashMap<>();
        PriorityQueue<Vertex> pq = new PriorityQueue<>(Comparator.comparingDouble(distance::get));

        distance.put(source, 0.0);
        pq.add(source);

        for (Vertex v : map.keySet()) {
            if (!v.equals(source)) {
                previous.put(v, null);
                distance.put(v, Double.MAX_VALUE);
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
                double newDist = distance.get(current) + e.getWeight();
                if (newDist < distance.get(e.getDestination())) {
                    distance.put(e.getDestination(), newDist);
                    previous.put(e.getDestination(), current);
                    pq.add(e.getDestination());
                }
            }
        }

        // check if destination is reachable
        if (!distance.containsKey(destination) || distance.get(destination) == Double.MAX_VALUE) {
            return new Pair<>(new ArrayList<>(), -1.0);
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

    /**
     * Print the graph
     */
    public void print() {
        for (Vertex v : map.keySet()) {
            System.out.println("Vertex: " + v.getName());
            for (Edge e : Objects.requireNonNull(map.get(v))) {
                System.out.println("\t-> " + e.getDestination().getName() + "\tWeight: " + e.getWeight());
            }
        }
    }

    /**
     * Connects a vertex to an edge in the map by splitting the edge in the closest point to the new vertex.
     * The new vertex is added to the graph and connected to the edge.
     * The edge is replaced by two new edges connecting the source and destination vertices a new connection vertex,
     * allowing the new vertex to be connected to the graph.
     * @param v: the vertex
     * @param e: the edge
     */
    public void connectVertexToEdge(Vertex v, Edge e) {
        // assert e in map
        assert map.containsKey(e.getSource());

        // add the vertex to the graph if it is not already in the graph
        if (!map.containsKey(v))
            addVertex(v);

        // get the closest point to the vertex on the edge
        double x1 = e.getSource().getLongitude();
        double y1 = e.getSource().getLatitude();
        double x2 = e.getDestination().getLongitude();
        double y2 = e.getDestination().getLatitude();
        double x3 = v.getLongitude();
        double y3 = v.getLatitude();

        // calculate the closest point
        double u = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1)) / (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        double x = x1 + u * (x2 - x1);
        double y = y1 + u * (y2 - y1);

        // create new connection vertex lying on the edge
        Vertex connectionVertex = new Vertex( v.getName() + " (Connection)", VertexType.CONNECTION, x, y, v.getFloor());
        addVertex(connectionVertex);

        // compute the new weights
        double weight1 = computeDistance(e.getSource(), connectionVertex);
        double weight2 = computeDistance(connectionVertex, e.getDestination());

        // create new edges
        addEdge(e.getSource(), connectionVertex, weight1, e.getName());
        addEdge(connectionVertex, e.getDestination(), weight2, e.getName());

        // create new edge from edge to vertex
        double weight = computeDistance(v, connectionVertex);
        addEdge(connectionVertex, v, weight1, e.getName() + "-" + v.getName());

        // remove old edge
        map.get(e.getSource()).remove(e);
        map.get(e.getDestination()).remove(e);
    }

    /**
     * Connects a vertex to the closest edge with the given name
     * @param v: the vertex
     * @param edgeName: the name of the edge
     */
    public void connectVertexToEdgeByName(Vertex v, String edgeName) {
        // search for all edges with the given name
        List<Edge> edges = new ArrayList<>();
        for (Vertex vertex : map.keySet()) {
            for (Edge edge : map.get(vertex)) {
                if (edge.getName().equals(edgeName) && !edges.contains(edge) && edge.getDestination().getFloor() == v.getFloor() && edge.getSource().getFloor() == v.getFloor()) {
                    edges.add(edge);
                }
            }
        }

        if (edges.isEmpty()) {
            return;
        }

        if (edges.size() == 1) {
            connectVertexToEdge(v, edges.get(0));
            return;
        }

        // find the closest edge
        Edge closestEdge = null;
        double minDistance = Double.MAX_VALUE;
        for (Edge edge : edges) {
            double x1 = edge.getSource().getLongitude();
            double y1 = edge.getSource().getLatitude();
            double x2 = edge.getDestination().getLongitude();
            double y2 = edge.getDestination().getLatitude();
            double x3 = v.getLongitude();
            double y3 = v.getLatitude();

            // calculate the closest point
            double u = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1)) / (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            double x = x1 + u * (x2 - x1);
            double y = y1 + u * (y2 - y1);

            double distance = Math.sqrt(Math.pow(x - x3, 2) + Math.pow(y - y3, 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestEdge = edge;
            }
        }

        // connect the vertex to the closest edge
        connectVertexToEdge(v, closestEdge);
    }



    /**
     * Returns the angle between three vertices
     * @param v1: the first vertex
     * @param v2: the second vertex
     * @param v3: the third vertex
     * @return the angle between the three vertices
     */
    public double getAngle(Vertex v1, Vertex v2, Vertex v3) {
        double x1 = v1.getLongitude();
        double y1 = v1.getLatitude();
        double x2 = v2.getLongitude();
        double y2 = v2.getLatitude();
        double x3 = v3.getLongitude();
        double y3 = v3.getLatitude();

        double a = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        double b = Math.sqrt(Math.pow(x3 - x2, 2) + Math.pow(y3 - y2, 2));
        double c = Math.sqrt(Math.pow(x3 - x1, 2) + Math.pow(y3 - y1, 2));

        return Math.acos((Math.pow(a, 2) + Math.pow(b, 2) - Math.pow(c, 2)) / (2 * a * b));
    }

    public double computeDistance(Vertex v1, Vertex v2) {
        double x1 = v1.getLongitude();
        double y1 = v1.getLatitude();
        double x2 = v2.getLongitude();
        double y2 = v2.getLatitude();


        // calculate the distance in meters: https://en.wikipedia.org/wiki/Haversine_formula
        double R = 6371000; // radius of Earth in meters
        double lat1 = Math.toRadians(y1);
        double lat2 = Math.toRadians(y2);
        double deltaLat = Math.toRadians(y2 - y1);
        double deltaLon = Math.toRadians(x2 - x1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Generates a map of the USI campus
     * @return the graph representing the USI campus
     */
    public Graph generateUSIMap() {
        Graph graph = new Graph();
        // create D corridor
        Vertex D0_CorridorEnd = new Vertex("Corridor D0", VertexType.CONNECTION, 46.012324, 8.961444, 0);
        Vertex D0_CorridorStart = new Vertex("Corridor D0", VertexType.CONNECTION, 46.011607, 8.961346, 0);
        graph.addVertex(D0_CorridorEnd);
        graph.addVertex(D0_CorridorStart);
        graph.addEdge(D0_CorridorStart, D0_CorridorEnd, computeDistance(D0_CorridorStart, D0_CorridorEnd), "D Corridor");

        Vertex D_Door = new Vertex("Door Sector D", VertexType.DOOR, 46.011951, 8.961339, 0);
        Vertex D_Door2 = new Vertex("Door Sector D", VertexType.DOOR, 46.011815, 8.961308, 0);
        Vertex Stairs_base = new Vertex("Stairs D Base", VertexType.CONNECTION, 46.011997, 8.961422, 0);
        Vertex Stairs_top = new Vertex("Stairs D Top", VertexType.CONNECTION, 46.012091, 8.961406, 1);

        Vertex D002 = new Vertex("D0:02", VertexType.ROOM, 46.012090, 8.961486, 0);
        Vertex D004 = new Vertex("D0:04", VertexType.ROOM, 46.011884, 8.961442, 0);

        graph.addVertex(D_Door);
        graph.addVertex(D_Door2);
        graph.addVertex(Stairs_base);
        graph.addVertex(Stairs_top);
        graph.addEdge(Stairs_top, Stairs_base, computeDistance(Stairs_top, Stairs_base), "Stairs D");

        graph.addVertex(D002);
        graph.addVertex(D004);

        graph.connectVertexToEdgeByName(D_Door, "D Corridor");
        graph.connectVertexToEdgeByName(D_Door2, "D Corridor");
        graph.connectVertexToEdgeByName(Stairs_base, "D Corridor");
        graph.connectVertexToEdgeByName(D002, "D Corridor");
        graph.connectVertexToEdgeByName(D004, "D Corridor");


        // First floor
        // Corridor D1
        Vertex D1_CorridorEnd = new Vertex("Corridor D0", VertexType.CONNECTION, 46.012324, 8.961444, 1);
        Vertex D1_CorridorStart = new Vertex("Corridor D0", VertexType.CONNECTION, 46.011607, 8.961346, 1);
        graph.addVertex(D1_CorridorEnd);
        graph.addVertex(D1_CorridorStart);
        graph.addEdge(D1_CorridorStart, D1_CorridorEnd, computeDistance(D1_CorridorStart, D1_CorridorEnd), "D1 Corridor");
        // connect to stairs
        graph.connectVertexToEdgeByName(Stairs_top, "D1 Corridor");

        Vertex D115 = new Vertex("D1:15", VertexType.ROOM, 46.011589, 8.961352, 1);
        graph.addVertex(D115);
        graph.connectVertexToEdgeByName(D115, "D1 Corridor");

        // corridor C1
        Vertex C1_CorridorEnd = new Vertex("Corridor C1", VertexType.CONNECTION, 46.0123761558387, 8.960759281466686, 1);
        graph.addVertex(C1_CorridorEnd);
        graph.addEdge(D1_CorridorEnd, C1_CorridorEnd, computeDistance(D1_CorridorEnd, C1_CorridorEnd), "C1 Corridor");

        Vertex D1_03 = new Vertex("C1:03", VertexType.ROOM, 46.01239944086005, 8.961342661886274, 1);
        graph.addVertex(D1_03);
        graph.connectVertexToEdgeByName(D1_03, "C1 Corridor");
        Vertex D1_04 = new Vertex("C1:04", VertexType.ROOM, 46.01239944086005, 8.961342661886274, 1);
        graph.addVertex(D1_04);

        return graph;
    }
}



