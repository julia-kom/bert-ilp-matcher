package bpm.matcher;

import bpm.alignment.Result;
import bpm.evaluation.ExecutionTimer;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.File;

public interface MatchingPipeline {
    Result run(File f1, File f2, ExecutionTimer t);

    Result run(File f1, File f2);

    Result run(File n1, File l1, File n2, File l2);

    Result run(File n1, File l1, File n2, File l2, ExecutionTimer timer);

    JSONObject toJSON() throws JSONException;

}

