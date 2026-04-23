/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.campus.models;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // Storage for Rooms: Key is Room ID
    public static Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Storage for Sensors: Key is Sensor ID
    public static Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Storage for Readings: Maps a Sensor ID to its list of historical readings
    public static Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();
}
