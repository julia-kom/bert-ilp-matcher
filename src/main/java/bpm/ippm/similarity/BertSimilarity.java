package bpm.ippm.similarity;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class BertSimilarity implements LabelSimilarity {


    public double sim(String label1, String label2) {

        String csvName = "./eval-data/pnml/sims.csv";

        List<String[]> r = null;
        try (CSVReader reader = new CSVReader(new FileReader(csvName))) {
            r = reader.readAll();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double sim = 0.0;
        for (String[] triple : r) {

            if (triple[0].equals(label1) && triple[1].equals(label2)) {
                sim = Double.parseDouble(triple[2]);
            }
        }
        return sim;
    }
}
