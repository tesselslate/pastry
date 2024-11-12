package main

import (
	"bytes"
	"compress/gzip"
	"encoding/binary"
	"fmt"
	"io"
	"math"
)

const (
	BlockEntities = 0
	Entities      = 1
	Unspecified   = 2
)

const (
	Frame       = 0
	Entity      = 1
	BlockEntity = 2
)

const (
	SilverfishSpawner = "mob_spawner(entity.minecraft.silverfish)"
)

// Record contains the data from a parsed Pastry recording.
type Record struct {
	Version int32
	Events  []Event
	Dict    map[int32]string
}

// Event represents a single event from a Pastry recording.
type Event interface{}

// BlockEntityEvent contains the data for a single block entity event from a
// Pastry recording.
type BlockEntityEvent struct {
	Pos        [3]int32
	Name, Data string
}

// EntityEvent contains the data for a single entity event from a Pastry
// recording.
type EntityEvent struct {
	Pos  [3]float64
	Name string
}

// FrameEvent contains the data for a single frame of a Pastry recording.
type FrameEvent struct {
	Num         int32      // Frame number
	Pos         [3]float64 // Camera position
	Pitch, Yaw  float32    // Camera rotation
	Percentages [3]float32 // Parent gameRenderer percentages
}

// NewRecord attempts to read a Pastry recording from r.
func NewRecord(r io.Reader) (Record, error) {
	var (
		record    Record
		numEvents int32
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
	numEvents, err = readInt32(byteReader)
	if err != nil {
		return Record{}, fmt.Errorf("read num events: %w", err)
	}
	record.Dict, err = readDict(byteReader)
	if err != nil {
		return Record{}, fmt.Errorf("read dict: %w", err)
	}

	for i := range numEvents {
		event, err := readEvent(byteReader, record.Dict)
		if err != nil {
			return Record{}, fmt.Errorf("read event %d: %w", i, err)
		}

		record.Events = append(record.Events, event)
	}

	return record, nil
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

	return BlockEntityEvent{
		Pos:  pos,
		Name: name,
		Data: data,
	}, nil
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

	return EntityEvent{
		Pos:  pos,
		Name: name,
	}, nil
}

// readFrameEvent reads a single frame event from a Pastry recording.
func readFrameEvent(r io.Reader) (Event, error) {
	var (
		cameraPos [3]float64
		cameraRot [2]float32
		percent   [3]float32
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

	return FrameEvent{
		Num:         frameNumber,
		Pos:         cameraPos,
		Pitch:       cameraRot[0],
		Yaw:         cameraRot[1],
		Percentages: percent,
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
	default:
		return nil, fmt.Errorf("unknown event type %d", eventType)
	}
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
