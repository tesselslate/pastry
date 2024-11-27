package com.tesselslate.pastry.capture.structure;

import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a single {@link StructureStart} from in the world.
 */
public class PastryCaptureStructure {
    public BlockBox boundingBox;
    public String name;

    public List<PastryCaptureStructurePiece> pieces;

    public PastryCaptureStructure(StructureStart<?> structureStart) {
        this.boundingBox = structureStart.getBoundingBox();
        this.name = structureStart.getFeature().getName();

        this.pieces = new ArrayList<>();
        for (StructurePiece piece : structureStart.getChildren()) {
            this.pieces.add(new PastryCaptureStructurePiece(piece));
        }
    }

    public PastryCaptureStructure(PastryCaptureInputStream input) throws IOException {
        this.boundingBox = input.readBlockBox();
        this.name = input.readString();

        this.pieces = new ArrayList<>();

        int size = input.readInt();

        for (int i = 0; i < size; i++) {
            this.pieces.add(new PastryCaptureStructurePiece(input));
        }
    }

    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeBlockBox(this.boundingBox);
        output.writeString(this.name);

        output.writeInt(this.pieces.size());

        for (PastryCaptureStructurePiece piece : this.pieces) {
            piece.write(output);
        }
    }
}
