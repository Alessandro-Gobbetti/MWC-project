package com.example.usimaps.map;

import static android.content.Context.MODE_PRIVATE;

import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
public class Graph implements Serializable {
    // map of vertices to edges
    private Map<Vertex, Set<Edge>> map;
    private String mapName;

    /**
     * Constructor for Graph
     */
    public Graph() {
        this.map = new HashMap<>();
    }

    /**
     * Constructor for Graph
     * @param mapName the name of the map
     */
    public Graph(String mapName) {
        this.map = new HashMap<>();
        this.mapName = mapName;
    }

    /**
     * get the map name
     */
    public String getMapName() {
        return mapName;
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

    public List<String> getFloorNames() {
        Set<String> floorNames = new HashSet<>();
        for (Vertex v : map.keySet()) {
            floorNames.add(ordinal(v.getFloor()));
        }
        return new ArrayList<>(floorNames);
    }



    /**
     * Get the edges of the graph
     * @return the set of edges
     */
    public Set<Edge> getEdges() {
        Set<Edge> edges = new HashSet<>();
        for (Vertex v : map.keySet()) {
            edges.addAll(map.get(v));
        }
        return edges;
    }

    public Set<String> getEdgeNames() {
        Set<String> edgeNames = new HashSet<>();
        for (Edge e : getEdges()) {
            edgeNames.add(e.getName());
        }
        return edgeNames;
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
                // ignore outside vertices
                if (e.getDestination().getType() == VertexType.OUTSIDE && destination.getType() != VertexType.OUTSIDE) {
                    continue;
                }
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
        double x1 = e.getSource().getLatitude();
        double y1 = e.getSource().getLongitude();
        double x2 = e.getDestination().getLatitude();
        double y2 = e.getDestination().getLongitude();
        double x3 = v.getLatitude();
        double y3 = v.getLongitude();

        // calculate the closest point
        double u = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1)) / (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        double x = x1 + u * (x2 - x1);
        double y = y1 + u * (y2 - y1);

        // create new connection vertex lying on the edge
        Vertex connectionVertex = new Vertex(e.getName() + "-" + v.getName(), VertexType.CONNECTION, x, y, v.getFloor());
        addVertex(connectionVertex);

        // compute the new weights
        double weight1 = computeDistance(e.getSource(), connectionVertex);
        double weight2 = computeDistance(connectionVertex, e.getDestination());

        // create new edges
        addEdge(e.getSource(), connectionVertex, weight1, e.getName());
        addEdge(connectionVertex, e.getDestination(), weight2, e.getName());

        // create new edge from edge to vertex
        double weight = computeDistance(v, connectionVertex);
        addEdge(connectionVertex, v, weight, e.getName() + "-" + v.getName());
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
                if (edge.getName().equals(edgeName) && !edges.contains(edge)) {
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
            double x1 = edge.getSource().getLatitude();
            double y1 = edge.getSource().getLongitude();
            double x2 = edge.getDestination().getLatitude();
            double y2 = edge.getDestination().getLongitude();
            double x3 = v.getLatitude();
            double y3 = v.getLongitude();

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

    /**
     * Returns the signed angle between three vertices
     * @param v1: the first vertex
     * @param v2: the second vertex
     * @param v3: the third vertex
     * @return the signed angle between the three vertices
     */
    public double getSignedAngle(Vertex v1, Vertex v2, Vertex v3) {
        double x1 = v1.getLongitude();
        double y1 = v1.getLatitude();
        double x2 = v2.getLongitude();
        double y2 = v2.getLatitude();
        double x3 = v3.getLongitude();
        double y3 = v3.getLatitude();

        double a = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        double b = Math.pow(x2 - x3, 2) + Math.pow(y2 - y3, 2);
        double c = Math.pow(x3 - x1, 2) + Math.pow(y3 - y1, 2);

        double angle = Math.acos((a + b - c) / Math.sqrt(4 * a * b));
        double cross = (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
        if (cross < 0) {
            angle = -angle;
        }
        return angle;
    }

    /**
     * Computes the distance between two vertices given their latitude and longitude
     * @param v1: the first vertex
     * @param v2: the second vertex
     * @return the distance between the two vertices
     */
    public double computeDistance(Vertex v1, Vertex v2) {
        if (v1.getType() == VertexType.OUTSIDE || v2.getType() == VertexType.OUTSIDE) {
            return 0;
        }

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
     * Returns true if the angle is right
     * @param angle the angle
     * @return true if the angle is right
     */
    private boolean isRight(final double angle) {
        return angle > 180 || angle < 0;
    }

    /**
     * Rounds the distance to the closest reasonable integer: useful for displaying distance in instructions
     * @param distance the distance
     * @return the rounded distance
     */
    public int roundDistance(final double distance) {
        if (distance < 4) {
            // round to closest integer
            return (int) Math.round(distance);
        } else if (distance < 30) {
            // round to closest 5
            return (int) Math.round(distance / 5) * 5;
        } else if (distance < 100) {
            // round to closest 10
            return (int) Math.round(distance / 10) * 10;
        } else {
            // round to closest 50
            return (int) Math.round(distance / 50) * 50;
        }
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
            instructions.add("You are already at " + path.get(0).getName());
            return new Pair<>(path, instructions);
        }
        if (path.size() == 2) {
            instructions.add(path.get(1) + " is right next to you");
            return new Pair<>(path, instructions);
        }

        // add start instruction
        if (path.get(0).getType() == VertexType.OUTSIDE) {
            instructions.add("Start outside");
        } else {
            instructions.add("Start at " + path.get(0).getName());
        }
        simplifiedPath.add(path.get(0));

        int angle_threshold = 20;

        for (int i = 1; i < path.size() - 1; i++) {
            Vertex v1 = path.get(i - 1);
            Vertex v2 = path.get(i);
            Vertex v3 = path.get(i + 1);
            double angle = Math.toDegrees(getSignedAngle(v1, v2, v3));
            // check if stairs

            // skip vertices if distance is too small
            if (computeDistance(v2, v3) < 1.0) {
                continue;
            }

            if (v2.getType() != VertexType.STAIR && v3.getType() == VertexType.STAIR) {
                // next vertex is a stair
                while (i < path.size() - 2 && path.get(i+1).getType() == VertexType.STAIR) {
                    i++;
                }

                int finalFloor = path.get(i).getFloor();
                boolean up = v2.getFloor() < finalFloor;
                boolean right = isRight(angle);
                String floor = ordinal(finalFloor);

                instructions.add("Take the stairs on the " + (right ? "right" : "left") + " " + (up ? "up" : "down") + " to the " + floor + " floor");
            } else if (v2.getType() == VertexType.STAIR) {
                // skip all next stairs
                while (i < path.size() - 2 && path.get(i+1).getType() == VertexType.STAIR) {
                    i++;
                }
                int finalFloor = path.get(i).getFloor();
                boolean up = v2.getFloor() < finalFloor;
                // compute final floor
                String floor = ordinal(finalFloor);
                instructions.add("Take the stairs " + (up ? "up" : "down") + " to the " + floor + " floor");
            } else if (isRight(angle)) {
                double distance =  computeDistance(v2, v3);
                int roundedDistance = roundDistance(distance);
                instructions.add(getTurnPhrase("right", roundedDistance));
            } else {
                double distance =  computeDistance(v2, v3);
                int roundedDistance = roundDistance(distance);
                instructions.add(getTurnPhrase("left", roundedDistance));
            }

            simplifiedPath.add(v2);
            if (v3.getType() == VertexType.DOOR) {
                // modify the instruction to include the door
                instructions.set(instructions.size() - 1, instructions.get(instructions.size() - 1) + " through the door");
            }

        }
        instructions.add("Destination: " + path.get(path.size() - 1).getName());
        simplifiedPath.add(path.get(path.size() - 1));

        return new Pair<>(simplifiedPath, instructions);
    }

    /**
     * Returns the instructions to follow the path. It picks randomly from a set of predefined instructions to diversify the instructions
     *
     * @param leftRight the direction to turn
     * @param distance the distance to walk
     * @return the instruction
     */
    private String getTurnPhrase(String leftRight, int distance) {
        // list of predefined instructions
        String[] instructions = {
                "Turn " + leftRight + " and walk " + distance + " meters",
                "Take a " + leftRight + " turn and walk " + distance + " meters",
                "Turn " + leftRight + " and continue for " + distance + " meters",
                "Take a " + leftRight + " turn and continue for " + distance + " meters",
                "Turn " + leftRight + " and proceed for " + distance + " meters",
                "Take a " + leftRight + " turn and proceed for " + distance + " meters",
                "Turn " + leftRight + " and go straight for " + distance + " meters",
                "Take a " + leftRight + " turn and go straight for " + distance + " meters",
                "Turn " + leftRight + " and keep going for " + distance + " meters",
                "Take a " + leftRight + " turn and keep going for " + distance + " meters",
                "Turn " + leftRight + " and walk straight for " + distance + " meters",
                "Take a " + leftRight + " turn and walk straight for " + distance + " meters",
                "Turn " + leftRight + " and continue straight for " + distance + " meters",
                "Take a " + leftRight + " turn and continue straight for " + distance + " meters",
                "Turn " + leftRight + " and proceed straight for " + distance + " meters",
                "Take a " + leftRight + " turn and proceed straight for " + distance + " meters",
                "Turn " + leftRight + " and go straight ahead for " + distance + " meters",
                "Take a " + leftRight + " turn and go straight ahead for " + distance + " meters",
                "Turn " + leftRight + " and keep going straight for " + distance + " meters",
                "Take a " + leftRight + " turn and keep going straight for " + distance + " meters",
                "Turn " + leftRight + " and walk straight ahead for " + distance + " meters",
                "Take a " + leftRight + " turn and walk straight ahead for " + distance + " meters",
                "Turn " + leftRight + " and continue straight ahead for " + distance + " meters"
        };
        int random = (int) (Math.random() * instructions.length);
        return instructions[random];
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

    public int ordinalFloorToInt(String floor) {
        if (floor.equals("Ground")) {
            return 0;
        }
        if (floor.endsWith("st")) {
            return Integer.parseInt(floor.substring(0, floor.length() - 2));
        }
        if (floor.endsWith("nd")) {
            return Integer.parseInt(floor.substring(0, floor.length() - 2));
        }
        if (floor.endsWith("rd")) {
            return Integer.parseInt(floor.substring(0, floor.length() - 2));
        }
        return Integer.parseInt(floor.substring(0, floor.length() - 2));
    }

    /**
     * Generates a map of the USI campus
     * @return the graph representing the USI campus
     */
    public Graph generateUSIMap() {
        Graph graph = new Graph("USI Campus EST");
        // create D corridor
        Vertex D0_CorridorEnd = new Vertex("Corridor D0", VertexType.CONNECTION, 46.012324, 8.961444, 0);
        Vertex D0_CorridorStart = new Vertex("Corridor D0", VertexType.CONNECTION, 46.011607, 8.961346, 0);
        graph.addVertex(D0_CorridorEnd);
        graph.addVertex(D0_CorridorStart);
        graph.addEdge(D0_CorridorStart, D0_CorridorEnd, computeDistance(D0_CorridorStart, D0_CorridorEnd), "Corridor D0");

        Vertex D_Door = new Vertex("Door1 Sector D", VertexType.DOOR, 46.011951, 8.961339, 0);
        Vertex D_Door2 = new Vertex("Door2 Sector D", VertexType.DOOR, 46.011815, 8.961308, 0);
        Vertex Stairs_base = new Vertex("Stairs D Base", VertexType.STAIR, 46.011997, 8.961422, 0);
        Vertex Stairs_top = new Vertex("Stairs D Top", VertexType.STAIR, 46.012081,8.961436, 1);

        Vertex D002 = new Vertex("D0.02", VertexType.ROOM, 46.012090, 8.961486, 0);
        Vertex D004 = new Vertex("D0.04", VertexType.ROOM, 46.011884, 8.961442, 0);

        graph.connectVertexToEdgeByName(D_Door, "Corridor D0");
        graph.connectVertexToEdgeByName(D_Door2, "Corridor D0");
        graph.connectVertexToEdgeByName(D002, "Corridor D0");
        graph.connectVertexToEdgeByName(D004, "Corridor D0");
        graph.connectVertexToEdgeByName(Stairs_base, "Corridor D0");

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

        Vertex D115 = new Vertex("D1.15", VertexType.ROOM, 46.011589, 8.961352, 1);
        graph.connectVertexToEdgeByName(D115, "D1 Corridor");

        // corridor C1
        Vertex C1_CorridorEnd = new Vertex("Corridor C1", VertexType.CONNECTION, 46.0123761558387, 8.960759281466686, 1);
        graph.addVertex(C1_CorridorEnd);
        graph.addEdge(D1_CorridorEnd, C1_CorridorEnd, computeDistance(D1_CorridorEnd, C1_CorridorEnd), "C1 Corridor");

        Vertex C1_03 = new Vertex("C1.03", VertexType.ROOM, 46.01239944086005, 8.961342661886274, 1);
        graph.connectVertexToEdgeByName(C1_03, "C1 Corridor");
        Vertex C1_04 = new Vertex("C1.04", VertexType.ROOM, 46.01239944086005, 8.961342661886274, 1);
        graph.connectVertexToEdgeByName(C1_04, "C1 Corridor");

        Vertex Outside = new Vertex("Outside", VertexType.OUTSIDE, 46.0123761558387, 8.960759281466686, 0);
        graph.addVertex(Outside);
        // connect to every vertex of type door
        for (Vertex v : graph.getVertices()) {
            if (v.getType() == VertexType.DOOR) {
                graph.addEdge(Outside, v, computeDistance(Outside, v), "Outside");
            }
        }

        return graph;
    }

    /**
     * Load the graph from a file
     * @param fileIn the file input stream
     * @return the graph
     */
    public static Graph loadGraph(FileInputStream fileIn) {
        try {
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Graph graph = (Graph) objectIn.readObject();
            objectIn.close();
            return graph;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save the graph to a file
     * @param fileout the file output stream
     * @param graph the graph
     * @return true if the graph was saved successfully
     */
    public static boolean saveGraph(FileOutputStream fileout, Graph graph) {
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(fileout);
            objectOut.writeObject(graph);
            objectOut.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Serialize a graph to a byte array
     * @param graph the graph
     * @return the byte array
     */
    public static byte[] serialize(Graph graph) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(graph);
            byte[] employeeAsBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(employeeAsBytes);
            return employeeAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deserialize a graph from a byte array
     * @param data the byte array
     * @return the graph
     */
    public static Graph deserialize(byte[] data) {
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            return (Graph ) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}



