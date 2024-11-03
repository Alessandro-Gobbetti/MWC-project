package com.example.usimaps.map;

public class Vertex {

    private String name;
    private VertexType type;
    private int longitude;
    private int latitude;
    private int floor;

    public Vertex(String name, VertexType type, int longitude, int latitude, int floor) {
        this.name = name;
        this.type = type;
        this.longitude = longitude;
        this.latitude = latitude;
        this.floor = floor;
    }

    public String getName() {
        return name;
    }

    public VertexType getType() {
        return type;
    }

    public int getLongitude() {
        return longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public int getFloor() {
        return floor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(VertexType type) {
        this.type = type;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vertex vertex = (Vertex) obj;
        return name.equals(vertex.name);
    }
}
