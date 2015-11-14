
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.File;

import weka.classifiers.functions.MultilayerPerceptron;


public class ExampleClassifier {

    public static void main(String[] args) throws Exception {
    	
    	ArffLoader m_Loader = new ArffLoader();
    	m_Loader.setSource(new File("examples/200hz-3class-gyro.arff"));
        Instances data = m_Loader.getDataSet();

        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        MultilayerPerceptron rbf; 
        rbf = (MultilayerPerceptron)SerializationHelper.read("models/2p1-gyro-mlp.model");
        
        float success = 0;
        
        for(int i = 0; i < data.numInstances(); i++)
        {
        	if (rbf.classifyInstance(data.instance(i)) == data.instance(i).value(data.classIndex()))
        	{
        		success += 1;
        	}
        }
        
        System.out.println(success / (float)data.numInstances() * 100.0 + "% success rate");
    }
}

