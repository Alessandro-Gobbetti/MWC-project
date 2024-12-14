package com.example.usimaps.map;

public class Vertex {

    private String name;
    private VertexType type;
    private double latitude;
    private double longitude;
    private int floor;
    private String imagepath;

    public Vertex(String name, VertexType type, double latitude, double longitude, int floor) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.floor = floor;

        // image paths array
        String[] images = new String[] {"/storage/emulated/0/Android/data/com.example.usimaps/files/Pictures/IMG_1732881224591.jpg", "/storage/emulated/0/Android/data/com.example.usimaps/files/Pictures/IMG_1732881794105.jpg","/storage/emulated/0/Android/data/com.example.usimaps/files/Pictures/IMG_1732881831511.jpg"};
        int random = (int) (Math.random() * images.length);
        this.imagepath = images[random];

    }

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
