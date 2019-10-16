package bpm.evaluation;

import bpm.alignment.Alignment;

import java.io.File;

public interface Reader {
    public Alignment getAlignmentFrom(File file);
}
