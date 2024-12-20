package com.example.usimaps.map;

/**
 * Vertex Type:
 * ROOM, CONNECTION, DOOR, STAIR, ELEVATOR, OUTSIDE
 * The outside type is special as the distance to it is always 0, it is supposed to connect to doors
 * and cannot be used as an intermediate point.
 */
public enum VertexType
{
    ROOM, CONNECTION, DOOR, STAIR, ELEVATOR, OUTSIDE
}
