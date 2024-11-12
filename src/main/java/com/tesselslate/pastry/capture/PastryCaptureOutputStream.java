package com.tesselslate.pastry.capture;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.jetbrains.annotations.Nullable;

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

    public void writeString(@Nullable String string) throws IOException {
        super.writeInt(string != null ? this.header.dictionary.get(string) : 0);
    }
}
