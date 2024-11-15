package com.tesselslate.pastry.capture;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockBox;

/**
 * Provides an abstraction over the string lookup table and allows for writing
 * a series of capture events to an {@link OutputStream}.
 *
 * @see PastryCaptureDictionary
 */
public class PastryCaptureOutputStream extends DataOutputStream {
    private PastryCaptureHeader header;

    private GZIPOutputStream parentOutput;
    private ByteArrayOutputStream buffer;

    public PastryCaptureOutputStream(OutputStream output, List<PastryCaptureEvent> events) throws IOException {
        this(new ByteArrayOutputStream(), output, events);
    }

    private PastryCaptureOutputStream(ByteArrayOutputStream buffer, OutputStream output,
            List<PastryCaptureEvent> events) throws IOException {
        super(buffer);

        this.header = new PastryCaptureHeader(events);

        this.parentOutput = new GZIPOutputStream(output);
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {
        DataOutputStream parentDataOutput = new DataOutputStream(this.parentOutput);

        this.header.write(parentDataOutput);
        this.buffer.writeTo(parentDataOutput);

        parentDataOutput.close();
        super.close();
    }

    /**
     * Writes the values of the {@code blockBox} to the output stream.
     *
     * @param blockBox The block box to write
     */
    public void writeBlockBox(BlockBox blockBox) throws IOException {
        super.writeInt(blockBox.minX);
        super.writeInt(blockBox.minY);
        super.writeInt(blockBox.minZ);
        super.writeInt(blockBox.maxX);
        super.writeInt(blockBox.maxY);
        super.writeInt(blockBox.maxZ);
    }

    /**
     * Looks up or allocates a spot for {@code string} in the string lookup
     * table and writes the ID to the output stream.
     *
     * @param string The string to reference
     */
    public void writeString(@Nullable String string) throws IOException {
        super.writeInt(string != null ? this.header.dictionary.get(string) : 0);
    }
}
