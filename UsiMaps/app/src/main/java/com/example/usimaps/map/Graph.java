package com.example.usimaps.map;

import androidx.annotation.NonNull;

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
            if (v.getType() != VertexType.CONNECTION && v.getType() != VertexType.STAIR && v.getType() != VertexType.ELEVATOR) {
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
            if (v.getType() != VertexType.CONNECTION) {
                System.out.println(v.getName());
                for (Edge e : Objects.requireNonNull(map.get(v))) {
                    System.out.println("\t-> " + e.getDestination().getName() + "\tWeight: " + e.getWeight());
                }
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
    public void connectVertexToEdge(Vertex v, @NonNull Edge e) {
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
        Vertex connectionVertex = new Vertex(e.getName().toUpperCase() + "-" + v.getName(), VertexType.CONNECTION, x, y, v.getFloor());
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

        double a = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        double b = Math.pow(x2 - x3, 2) + Math.pow(y2 - y3, 2);
        double c = Math.pow(x3 - x1, 2) + Math.pow(y3 - y1, 2);

        return Math.acos((a + b - c) / Math.sqrt(4 * a * b));
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
     * Simplifies a path by removing vertices with an angle smaller than the threshold angle
     * @param path the path to simplify
     * @param threshold_angle_degrees the threshold angle in degrees +- 180 degrees
     * @return the simplified path
     */
    public List<Vertex> simplifyPath(List<Vertex> path, double threshold_angle_degrees) {
        List<Vertex> simplifiedPath = new ArrayList<>();
        if (path.size() < 3) {
            return path;
        }
        simplifiedPath.add(path.get(0));
        for (int i = 1; i < path.size() - 1; i++) {
            Vertex v1 = path.get(i - 1);
            Vertex v2 = path.get(i);
            Vertex v3 = path.get(i + 1);
            double angle = Math.toDegrees(getAngle(v1, v2, v3));
            // check if the angle is in a range 180 +- threshold_angle_degrees
            if (Math.abs(angle - 180) >= threshold_angle_degrees) {
                simplifiedPath.add(v2);
            }
        }
        simplifiedPath.add(path.get(path.size() - 1));
        return simplifiedPath;
    }

    /**
     * Returns the instructions to follow the path
     * Such as: "Follow the corridor", "Turn left", "Take the stairs on the right up to the first floor"
     * @param path the path
     * @return the instructions
     */
    public Pair<List<Vertex>,List<String>> toSimpleInstructions(List<Vertex> path) {
        List<String> instructions = new ArrayList<>();
        List<Vertex> simplifiedPath = new ArrayList<>();
        path = simplifyPath(path, 20);

        if (path.size() == 1) {
            instructions.add("You are already at " + path.get(0));
            return new Pair<>(path, instructions);
        }
        if (path.size() == 2) {
            instructions.add(path.get(1) + " is right next to you");
            return new Pair<>(path, instructions);
        }

        // add start instruction
        instructions.add("Start at " + path.get(0).getName());

        int angle_threshold = 20;

        for (int i = 1; i < path.size() - 1; i++) {
            Vertex v1 = path.get(i - 1);
            Vertex v2 = path.get(i);
            Vertex v3 = path.get(i + 1);
            double angle = Math.toDegrees(getAngle(v1, v2, v3));
            // check if stairs

            if (v2.getName().contains("Stairs")) {
                boolean up = v2.getFloor() < v3.getFloor();

                // skip all next stairs
                while (i < path.size() - 1 && path.get(i).getName().contains("Stairs")) {
                    i++;
                }

                String upString = up ? "up" : "down";
                // compute final floor
                String floor = ordinal(v2.getFloor());
                instructions.add("Take the stairs on the " + (angle < 90 ? "right" : "left") + " " + (up ? "up" : "down") + " to the " + floor + " floor");
            } else if (angle < 180-angle_threshold && angle > 0) {
                instructions.add("Turn right");
            } else if ((angle > 180+angle_threshold && angle < 360-angle_threshold) || angle < 0) {
                instructions.add("Turn left");
            } else {
                instructions.add("Continue straight");
            }
            simplifiedPath.add(v2);
        }
        instructions.add("Destination: " + path.get(path.size() - 1).getName());
        simplifiedPath.add(path.get(path.size() - 1));
        return new Pair<>(simplifiedPath, instructions);
    }

    private String ordinal(int i) {
        if (i==0) {
            return "Ground";
        }
        int mod100 = i % 100;
        int mod10 = i % 10;
        if(mod10 == 1 && mod100 != 11) {
            return i + "st";
        } else if(mod10 == 2 && mod100 != 12) {
            return i + "nd";
        } else if(mod10 == 3 && mod100 != 13) {
            return i + "rd";
        } else {
            return i + "th";
        }
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

        Vertex D_Door = new Vertex("Door1 Sector D", VertexType.DOOR, 46.011951, 8.961339, 0);
        Vertex D_Door2 = new Vertex("Door2 Sector D", VertexType.DOOR, 46.011815, 8.961308, 0);
        Vertex Stairs_base = new Vertex("Stairs D Base", VertexType.STAIR, 46.011997, 8.961422, 0);
        Vertex Stairs_top = new Vertex("Stairs D Top", VertexType.STAIR, 46.012081,8.961436, 1);

        Vertex D002 = new Vertex("D0:02", VertexType.ROOM, 46.012090, 8.961486, 0);
        Vertex D004 = new Vertex("D0:04", VertexType.ROOM, 46.011884, 8.961442, 0);

        graph.connectVertexToEdgeByName(D_Door, "D Corridor");
        graph.connectVertexToEdgeByName(D_Door2, "D Corridor");
        graph.connectVertexToEdgeByName(D002, "D Corridor");
        graph.connectVertexToEdgeByName(D004, "D Corridor");
        graph.connectVertexToEdgeByName(Stairs_base, "D Corridor");

        graph.addVertex(Stairs_top);
        graph.addEdge(Stairs_top, Stairs_base, computeDistance(Stairs_top, Stairs_base), "Stairs D");


        // First floor
        // Corridor D1
        Vertex D1_CorridorEnd = new Vertex("Corridor D1", VertexType.CONNECTION, 46.012324, 8.961444, 1);
        Vertex D1_CorridorStart = new Vertex("Corridor D1", VertexType.CONNECTION, 46.011607, 8.961346, 1);
        graph.addVertex(D1_CorridorEnd);
        graph.addVertex(D1_CorridorStart);
        graph.addEdge(D1_CorridorStart, D1_CorridorEnd, computeDistance(D1_CorridorStart, D1_CorridorEnd), "D1 Corridor");
        // connect to stairs
        graph.connectVertexToEdgeByName(Stairs_top, "D1 Corridor");

        Vertex D115 = new Vertex("D1:15", VertexType.ROOM, 46.011589, 8.961352, 1);
        graph.connectVertexToEdgeByName(D115, "D1 Corridor");

        // corridor C1
        Vertex C1_CorridorEnd = new Vertex("Corridor C1", VertexType.CONNECTION, 46.0123761558387, 8.960759281466686, 1);
        graph.addVertex(C1_CorridorEnd);
        graph.addEdge(D1_CorridorEnd, C1_CorridorEnd, computeDistance(D1_CorridorEnd, C1_CorridorEnd), "C1 Corridor");

        Vertex C1_03 = new Vertex("C1:03", VertexType.ROOM, 46.01239944086005, 8.961342661886274, 1);
        graph.connectVertexToEdgeByName(C1_03, "C1 Corridor");
        Vertex C1_04 = new Vertex("C1:04", VertexType.ROOM, 46.01239944086005, 8.961342661886274, 1);
        graph.connectVertexToEdgeByName(C1_04, "C1 Corridor");

        return graph;
    }
}



