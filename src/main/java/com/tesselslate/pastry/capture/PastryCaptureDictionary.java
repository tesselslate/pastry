package com.tesselslate.pastry.capture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Contains a list of strings referenced by a {@link PastryCapture}.
 */
public class PastryCaptureDictionary {
    private Object2IntOpenHashMap<String> map;

    private ArrayList<String> strings;

    public PastryCaptureDictionary() {
        this.map = new Object2IntOpenHashMap<>();
        this.strings = new ArrayList<>();
    }

    public PastryCaptureDictionary(DataInputStream input) throws IOException {
        this();

        int size = input.readInt();

        for (int i = 0; i < size; i++) {
            int length = input.readInt();
            String string = new String(input.readNBytes(length));

            this.strings.add(string);
            this.map.put(string, i + 1);
        }
    }

    /**
     * Attempts to lookup a string with the given ID. If the ID is 0,
     * {@code null} will be returned.
     *
     * @param id The ID to l
     * @return The string associated with {@code id}, or {@code null} if {@code id}
     *         is 0
     * @throws IndexOutOfBoundsException If {@code id} is invalid
     */
    public @Nullable String get(int id) throws IndexOutOfBoundsException {
        return this.strings.get(id - 1);
    }

    /**
     * Attempts to find the ID associated with {@code string}. If the string is
     * not present in the lookup table, it is added and a new unique ID is
     * returned.
     *
     * @param string The string to search for
     * @return The ID of {@code string} in the lookup table
     */
    public int get(String string) {
        return this.map.computeIntIfAbsent(string, str -> {
            this.strings.add(string);
            return this.strings.size();
        });
    }

    /**
     * Serializes and writes the string lookup table to {@code output}.
     *
     * @param output The stream to which the lookup table is written
     */
    public void write(DataOutputStream output) throws IOException {
        output.writeInt(this.strings.size());

        for (String string : this.strings) {
            byte[] stringBytes = string.getBytes();

            output.writeInt(stringBytes.length);
            output.write(stringBytes);
        }
    }
}
