package Tracker;

import Common.SerializationException;
import Common.SocketIOException;

import java.io.File;

/**
 * Tracker represents a torrent-tracker.
 * Torrent-tracker stores information about shared files.
 * For each file Torrent-tracker stores the ID of file
 * and a list of clients that have this file.
 */
public interface Tracker {
    void start(int port, File rootDir) throws SocketIOException, SerializationException;

    void stop() throws SocketIOException, SerializationException;
}
