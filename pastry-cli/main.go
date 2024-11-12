package main

import (
	"fmt"
	"os"
)

func main() {
	if len(os.Args) < 2 {
		printHelp()
	}

	if err := run(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func printHelp() {
	fmt.Println("pastry-cli RECORDING")
	os.Exit(1)
}

func printFrame(f Frame) {
	sections := []struct {
		name  string
		index int
	}{
		{"BE", BlockEntities},
		{"E", Entities},
		{"U", Unspecified},
	}

	fmt.Printf("frame %d\n", f.Num)
	for _, section := range sections {
		fmt.Printf("\t%40s: %5.2f%% %5.2f%%\n", section.name, f.ParentPercentages[section.index], f.TotalPercentages[section.index])
	}
	for blockentity, count := range f.BlockEntities {
		fmt.Printf("\t%40s: %d\n", blockentity, count)
	}
	for entity, count := range f.Entities {
		fmt.Printf("\t%40s: %d\n", entity, count)
	}
}

func run() error {
	file, err := os.Open(os.Args[1])
	if err != nil {
		return fmt.Errorf("open recording: %w", err)
	}
	defer file.Close()

	recording, err := NewRecording(file)
	if err != nil {
		return fmt.Errorf("read recording: %w", err)
	}

	for _, frame := range recording {
		printFrame(frame)
	}

	return nil
}
