package main

import (
	"fmt"
	"time"
)

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
		case *OptionsEvent:
			dumpOptionsEvent(event)
		case *DimensionEvent:
			dumpDimensionEvent(event)
		case *SysinfoEvent:
			dumpSysinfoEvent(event)
		case *ProfilerEvent:
			dumpProfilerEvent(event)
		case *GamemodeEvent:
			dumpGamemodeEvent(event)
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
	fmt.Printf("Recorded at:    %s\n", record.CaptureTime.Format(time.RFC3339))

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

func dumpDimensionEvent(e *DimensionEvent) {
	fmt.Printf("\t%53s: %s\n", "Dimension", e.Name)
}

func dumpEntityEvent(e *EntityEvent) {
	fmt.Printf("\t%53s (%8.2f %6.2f %8.2f)\n", e.Name, e.Pos[0], e.Pos[1], e.Pos[2])
}

func dumpFrameEvent(e *FrameEvent) {
	fmt.Printf("\t%53s: %.2f %.2f %.2f (%.2f %.2f)\n", "Camera", e.Pos[0], e.Pos[1], e.Pos[2], e.Pitch, e.Yaw)
	fmt.Printf("\t%53s: %d\n", "Total Entities", e.TotalEntities)

	fmt.Printf("-------------------------------- %d ms since capture start\n", e.TimeElapsed)
}

func dumpGamemodeEvent(e *GamemodeEvent) {
	var mode string
	switch e.Mode {
	case 0:
		mode = "survival"
	case 1:
		mode = "creative"
	case 2:
		mode = "adventure"
	case 3:
		mode = "spectator"
	default:
		mode = "unknown"
	}

	fmt.Printf("\t%53s: %s\n", "Gamemode", mode)
}

func dumpOptionsEvent(e *OptionsEvent) {
	fmt.Printf("\t%53s: %d chunks\n", "Render Distance", e.RenderDistance)
	fmt.Printf("\t%53s: %d%%\n", "Entity Distance", e.EntityDistance)
	fmt.Printf("\t%53s: %d\n", "FOV", e.Fov)
	fmt.Printf("\t%53s: %d x %d\n", "Window Size", e.Width, e.Height)
	fmt.Printf("\t%53s: %t\n", "Hitboxes", e.Hitboxes)
	fmt.Printf("\t%53s: %t\n", "Chunk Borders", e.ChunkBorders)
	fmt.Printf("\t%53s: %t\n", "Cull State Renderer", e.CullState)
}

func dumpProfilerEvent(e *ProfilerEvent) {
	sections := []struct {
		name  string
		index int
	}{
		{"Block Entities", BlockEntities},
		{"Entities", Entities},
		{"Unspecified", Unspecified},
		{"Destroy Progress", DestroyProgress},
		{"Prepare", Prepare},
	}

	for _, section := range sections {
		fmt.Printf("\t%53s: %5.2f%% %5.2f%%\n", section.name, e.Parent[section.index], e.Total[section.index])
	}

	fmt.Printf("\t%53s: %5.2f%%\n", "gameRenderer Total", e.SumTotal)
}

func dumpSysinfoEvent(e *SysinfoEvent) {
	fmt.Println("-------- SYSTEM INFO")

	fmt.Printf("GL Vendor:              %s\n", e.GlVendor)
	fmt.Printf("GL Renderer:            %s\n", e.GlRenderer)
	fmt.Printf("GL Version:             %s\n", e.GlVersion)
	fmt.Printf("CPU:                    %s\n", e.Cpu)
	fmt.Printf("JVM Version:            %s\n", e.JvmVersion)
	fmt.Printf("JVM Arguments:          %s\n", e.JvmArgs)
	fmt.Printf("Max Memory:             %d bytes\n", e.MaxMemory)
	fmt.Printf("Available Processors:   %d\n", e.AvailableProcessors)

	fmt.Println("-------- END SYSTEM INFO")
}

func dumpWorldLoadEvent(e *WorldLoadEvent) {
	fmt.Printf("\t%53s: %s\n", "World Name", e.Name)
	fmt.Printf("\t%53s: %d\n", "World Seed", e.Seed)
}
