package com.tesselslate.pastry.capture.events;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public final String glVendor;

    @NotNull
    public final String glRenderer;

    @NotNull
    public final String glVersion;

    @NotNull
    public final String cpu;

    @NotNull
    public final String jvmVersion;

    @NotNull
    public final String jvmArgs;

    public final long maxMemory;
    public final int availableProcessors;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(this.glVendor);
        result = prime * result + Objects.hashCode(this.glRenderer);
        result = prime * result + Objects.hashCode(this.glVersion);
        result = prime * result + Objects.hashCode(this.cpu);
        result = prime * result + Objects.hashCode(this.jvmVersion);
        result = prime * result + Objects.hashCode(this.jvmArgs);
        result = prime * result + (int) (this.maxMemory ^ (this.maxMemory >>> 32));
        result = prime * result + this.availableProcessors;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PastryCaptureSysinfoEvent)) {
            return false;
        } else {
            PastryCaptureSysinfoEvent other = (PastryCaptureSysinfoEvent) obj;

            return (this.glVendor.equals(other.glVendor)) && (this.glRenderer.equals(other.glRenderer))
                    && (this.glVersion.equals(other.glVersion)) && (this.cpu.equals(other.cpu))
                    && (this.jvmVersion.equals(other.jvmVersion)) && (this.jvmArgs.equals(other.jvmArgs))
                    && (this.maxMemory == other.maxMemory) && (this.availableProcessors == other.availableProcessors);
        }
    }
}
