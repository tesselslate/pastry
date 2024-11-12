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

// Recording contains a list of frames present from a Pastry recording.
type Recording []Frame

// Frame contains the data for a single frame of a Pastry recording.
type Frame struct {
	Num               int32      // Frame number
	TotalPercentages  [3]float64 // Total gameRenderer percentages
	ParentPercentages [3]float64 // Parent gameRenderer percentages

	Entities      map[string]int32 // Counts of visible entities by type
	BlockEntities map[string]int32 // Counts of visible blockentities by type
}

// NewRecording attempts to read a Pastry recording from r.
func NewRecording(r io.Reader) (Recording, error) {
	gzipReader, err := gzip.NewReader(r)
	if err != nil {
		return nil, fmt.Errorf("new gzip reader: %w", err)
	}

	raw, err := io.ReadAll(gzipReader)
	if err != nil {
		return nil, fmt.Errorf("decompress: %w", err)
	}

	byteReader := bytes.NewReader(raw)

	var frames Recording
	for {
		frame, err := readFrame(byteReader)
		if err == io.EOF {
			break
		} else if err != nil {
			return nil, fmt.Errorf("read frame %d: %w", len(frames), err)
		}

		frames = append(frames, frame)
	}

	return frames, nil
}

// readFrame attempts to read a single frame of a Pastry recording from r.
func readFrame(r io.Reader) (Frame, error) {
	var err error
	f := Frame{}

	f.Num, err = readInt32(r)
	if err == io.EOF {
		return Frame{}, io.EOF
	} else if err != nil {
		return Frame{}, fmt.Errorf("read frame number: %w", err)
	}

	for i := range 3 {
		f.TotalPercentages[i], err = readFloat64(r)
		if err != nil {
			return Frame{}, fmt.Errorf("read total percentage %d: %w", i, err)
		}
	}

	for i := range 3 {
		f.ParentPercentages[i], err = readFloat64(r)
		if err != nil {
			return Frame{}, fmt.Errorf("read parent percentage %d: %w", i, err)
		}
	}

	f.Entities, err = readMap(r)
	if err != nil {
		return Frame{}, fmt.Errorf("read entities map: %w", err)
	}

	f.BlockEntities, err = readMap(r)
	if err != nil {
		return Frame{}, fmt.Errorf("read block entities map: %w", err)
	}

	return f, nil
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

// readMap reads a string-to-int32 map from r, formatted as a signed 32-bit
// integer containing the number of entries followed by the list of entries.
//
// Each entry is formatted as a string (int32, content) followed by an int32
// to hold the value.
func readMap(r io.Reader) (map[string]int32, error) {
	numEntries, err := readInt32(r)
	if err != nil {
		return nil, err
	}
	if numEntries == 0 {
		return nil, nil
	}

	m := make(map[string]int32, numEntries)
	for range numEntries {
		key, err := readString(r)
		if err != nil {
			return nil, err
		}

		val, err := readInt32(r)
		if err != nil {
			return nil, err
		}

		m[key] = val
	}
	return m, nil
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
