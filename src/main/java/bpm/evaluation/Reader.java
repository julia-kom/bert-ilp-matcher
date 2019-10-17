package bpm.evaluation;

import bpm.alignment.Alignment;

import java.io.File;

public interface Reader {
    public Alignment readAlignmentFrom(File file) throws Exception;
    public void writeAlignmentTo(File file, Alignment alignment, String model1, String model2) throws Exception;
}
