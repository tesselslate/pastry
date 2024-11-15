package main

import "fmt"

// Dump formats and writes a human-readable representation of record to w.
func Dump(record Record) error {
	for _, event := range record.Events {
		switch event := event.(type) {
		case *BlockEntityEvent:
			dumpBlockEntityEvent(event)
		case *EntityEvent:
			dumpEntityEvent(event)
		case *FrameEvent:
			dumpFrameEvent(event)
		case *WorldLoadEvent:
			dumpWorldLoadEvent(event)
		case *BlockOutlineEvent:
			dumpBlockOutlineEvent(event)
		default:
			return fmt.Errorf("unknown event type %T", event)
		}
	}

	for _, structure := range record.Structures {
		dumpStructure(structure)
	}

	fmt.Printf("Record version: %d\n", record.Version)
	fmt.Printf("Dictionary len: %d\n", len(record.Dict))
	fmt.Printf("Event count:    %d\n", len(record.Events))

	return nil
}

func dumpStructure(s Structure) {
	fmt.Printf(
		"Structure %s: (%d, %d, %d)-(%d, %d, %d)\n\t%d pieces\n",
		s.Name,
		s.Box.Min[0], s.Box.Min[1], s.Box.Min[2],
		s.Box.Max[0], s.Box.Max[1], s.Box.Max[2],
		len(s.Pieces),
	)
}

func dumpBlockEntityEvent(e *BlockEntityEvent) {
	fmt.Printf("\t%53s (%5d %3d %5d) %s\n", e.Name, e.Pos[0], e.Pos[1], e.Pos[2], e.Data)
}

func dumpBlockOutlineEvent(e *BlockOutlineEvent) {
	fmt.Printf("\t%53s (%d %d %d)\n", "Targeted Block", e.Pos[0], e.Pos[1], e.Pos[2])
}

func dumpEntityEvent(e *EntityEvent) {
	fmt.Printf("\t%53s (%8.2f %6.2f %8.2f)\n", e.Name, e.Pos[0], e.Pos[1], e.Pos[2])
}

func dumpFrameEvent(e *FrameEvent) {
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

func dumpWorldLoadEvent(e *WorldLoadEvent) {
	fmt.Printf("\t%53s: %s\n", "World Name", e.Name)
	fmt.Printf("\t%53s: %d\n", "World Seed", e.Seed)
}
