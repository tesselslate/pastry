package main

import (
	"fmt"
	"os"
)

func main() {
	if len(os.Args) < 3 {
		printHelp()
	}

	if err := run(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func printHelp() {
	fmt.Print(`
    Usage:
        pastry-cli SUBCOMMAND RECORDING

    Subcommands:
        dump        Prints a formatted representation of the recording to stdout
        runs        Prints a list of frame runs

    `)
	os.Exit(1)
}

func run() error {
	cmd := os.Args[1]
	filePath := os.Args[2]

	file, err := os.Open(filePath)
	if err != nil {
		return fmt.Errorf("open recording: %w", err)
	}
	record, err := NewRecord(file)
	if err != nil {
		return fmt.Errorf("read recording: %w", err)
	}

	switch cmd {
	case "dump":
		Dump(record, os.Stdout)
	case "runs":
		Runs(record, os.Stdout)
	}

	return nil
}
