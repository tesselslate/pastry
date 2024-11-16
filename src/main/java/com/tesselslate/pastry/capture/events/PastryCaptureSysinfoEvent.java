package com.tesselslate.pastry.capture.events;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import com.mojang.blaze3d.platform.GlDebugInfo;
import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

/**
 * Contains information about the user's hardware and JVM configuration.
 *
 * @since format V7
 */
public class PastryCaptureSysinfoEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.SYSINFO;

    public String glVendor;
    public String glRenderer;
    public String glVersion;
    public String cpu;

    public String jvmVersion;
    public String jvmArgs;

    public long maxMemory;
    public int availableProcessors;

    public PastryCaptureSysinfoEvent() {
        this.glVendor = GlDebugInfo.getVendor();
        this.glRenderer = GlDebugInfo.getRenderer();
        this.glVersion = GlDebugInfo.getVersion();
        this.cpu = GlDebugInfo.getCpuInfo();

        this.jvmVersion = Runtime.version().toString();
        this.jvmArgs = String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments());

        this.maxMemory = Runtime.getRuntime().maxMemory();
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
    }

    public PastryCaptureSysinfoEvent(PastryCaptureInputStream input) throws IOException {
        this.glVendor = input.readString();
        this.glRenderer = input.readString();
        this.glVersion = input.readString();
        this.cpu = input.readString();

        this.jvmVersion = input.readString();
        this.jvmArgs = input.readString();

        this.maxMemory = input.readLong();
        this.availableProcessors = input.readInt();
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeString(this.glVendor);
        output.writeString(this.glRenderer);
        output.writeString(this.glVersion);
        output.writeString(this.cpu);

        output.writeString(this.jvmVersion);
        output.writeString(this.jvmArgs);

        output.writeLong(this.maxMemory);
        output.writeInt(this.availableProcessors);
    }
}
