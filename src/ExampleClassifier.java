
import weka.core.Instances;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import weka.classifiers.functions.MultilayerPerceptron;


public class ExampleClassifier {

    public static void main(String[] args) throws Exception {
    	
    	ArffLoader m_Loader = new ArffLoader();
    	m_Loader.setSource(new File("examples/200hz-3class-gyro.arff"));
        Instances data = m_Loader.getDataSet();

        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        MultilayerPerceptron rbf; 
        rbf = (MultilayerPerceptron)read("models/2p1-gyro-mlp.model");
        
        rbf.export();
                
        /*      
        float success = 0;
        
        for(int i = 0; i < data.numInstances(); i++)
        {
        	if (rbf.classifyInstance(data.instance(i)) == data.instance(i).value(data.classIndex()))
        	{
        		success += 1;
        	}
        }
        
        System.out.println(success / data.numInstances());
        */
    }
    
    /**
     * deserializes from the given stream and returns the object from it.
     * 
     * @param stream the stream to deserialize from
     * @return       the deserialized object
     * @throws Exception if deserialization fails
     */
    public static Object read(String filename) throws Exception
    {
    	ObjectInputStream ois = new ObjectInputStream
        (
            new BufferedInputStream
            (
                new FileInputStream(filename)
            )
        );
  	    Object result = ois.readObject();

	    ois.close();
      
	    return result;
    }
}

