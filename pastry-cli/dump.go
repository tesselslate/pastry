package main

import (
	"fmt"
	"io"
)

// Dump formats and writes a human-readable representation of record to w.
func Dump(record Record, w io.Writer) {
	for _, frame := range record {
		dumpFrame(frame, w)
	}
}

// dumpFrame writes a human-readable representation of f to w. Any IO errors
// are discarded.
func dumpFrame(f Frame, w io.Writer) {
	sections := []struct {
		name  string
		index int
	}{
		{"BE", BlockEntities},
		{"E", Entities},
		{"U", Unspecified},
	}

	fmt.Fprintf(w, "-------------------------------------------------- Frame %d\n", f.Num)
	for _, section := range sections {
		fmt.Fprintf(w, "\t%53s: %5.2f%% %5.2f%%\n", section.name, f.ParentPercentages[section.index], f.TotalPercentages[section.index])
	}
	fmt.Fprint(w, "\n")
	for blockentity, count := range f.BlockEntities {
		fmt.Fprintf(w, "\t%50s BE: %2d\n", blockentity, count)
	}
	for entity, count := range f.Entities {
		fmt.Fprintf(w, "\t%50s  E: %2d\n", entity, count)
	}
}
