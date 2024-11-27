package com.tesselslate.pastry.capture.structure;

import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.io.IOException;

/**
 * Contains information about a single {@link StructurePiece} from an in-world
 * structure.
 */
public class PastryCaptureStructurePiece {
    public StructurePieceType type;
    public BlockBox boundingBox;
    public Direction rotation;

    public PastryCaptureStructurePiece(StructurePiece piece) {
        this.type = piece.getType();
        this.boundingBox = piece.getBoundingBox();
        this.rotation = piece.getFacing();
    }

    /**
     * @throws IndexOutOfBoundsException If the piece type ID is null
     */
    public PastryCaptureStructurePiece(PastryCaptureInputStream input) throws IndexOutOfBoundsException, IOException {
        String typeId = input.readString();
        if (typeId == null) {
            throw new IndexOutOfBoundsException("Cannot create PastryStructurePiece with null type ID");
        }
        this.type = Registry.STRUCTURE_PIECE.get(new Identifier(typeId));

        this.boundingBox = input.readBlockBox();
        this.rotation = Direction.byId(input.readInt());
    }

    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeString(Registry.STRUCTURE_PIECE.getId(this.type).toString());
        output.writeBlockBox(this.boundingBox);
        output.writeInt(this.rotation.getId());
    }
}
