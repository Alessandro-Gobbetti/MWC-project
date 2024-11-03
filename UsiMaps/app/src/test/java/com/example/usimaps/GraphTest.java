package com.example.usimaps;

import com.example.usimaps.map.Graph;
import com.example.usimaps.map.Vertex;
import com.example.usimaps.map.VertexType;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import kotlin.Pair;

public class GraphTest {

    @Test
    public void testAddVertex() {
        Graph graph = new Graph();
        Vertex vertex = new Vertex("vertex", VertexType.ROOM, 0, 0, 0);
        graph.addVertex(vertex);
        assertTrue(graph.getVertices().contains(vertex));
    }

    @Test
    public void testAddEdge() {
        Graph graph = new Graph();
        Vertex vertex1 = new Vertex("vertex1", VertexType.ROOM, 0, 0, 0);
        Vertex vertex2 = new Vertex("vertex2", VertexType.ROOM, 0, 0, 0);
        graph.addVertex(vertex1);
        graph.addVertex(vertex2);
        graph.addEdge(vertex1, vertex2, 1);
        assertEquals(1, graph.getWeight(vertex1, vertex2));
    }

    @Test
    public void testGetShortestPath() {
        Graph graph = new Graph();
        Vertex vertex1 = new Vertex("vertex1", VertexType.ROOM, 0, 0, 0);
        Vertex vertex2 = new Vertex("vertex2", VertexType.ROOM, 0, 0, 0);
        Vertex vertex3 = new Vertex("vertex3", VertexType.ROOM, 0, 0, 0);
        Vertex vertex4 = new Vertex("vertex4", VertexType.ROOM, 0, 0, 0);
        graph.addVertex(vertex1);
        graph.addVertex(vertex2);
        graph.addVertex(vertex3);
        graph.addVertex(vertex4);
        graph.addEdge(vertex1, vertex2, 1);
        graph.addEdge(vertex1, vertex3, 2);
        graph.addEdge(vertex2, vertex4, 3);
        graph.addEdge(vertex3, vertex4, 4);
        graph.addEdge(vertex4, vertex1, 5);

        // shortest path
        Pair<List<Vertex>, Integer> shortestPath = graph.getShortestPath(vertex1, vertex4);
        List<Vertex> path = shortestPath.getFirst();
        int weight = shortestPath.getSecond();

        // cost of shortest path
        assertEquals(4, weight);
        // number of vertices in shortest path
        assertEquals(3, path.size());
        // vertices in shortest path
        assertEquals(vertex1, path.get(0));
        assertEquals(vertex2, path.get(1));
        assertEquals(vertex4, path.get(2));
    }

    @Test
    public void testGetShortestPathNoPath() {
        Graph graph = new Graph();
        Vertex vertex1 = new Vertex("vertex1", VertexType.ROOM, 0, 0, 0);
        Vertex vertex2 = new Vertex("vertex2", VertexType.ROOM, 0, 0, 0);
        Vertex vertex3 = new Vertex("vertex3", VertexType.ROOM, 0, 0, 0);
        Vertex vertex4 = new Vertex("vertex4", VertexType.ROOM, 0, 0, 0);
        graph.addVertex(vertex1);
        graph.addVertex(vertex2);
        graph.addVertex(vertex3);
        graph.addVertex(vertex4);
        graph.addEdge(vertex1, vertex2, 1);
        graph.addEdge(vertex3, vertex4, 3);

        // shortest path
        Pair<List<Vertex>, Integer> shortestPath = graph.getShortestPath(vertex1, vertex4);
        List<Vertex> path = shortestPath.getFirst();
        int weight = shortestPath.getSecond();

        // cost of shortest path is negative
        assertEquals(-1, weight);
        // number of vertices in shortest path
        assertEquals(0, path.size());
    }

    @Test
    public void testGetShortestPathSameVertex() {
        Graph graph = new Graph();
        Vertex vertex1 = new Vertex("vertex1", VertexType.ROOM, 0, 0, 0);
        graph.addVertex(vertex1);

        Pair<List<Vertex>, Integer> shortestPath = graph.getShortestPath(vertex1, vertex1);
        List<Vertex> path = shortestPath.getFirst();
        int weight = shortestPath.getSecond();

        // cost of shortest path is 0
        assertEquals(0, weight);
        // number of vertices in shortest path
        assertEquals(1, path.size());
    }

    @Test
    public void testGetShortestPathNoVertices() {
        Graph graph = new Graph();
        Vertex vertex1 = new Vertex("vertex1", VertexType.ROOM, 0, 0, 0);
        Vertex vertex2 = new Vertex("vertex2", VertexType.ROOM, 0, 0, 0);

        Pair<List<Vertex>, Integer> shortestPath = graph.getShortestPath(vertex1, vertex2);
        List<Vertex> path = shortestPath.getFirst();
        int weight = shortestPath.getSecond();

        // cost of shortest path is negative
        assertEquals(-1, weight);
        // number of vertices in shortest path
        assertEquals(0, path.size());
    }

}
