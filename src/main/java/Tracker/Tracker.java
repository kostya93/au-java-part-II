package Tracker;

import Common.SocketIOException;

import java.io.File;

/**
 * Tracker represents a torrent-tracker.
 * Torrent-tracker stores information about shared files.
 * For each file Torrent-tracker stores the ID of file
 * and a list of clients that have this file.
 */
public interface Tracker {
    int PORT = 55555;

    void start() throws SocketIOException, SerializationException;

    void stop() throws SocketIOException, SerializationException;
}
