package main

import (
	"fmt"
	"io"
)

// Dump formats and writes a human-readable representation of record to w.
func Dump(record Record, w io.Writer) {
	for _, event := range record.Events {
		switch event := event.(type) {
		case BlockEntityEvent:
			dumpBlockEntityEvent(event)
		case EntityEvent:
			dumpEntityEvent(event)
		case FrameEvent:
			dumpFrameEvent(event)
		}
	}

	fmt.Printf("Record version: %d\n", record.Version)
	fmt.Printf("Dictionary len: %d\n", len(record.Dict))
	fmt.Printf("Event count:    %d\n", len(record.Events))
}

func dumpBlockEntityEvent(e BlockEntityEvent) {
	fmt.Printf("\t%53s (%5d %3d %5d) %s\n", e.Name, e.Pos[0], e.Pos[1], e.Pos[2], e.Data)
}

func dumpEntityEvent(e EntityEvent) {
	fmt.Printf("\t%53s (%8.2f %6.2f %8.2f)\n", e.Name, e.Pos[0], e.Pos[1], e.Pos[2])
}

func dumpFrameEvent(e FrameEvent) {
	sections := []struct {
		name  string
		index int
	}{
		{"BE", BlockEntities},
		{"E", Entities},
		{"U", Unspecified},
	}

	for _, section := range sections {
		fmt.Printf("\t%53s: %5.2f%%\n", section.name, e.Percentages[section.index])
	}
	fmt.Printf("\t%53s: %.2f %.2f %.2f (%.2f %.2f)\n", "Camera", e.Pos[0], e.Pos[1], e.Pos[2], e.Pitch, e.Yaw)

	fmt.Printf("-------------------------------- end of frame %d\n", e.Num)
}
