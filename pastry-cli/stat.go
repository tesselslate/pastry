package main

import (
	"cmp"
	"fmt"
	"io"
	"math"
	"slices"
)

// Spike represents a valid preemptive spike which has been verified by
// [filterSpikes].
type Spike Run

// SpikeStats contains statistics about the gameRenderer profiling measurements
// during a [Spike].
type SpikeStats struct {
	Max [3]float64
	Min [3]float64
	Avg [3]float64
}

// StatByBlockEntities writes statistics for all valid preemptive spikes in
// record, split by the number of visible block entities.
func StatByBlockEntities(record Record, w io.Writer) {
	spikes := filterSpikes(record)

	stats := make(map[int][]SpikeStats)
	for _, spike := range spikes {
		numBlockEntities := len(spike.BlockEntities())
		stat := spike.PieStats()

		stats[numBlockEntities] = append(stats[numBlockEntities], stat)
	}

	mapIter(stats, func(num int, stats []SpikeStats) {
		stat := condense(stats)

		fmt.Fprintf(w, "---- %3d block entities: (sample size: %d)\n", num, len(stats))
		fmt.Fprint(w, "\t\tBE\tE\tU\n")
		fmt.Fprintf(w, "\tmin:\t%5.2f%%\t%5.2f%%\t%5.2f%%\n", stat.Min[0], stat.Min[1], stat.Min[2])
		fmt.Fprintf(w, "\tmax:\t%5.2f%%\t%5.2f%%\t%5.2f%%\n", stat.Max[0], stat.Max[1], stat.Max[2])
		fmt.Fprintf(w, "\tavg:\t%5.2f%%\t%5.2f%%\t%5.2f%%\n", stat.Avg[0], stat.Avg[1], stat.Avg[2])
	})
}

// StatByEntities writes statistics for all valid preemptive spikes in record,
// split by the number of visible entities.
func StatByEntities(record Record, w io.Writer) {
	spikes := filterSpikes(record)

	stats := make(map[int][]SpikeStats)
	for _, spike := range spikes {
		numEntities := len(spike.Entities())
		stat := spike.PieStats()

		stats[numEntities] = append(stats[numEntities], stat)
	}

	mapIter(stats, func(num int, stats []SpikeStats) {
		stat := condense(stats)

		fmt.Fprintf(w, "---- %3d entities: (sample size: %d)\n", num, len(stats))
		fmt.Fprint(w, "\t\tBE\tE\tU\n")
		fmt.Fprintf(w, "\tmin:\t%5.2f%%\t%5.2f%%\t%5.2f%%\n", stat.Min[0], stat.Min[1], stat.Min[2])
		fmt.Fprintf(w, "\tmax:\t%5.2f%%\t%5.2f%%\t%5.2f%%\n", stat.Max[0], stat.Max[1], stat.Max[2])
		fmt.Fprintf(w, "\tavg:\t%5.2f%%\t%5.2f%%\t%5.2f%%\n", stat.Avg[0], stat.Avg[1], stat.Avg[2])
	})
}

// BlockEntities returns the set of visible block entities present in s.
func (s Spike) BlockEntities() map[string]int32 {
	return s[0].BlockEntities
}

// Entities returns the set of visible entities present in s.
func (s Spike) Entities() map[string]int32 {
	return s[0].Entities
}

// PieStats returns statistics about the values presented on the pie chart
// during s.
func (s Spike) PieStats() SpikeStats {
	var (
		minima = [3]float64{math.MaxFloat64, math.MaxFloat64, math.MaxFloat64}
		maxima = [3]float64{-math.MaxFloat64, -math.MaxFloat64, -math.MaxFloat64}
		sum    = [3]float64{}
	)

	for _, f := range s {
		for i := range 3 {
			minima[i] = min(minima[i], f.ParentPercentages[i])
			maxima[i] = max(maxima[i], f.ParentPercentages[i])
			sum[i] += f.ParentPercentages[i]
		}
	}

	l := float64(len(s))
	return SpikeStats{
		Max: maxima,
		Min: minima,
		Avg: [3]float64{sum[0] / l, sum[1] / l, sum[2] / l},
	}
}

// condense calculates the minima, maxima, and averages of the spikes within
// stats and returns the results.
func condense(stats []SpikeStats) SpikeStats {
	var (
		minima = [3]float64{math.MaxFloat64, math.MaxFloat64, math.MaxFloat64}
		maxima = [3]float64{-math.MaxFloat64, -math.MaxFloat64, -math.MaxFloat64}
		sum    = [3]float64{}
	)

	for _, spike := range stats {
		for i := range 3 {
			minima[i] = min(minima[i], spike.Min[i])
			maxima[i] = max(maxima[i], spike.Max[i])
			sum[i] += spike.Avg[i]
		}
	}

	l := float64(len(stats))
	return SpikeStats{
		Max: maxima,
		Min: minima,
		Avg: [3]float64{sum[0] / l, sum[1] / l, sum[2] / l},
	}
}

// filterSpikes returns a list of all frame runs in record which are valid
// preemptive spikes. "Valid" preemptive spikes meet the following criteria:
//
//  1. All frames present within the run have a visible silverfish spawner.
//  2. All frames present within the run have the same set of visible entities.
//  3. All frames present within the run have the same set of visible block
//     entities.
func filterSpikes(record Record) []Spike {
	runs := record.Runs()

	var spikes []Spike
	for _, run := range runs {
		matchEntities := run[0].Entities
		matchBlockEntities := run[0].BlockEntities

		valid := run.All(func(f Frame) bool {
			return mapEqual(f.Entities, matchEntities) &&
				mapEqual(f.BlockEntities, matchBlockEntities) &&
				f.BlockEntities[SilverfishSpawner] > 0
		})

		if valid {
			spikes = append(spikes, Spike(run))
		}
	}

	return spikes
}

// mapEqual returns whether or not a and b are equal (have an identical set of
// key/value pairs).
func mapEqual(a map[string]int32, b map[string]int32) bool {
	if len(a) != len(b) {
		return false
	}

	for k, v := range a {
		bv, ok := b[k]
		if !ok || v != bv {
			return false
		}
	}

	return true
}

// mapIter iterates over m in sorted order, calling f for each key/value pair in
// m.
func mapIter[K cmp.Ordered, V any](m map[K]V, f func(K, V)) {
	keys := make([]K, 0, len(m))
	for k := range m {
		keys = append(keys, k)
	}

	slices.Sort(keys)
	for _, k := range keys {
		f(k, m[k])
	}
}
