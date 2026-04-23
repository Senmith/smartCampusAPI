/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.campus.models;

import com.campus.models.Room;
import com.campus.models.Sensor;
import com.campus.models.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // Storage for Rooms: Key is Room ID (e.g., "LIB-301")
    public static Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Storage for Sensors: Key is Sensor ID (e.g., "TEMP-001")
    public static Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Storage for Readings: Maps a Sensor ID to its list of historical readings [cite: 139]
    public static Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();
    
    // Optional: Static block to add dummy data for testing
    static {
        Room demoRoom = new Room();
        demoRoom.setId("LIB-301");
        demoRoom.setName("Library Quiet Study");
        demoRoom.setCapacity(20);
        rooms.put(demoRoom.getId(), demoRoom);
    }
}
