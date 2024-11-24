package com.tesselslate.pastry.task;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysis;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveStronghold;
import com.tesselslate.pastry.capture.PastryCapture;

public class AnalyzeCapturesTask extends PastryTask<AnalyzeCapturesTask.Result> {
    private final List<ListCapturesTask.Entry> captures;

    private AtomicInteger progress;

    public AnalyzeCapturesTask(List<ListCapturesTask.Entry> captures) {
        super("AnalyzeCapturesTask");

        this.captures = captures;
        this.progress = new AtomicInteger(0);
    }

    @Override
    protected AnalyzeCapturesTask.Result runTask() throws Exception {
        HashSet<Subtask> tasks = new HashSet<>();

        for (ListCapturesTask.Entry capture : this.captures) {
            Subtask task = new Subtask(capture.path);

            tasks.add(task);
            Pastry.TASK_POOL.submit(task);
        }

        PreemptiveAnalysis sum = new PreemptiveAnalysis();
        HashMap<File, Exception> exceptions = new HashMap<>();

        tasks.stream().forEach(task -> {
            Exceptional<PreemptiveAnalysis> result = task.join();

            if (result.hasError()) {
                exceptions.put(task.file, result.getError());
                return;
            }

            assert result.hasValue();
            sum.add(result.getValue());

            this.progress.getAndIncrement();
        });

        return new Result(sum, exceptions);
    }

    public int getProgress() {
        return this.progress.get();
    }

    public static class Result {
        public final PreemptiveAnalysis analysis;
        public final Map<File, Exception> exceptions;

        private Result(PreemptiveAnalysis analysis, Map<File, Exception> exceptions) {
            this.analysis = analysis;
            this.exceptions = exceptions;
        }
    }

    private static class Subtask extends RecursiveTask<Exceptional<PreemptiveAnalysis>> {
        private final File file;

        private Subtask(File file) {
            this.file = file;
        }

        @Override
        protected Exceptional<PreemptiveAnalysis> compute() {
            try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(this.file))) {
                PastryCapture capture = new PastryCapture(input);

                List<PreemptiveStronghold> strongholds = PreemptiveStronghold.readFromCapture(capture);
                PreemptiveAnalysis analysis = new PreemptiveAnalysis();
                strongholds.forEach(stronghold -> analysis.process(stronghold));

                return new Exceptional<>(analysis);
            } catch (Exception e) {
                return new Exceptional<>(e);
            }
        }
    }
}
