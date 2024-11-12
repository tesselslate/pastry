package com.tesselslate.pastry.capture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

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

    public String get(int id) throws IndexOutOfBoundsException {
        return this.strings.get(id - 1);
    }

    public int get(String string) {
        return this.map.computeIntIfAbsent(string, str -> {
            this.strings.add(string);
            return this.strings.size();
        });
    }

    public void write(DataOutputStream output) throws IOException {
        output.writeInt(this.strings.size());

        for (String string : this.strings) {
            byte[] stringBytes = string.getBytes();

            output.writeInt(stringBytes.length);
            output.write(stringBytes);
        }
    }
}
