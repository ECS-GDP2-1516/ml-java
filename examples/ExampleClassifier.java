package re.dan.gdp.weka;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import weka.classifiers.Classifier;


public class ExampleClassifier {

    public static void main(String...args) throws Exception {
        DataSource source = new DataSource("/change/me/to/point/at/the/file.arff");
        Instances data = source.getDataSet();

        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        Classifier rbf; 
        rbf = (Classifier)SerializationHelper.read("/change/me/to/point/at/the/model.model");

        for(int i = 0; i < data.numInstances(); i++) {
            System.out.println(rbf.classifyInstance(data.instance(i)));
        }
    }
}

