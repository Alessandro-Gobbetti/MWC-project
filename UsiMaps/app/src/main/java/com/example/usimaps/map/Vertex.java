package com.example.usimaps.map;

import java.io.Serializable;

/**
 * Vertex class
 */
public class Vertex implements Serializable {
    // Vertex attributes
    private String name;
    private VertexType type;
    private double latitude;
    private double longitude;
    private int floor;
    private String imagepath;

    /**
     * Constructor
     * @param name Name
     * @param type Type
     * @param latitude Latitude
     * @param longitude Longitude
     * @param floor Floor
     */
    public Vertex(String name, VertexType type, double latitude, double longitude, int floor) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.floor = floor;
        this.imagepath = "";
    }

    /**
     * Constructor
     * @param name Name
     * @param type Type
     * @param latitude Latitude
     * @param longitude Longitude
     * @param floor Floor
     * @param imagepath Image path
     */
    public Vertex(String name, VertexType type, double latitude, double longitude, int floor, String imagepath) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.floor = floor;
        this.imagepath = imagepath;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public VertexType getType() {
        return type;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getFloor() {
        return floor;
    }

    public String getImagePath() {
        return imagepath;
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

    public void setImagePath(String imagepath) {
        this.imagepath = imagepath;
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
