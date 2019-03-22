package org.weymouth.book.exploration;

import java.io.File;

/*
 * Used: https://progur.com/2017/06/how-to-create-lstm-rnn-deeplearning4j.html
 * with modifications based on
 *   LSTMCharModellingExample.java
 * Extracted (modified) from example code at:
 *   https://github.com/deeplearning4j/dl4j-examples.git
 *   see: src/main/java/org/deeplearning4j/examples/recurrent/character
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class LSTMDeomReadBook {
	
	public static void main(String[] args) throws Exception {
		(new LSTMDeomReadBook()).exec();
	}

	private void exec() throws FileNotFoundException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("HayslopeGrange-EmmaLeslie.txt").getFile());
		String inputData = IOUtils.toString(new FileInputStream(file), "UTF-8");
		inputData = inputData.substring(1568, 3000);
		
		String validCharacters = 
				"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"\n',.?;()[]{}:!- ";
		
		LSTM.Builder lstmBuilder = new LSTM.Builder();
		lstmBuilder.activation(Activation.TANH);
		lstmBuilder.nIn(validCharacters.length());
		lstmBuilder.nOut(30); // Hidden
		LSTM inputLayer = lstmBuilder.build();

		RnnOutputLayer.Builder outputBuilder = new RnnOutputLayer.Builder()
				.lossFunction(LossFunctions.LossFunction.MSE)
				.activation(Activation.SOFTMAX)
				.nIn(30) // Hidden
				.nOut(validCharacters.length());
		RnnOutputLayer outputLayer = outputBuilder.build();
		
		
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(12345)
				.l2(0.0001)
	            .weightInit(WeightInit.XAVIER)
	            .updater(new Adam(0.005))
				.list()
				.layer(0, inputLayer)
				.layer(1, outputLayer)
	            .backpropType(BackpropType.TruncatedBPTT)
	            .build();

		MultiLayerNetwork network = new MultiLayerNetwork(conf);
		
		network.init();
		
		INDArray inputArray = Nd4j.zeros(1, inputLayer.getNIn(), inputData.length());
		INDArray inputLabels = Nd4j.zeros(1, outputLayer.getNOut(), inputData.length());
		
		// Quote: While using an LSTM, the label for an input value is nothing but the next input value. 
		//     For example, if your training data is the string “abc”, for the input value “a”,
		//     the label will be “b”. Similarly, for the input value “b”, the label will be “c”.
		//     Keeping that in mind, you can populate the INDArray objects using the following code:

		for(int i=0;i<inputData.length() - 1;i++) {
		    int positionInValidCharacters1 = validCharacters.indexOf(inputData.charAt(i));
		    inputArray.putScalar(new int[]{0, positionInValidCharacters1, i}, 1);

		    int positionInValidCharacters2 = validCharacters.indexOf(inputData.charAt(i+1));
		    inputLabels.putScalar(new int[]{0, positionInValidCharacters2, i}, 1);
		}

		DataSet dataSet = new DataSet(inputArray, inputLabels);
		
		// 1000 itterations of 'fit' with a printout of predicted 200 characters, each fit.
		for(int z=0;z<1000;z++) {
		    network.fit(dataSet);

		    //INDArray testInputArray = Nd4j.zeros(inputLayer.getNIn());
		    INDArray testInputArray = Nd4j.zeros(1,inputLayer.getNIn(),1);
		    testInputArray.putScalar(new int[]{0,0,0},1.0);
		    
		    network.rnnClearPreviousState();
		    String output = "";
		    for (int k = 0; k < 200; k++) {
		        INDArray outputArray = network.rnnTimeStep(testInputArray);
		        double maxPrediction = Double.MIN_VALUE;
		        int maxPredictionIndex = -1;
		        for (int i = 0; i < validCharacters.length(); i++) {
		            if (maxPrediction < outputArray.getDouble(i)) {
		                maxPrediction = outputArray.getDouble(i);
		                maxPredictionIndex = i;
		            }
		        }
		        // Concatenate generated character
		        output += validCharacters.charAt(maxPredictionIndex);
			    testInputArray = Nd4j.zeros(1,inputLayer.getNIn(),1);
			    testInputArray.putScalar(new int[]{0,maxPredictionIndex,0},1.0);
		    }
		    System.out.println(z + " > A" + output + "\n----------\n");
		}
	}

}
