package Tracker;

/**
 * Tracker represents a torrent-tracker.
 * Torrent-tracker stores information about shared files.
 * For each file Torrent-tracker stores the ID of file
 * and a list of clients that have this file.
 */
public interface Tracker {
    int PORT = 8081;
    int start();
    int stop();
}
