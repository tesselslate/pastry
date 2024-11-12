package main

import (
	"fmt"
	"io"
)

// Runs writes a list of consecutive frame runs in record to w.
func Runs(record Record, w io.Writer) {
	for i, run := range record.Runs() {
		fmt.Fprintf(
			w,
			"Run %d: frame %d-%d (len: %d)\n",
			i+1,
			run[0].Num,
			run[len(run)-1].Num,
			len(run),
		)
	}
}
