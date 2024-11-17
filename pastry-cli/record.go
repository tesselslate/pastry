package main

import (
	"bytes"
	"compress/gzip"
	"encoding/binary"
	"fmt"
	"io"
	"math"
	"time"
)

const (
	BlockEntities = 0
	Entities      = 1
	Unspecified   = 2
)

const (
	Frame        = 0
	Entity       = 1
	BlockEntity  = 2
	WorldLoad    = 3
	BlockOutline = 4
	Options      = 5
	Dimension    = 6
	Sysinfo      = 7
	Profiler     = 8
)

const currentVersion = 9

// Record contains the data from a parsed Pastry recording.
type Record struct {
	Version     int32
	CaptureTime time.Time
	Dict        map[int32]string

	Structures []Structure
	Events     []Event
}

// Structure contains information about a single in-world structure and its
// constituent pieces.
type Structure struct {
	Box    Box
	Name   string
	Pieces []StructurePiece
}

// StructurePiece contains information about a single in-world structure piece.
type StructurePiece struct {
	Box      Box
	Type     string
	Rotation int32
}

// Box contains a bounding box whose vertices are all block-aligned.
type Box struct {
	Min [3]int32
	Max [3]int32
}

// Event represents a single event from a Pastry recording.
type Event interface{}

// BlockEntityEvent contains the data for a single block entity event from a
// Pastry recording.
type BlockEntityEvent struct {
	Pos        [3]int32
	Name, Data string
}

// BlockOutlineEvent contains the data for a single block outline event from a
// Pastry recording.
type BlockOutlineEvent struct {
	Pos [3]int32
}

// DimensionEvent contains information about the dimension the player is in.
type DimensionEvent struct {
	Name string
}

// EntityEvent contains the data for a single entity event from a Pastry
// recording.
type EntityEvent struct {
	Pos  [3]float64
	Name string
}

// FrameEvent contains data about the player for a single frame of a Pastry
// recording.
type FrameEvent struct {
	Num        int32      // Frame number
	Pos        [3]float64 // Camera position
	Pitch, Yaw float32    // Camera rotation
}

// OptionsEvent contains various rendering settings for a single frame of a
// Pastry recording.
type OptionsEvent struct {
	RenderDistance int
	EntityDistance int
	Fov            int

	Width, Height int32

	Hitboxes     bool
	ChunkBorders bool
	CullState    bool
}

// ProfilerEvent contains information about the state of the gameRenderer
// profiler results for a single frame of a Pastry recording.
type ProfilerEvent struct {
	Percentages [3]float32 // Parent gameRenderer percentages
}

// SysinfoEvent contains information about the hardware and JVM on which a
// Pastry capture was recorded.
type SysinfoEvent struct {
	GlVendor   string
	GlRenderer string
	GlVersion  string
	Cpu        string

	JvmVersion string
	JvmArgs    string

	MaxMemory           int64
	AvailableProcessors int32
}

// WorldLoadEvent contains metadata about the world which this recording was
// captured in.
type WorldLoadEvent struct {
	Name string
	Seed int64
}

// NewRecord attempts to read a Pastry recording from r.
func NewRecord(r io.Reader) (Record, error) {
	var (
		record      Record
		numEvents   int32
		captureTime int64
	)

	gzipReader, err := gzip.NewReader(r)
	if err != nil {
		return Record{}, fmt.Errorf("new gzip reader: %w", err)
	}

	raw, err := io.ReadAll(gzipReader)
	if err != nil {
		return Record{}, fmt.Errorf("decompress: %w", err)
	}

	byteReader := bytes.NewReader(raw)

	record.Version, err = readInt32(byteReader)
	if err != nil {
		return Record{}, fmt.Errorf("read version: %w", err)
	}
	if record.Version != currentVersion {
		return Record{}, fmt.Errorf("cannot process version %d", record.Version)
	}
	numEvents, err = readInt32(byteReader)
	if err != nil {
		return Record{}, fmt.Errorf("read num events: %w", err)
	}
	captureTime, err = readInt64(byteReader)
	if err != nil {
		return Record{}, fmt.Errorf("read capture time: %w", err)
	}
	record.CaptureTime = time.UnixMilli(captureTime)
	record.Dict, err = readDict(byteReader)
	if err != nil {
		return Record{}, fmt.Errorf("read dict: %w", err)
	}

	structures, err := readStructures(byteReader, record.Dict)
	if err != nil {
		return Record{}, fmt.Errorf("read structures: %w", err)
	}
	record.Structures = structures

	events, err := readEvents(numEvents, byteReader, record.Dict)
	if err != nil {
		return Record{}, fmt.Errorf("read events: %w", err)
	}
	record.Events = events

	return record, nil
}

// readBox reads a bounding box from a Pastry recording.
func readBox(r io.Reader) (Box, error) {
	var (
		coords [6]int32
		err    error
	)

	for i := range coords {
		coords[i], err = readInt32(r)
		if err != nil {
			return Box{}, err
		}
	}

	return Box{
		Min: *(*[3]int32)(coords[0:3]),
		Max: *(*[3]int32)(coords[3:6]),
	}, nil
}

// readDict reads the string dictionary present at the start of a Pastry
// recording.
func readDict(r io.Reader) (map[int32]string, error) {
	numStrings, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	m := make(map[int32]string, numStrings)
	for i := range numStrings {
		str, err := readString(r)
		if err != nil {
			return nil, err
		}

		m[i+1] = str
	}

	return m, nil
}

// readStructure reads a single structure present at the start of a Pastry
// recording.
func readStructure(r io.Reader, dict map[int32]string) (Structure, error) {
	box, err := readBox(r)
	if err != nil {
		return Structure{}, err
	}

	name, err := readStringRef(r, dict)
	if err != nil {
		return Structure{}, err
	}

	numPieces, err := readInt32(r)
	if err != nil {
		return Structure{}, err
	}

	pieces := make([]StructurePiece, 0, numPieces)
	for range numPieces {
		piece, err := readStructurePiece(r, dict)
		if err != nil {
			return Structure{}, err
		}

		pieces = append(pieces, piece)
	}

	return Structure{
		Box:    box,
		Name:   name,
		Pieces: pieces,
	}, nil
}

// readStructurePiece reads a single structure piece present at the start of a
// Pastry recording.
func readStructurePiece(r io.Reader, dict map[int32]string) (StructurePiece, error) {
	typ, err := readStringRef(r, dict)
	if err != nil {
		return StructurePiece{}, err
	}

	box, err := readBox(r)
	if err != nil {
		return StructurePiece{}, err
	}

	rotation, err := readInt32(r)
	if err != nil {
		return StructurePiece{}, err
	}

	return StructurePiece{
		Box:      box,
		Type:     typ,
		Rotation: rotation,
	}, nil
}

// readStructures reads the structure list present at the start of a Pastry
// recording.
func readStructures(r io.Reader, dict map[int32]string) ([]Structure, error) {
	numStructures, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	structures := make([]Structure, 0, numStructures)
	for range numStructures {
		structure, err := readStructure(r, dict)
		if err != nil {
			return nil, err
		}

		structures = append(structures, structure)
	}

	return structures, nil
}

// readBlockEntityEvent reads a single block entity event from a Pastry
// recording.
func readBlockEntityEvent(r io.Reader, dict map[int32]string) (Event, error) {
	var (
		pos [3]int32
		err error
	)

	for i := range pos {
		pos[i], err = readInt32(r)
		if err != nil {
			return nil, err
		}
	}

	name, err := readStringRef(r, dict)
	if err != nil {
		return nil, err
	}

	data, err := readStringRef(r, dict)
	if err != nil {
		return nil, err
	}

	return &BlockEntityEvent{
		Pos:  pos,
		Name: name,
		Data: data,
	}, nil
}

// readBlockOutlineEvent reads a single block outline event from a Pastry
// recording.
func readBlockOutlineEvent(r io.Reader) (Event, error) {
	packedPos, err := readInt64(r)
	if err != nil {
		return nil, err
	}

	// https://wiki.vg/Data_types#Position
	var pos [3]int32
	pos[0] = int32(packedPos >> 38)
	pos[1] = int32(packedPos << 52 >> 52)
	pos[2] = int32(packedPos << 26 >> 38)

	return &BlockOutlineEvent{
		Pos: pos,
	}, nil
}

// readDimensionEvent reads a single dimension event from a Pastry recording.
func readDimensionEvent(r io.Reader, dict map[int32]string) (Event, error) {
	name, err := readStringRef(r, dict)
	if err != nil {
		return nil, err
	}
	return &DimensionEvent{Name: name}, nil
}

// readEntityEvent reads a single entity event from a Pastry recording.
func readEntityEvent(r io.Reader, dict map[int32]string) (Event, error) {
	var (
		pos [3]float64
		err error
	)

	for i := range pos {
		pos[i], err = readFloat64(r)
		if err != nil {
			return nil, err
		}
	}

	name, err := readStringRef(r, dict)
	if err != nil {
		return nil, err
	}

	return &EntityEvent{
		Pos:  pos,
		Name: name,
	}, nil
}

// readFrameEvent reads a single frame event from a Pastry recording.
func readFrameEvent(r io.Reader) (Event, error) {
	var (
		cameraPos [3]float64
		cameraRot [2]float32
	)

	frameNumber, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	for i := range cameraPos {
		cameraPos[i], err = readFloat64(r)
		if err != nil {
			return nil, err
		}
	}

	for i := range cameraRot {
		cameraRot[i], err = readFloat32(r)
		if err != nil {
			return nil, err
		}
	}

	return &FrameEvent{
		Num:   frameNumber,
		Pos:   cameraPos,
		Pitch: cameraRot[0],
		Yaw:   cameraRot[1],
	}, nil
}

// readOptionsEvent reads an options event from a Pastry recording.
func readOptionsEvent(r io.Reader) (Event, error) {
	const (
		maskHitboxes     = 0x01000000
		maskChunkBorders = 0x02000000
		maskCullState    = 0x04000000
	)

	packed, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	renderDistance := int(packed & 0xFF)
	entityDistance := int((packed&0xFF00)>>8) * 10
	fov := int((packed & 0xFF0000) >> 16)

	hitboxes := (packed & maskHitboxes) != 0
	chunkBorders := (packed & maskChunkBorders) != 0
	cullState := (packed & maskCullState) != 0

	width, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	height, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	return &OptionsEvent{
		RenderDistance: renderDistance,
		EntityDistance: entityDistance,
		Fov:            fov,
		Width:          width,
		Height:         height,
		Hitboxes:       hitboxes,
		ChunkBorders:   chunkBorders,
		CullState:      cullState,
	}, nil
}

// readProfilerEvent reads a profiler event from a Pastry recording.
func readProfilerEvent(r io.Reader) (Event, error) {
	var percent [3]float32

	for i := range percent {
		upper, err := readUint8(r)
		if err != nil {
			return nil, err
		}

		lower, err := readUint8(r)
		if err != nil {
			return nil, err
		}

		percent[i] = float32(upper) + float32(lower)/100.0
	}

	return &ProfilerEvent{Percentages: percent}, nil
}

// readSysinfoEvent reads a system info event from a Pastry recording.
func readSysinfoEvent(r io.Reader, dict map[int32]string) (Event, error) {
	var (
		strings [6]string
		err     error
	)

	for i := range strings {
		strings[i], err = readStringRef(r, dict)
		if err != nil {
			return nil, err
		}
	}

	maxMemory, err := readInt64(r)
	if err != nil {
		return nil, err
	}

	availableProcessors, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	return &SysinfoEvent{
		GlVendor:   strings[0],
		GlRenderer: strings[1],
		GlVersion:  strings[2],
		Cpu:        strings[3],

		JvmVersion: strings[4],
		JvmArgs:    strings[5],

		MaxMemory:           maxMemory,
		AvailableProcessors: availableProcessors,
	}, nil
}

// readWorldLoadEvent reads a world load event from a Pastry recording.
func readWorldLoadEvent(r io.Reader, dict map[int32]string) (Event, error) {
	name, err := readStringRef(r, dict)
	if err != nil {
		return nil, err
	}

	seed, err := readInt64(r)
	if err != nil {
		return nil, err
	}

	return &WorldLoadEvent{
		Name: name,
		Seed: seed,
	}, nil
}

// readEvent reads a single event from a Pastry recording.
func readEvent(r io.Reader, dict map[int32]string) (Event, error) {
	eventType, err := readInt32(r)
	if err != nil {
		return nil, err
	}

	switch eventType {
	case Frame:
		return readFrameEvent(r)
	case Entity:
		return readEntityEvent(r, dict)
	case BlockEntity:
		return readBlockEntityEvent(r, dict)
	case WorldLoad:
		return readWorldLoadEvent(r, dict)
	case BlockOutline:
		return readBlockOutlineEvent(r)
	case Options:
		return readOptionsEvent(r)
	case Dimension:
		return readDimensionEvent(r, dict)
	case Sysinfo:
		return readSysinfoEvent(r, dict)
	case Profiler:
		return readProfilerEvent(r)
	default:
		return nil, fmt.Errorf("unknown event type %d", eventType)
	}
}

// readEvents reads a list of events from a Pastry capture.
func readEvents(numEvents int32, r io.Reader, dict map[int32]string) ([]Event, error) {
	var events []Event

	for i := range numEvents {
		event, err := readEvent(r, dict)
		if err != nil {
			return nil, fmt.Errorf("read event %d: %w", i, err)
		}

		events = append(events, event)
	}

	return events, nil
}

// readFloat32 reads a single 32-bit floating point number from r.
func readFloat32(r io.Reader) (float32, error) {
	var buf [4]byte

	if _, err := r.Read(buf[0:4]); err != nil {
		return 0, err
	}

	u32 := binary.BigEndian.Uint32(buf[0:4])
	return math.Float32frombits(u32), nil
}

// readFloat64 reads a single 64-bit floating point number from r.
func readFloat64(r io.Reader) (float64, error) {
	var buf [8]byte

	if _, err := r.Read(buf[0:8]); err != nil {
		return 0, err
	}

	u64 := binary.BigEndian.Uint64(buf[0:8])
	return math.Float64frombits(u64), nil
}

// readInt32 reads a single signed 32-bit integer from r.
func readInt32(r io.Reader) (int32, error) {
	var buf [4]byte

	if _, err := r.Read(buf[0:4]); err != nil {
		return 0, err
	}

	return int32(binary.BigEndian.Uint32(buf[0:4])), nil
}

// readInt64 reads a single signed 64-bit integer from r.
func readInt64(r io.Reader) (int64, error) {
	var buf [8]byte

	if _, err := r.Read(buf[0:8]); err != nil {
		return 0, err
	}

	return int64(binary.BigEndian.Uint64(buf[0:8])), nil
}

// readString reads a single string from r, formatted as a signed 32-bit integer
// containing the string length followed by the contents of the string.
func readString(r io.Reader) (string, error) {
	length, err := readInt32(r)
	if err != nil {
		return "", err
	}

	buf := make([]byte, length)
	if _, err := r.Read(buf); err != nil {
		return "", err
	}

	return string(buf), nil
}

// readStringRef reads a single string reference from r, which consists of a
// 32-bit signed integer index into the recording string dictionary.
func readStringRef(r io.Reader, dict map[int32]string) (string, error) {
	id, err := readInt32(r)
	if err != nil {
		return "", err
	}

	if id == 0 {
		return "", nil
	}

	if id < 0 || int(id) > len(dict) {
		return "", fmt.Errorf("invalid string ref %d", id)
	}
	return dict[id], nil
}

// readUint8 reads a single unsigned 8-bit integer from r.
func readUint8(r io.Reader) (uint8, error) {
	var buf [1]byte

	if _, err := r.Read(buf[0:1]); err != nil {
		return 0, err
	}

	return uint8(buf[0]), nil
}
