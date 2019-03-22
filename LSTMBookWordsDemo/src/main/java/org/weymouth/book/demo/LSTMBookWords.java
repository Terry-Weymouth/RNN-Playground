package org.weymouth.book.demo;

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

public class LSTMBookWords {

	public static void main(String[] args) throws Exception {
		(new LSTMBookWords()).exec();
	}

	private void exec() throws Exception {

		PrepBookWordList helper = new PrepBookWordList();

		System.out.println(helper.wordDictionaryList.size());
		System.out.println(helper.inputWordIndexList.size());

		int inputAndOutputLayersize = helper.wordDictionaryList.size();
		int middleLayerSize = 2 * inputAndOutputLayersize;

		LSTM.Builder lstmBuilder = new LSTM.Builder().activation(Activation.TANH).nIn(inputAndOutputLayersize)
				.nOut(middleLayerSize); // Hidden
		LSTM inputLayer = lstmBuilder.build();

		RnnOutputLayer.Builder outputBuilder = new RnnOutputLayer.Builder().lossFunction(LossFunctions.LossFunction.MSE)
				.activation(Activation.SOFTMAX).nIn(middleLayerSize) // Hidden
				.nOut(inputAndOutputLayersize);
		RnnOutputLayer outputLayer = outputBuilder.build();

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(12345).l2(0.0001)
				.weightInit(WeightInit.XAVIER).updater(new Adam(0.005)).list().layer(0, inputLayer)
				.layer(1, outputLayer).backpropType(BackpropType.TruncatedBPTT).build();

		MultiLayerNetwork network = new MultiLayerNetwork(conf);

		System.out.println("Starting network.init()");
		network.init();
		System.out.println("Done with network.init()");

		int startTestIndex = helper.wordIndexMap.get("."); // start test generated with end of sentence
		int batchSize = 32;

		long start = System.currentTimeMillis();
		for (int z = 0; z < 1000; z++) {
			System.out.println(timeStamp(start) + "Start epoch: " + z);

			DatasetIterator datasetSource = new DatasetIterator(helper.inputWordIndexList, batchSize,
					inputLayer.getNIn());

			while (datasetSource.hasNext()) {
				DataSet dataset = datasetSource.next();
				network.fit(dataset);

				if (z % 10 == 1) {
					System.out.println(timeStamp(start) + z + " Starting listing of example evaluation");
					String output = generateSample(z, network, inputLayer, outputLayer, startTestIndex, start, helper);
					System.out.println(timeStamp(start) + z + " > A" + output + "\n----------\n");
				}
			}
		}

	}

	private String generateSample(int z, MultiLayerNetwork network, LSTM inputLayer, RnnOutputLayer outputLayer,
			int startTestIndex, long start, PrepBookWordList helper) {
		INDArray testInputArray = Nd4j.zeros(1, inputLayer.getNIn(), 1);
		testInputArray.putScalar(new int[] { 0, startTestIndex, 0 }, 1.0);
		network.rnnClearPreviousState();
		String output = "";
		for (int k = 0; k < 50; k++) {
			INDArray outputArray = network.rnnTimeStep(testInputArray);
			double maxPrediction = Double.MIN_VALUE;
			int maxPredictionIndex = -1;

			for (int i = 0; i < outputLayer.getNOut(); i++) {
				if (maxPrediction < outputArray.getDouble(i)) {
					maxPrediction = outputArray.getDouble(i);
					maxPredictionIndex = i;
				}
			}
			// Concatenate generated character
			String word = helper.wordDictionaryList.get(maxPredictionIndex);
			output += " " + word;
			testInputArray = Nd4j.zeros(1, inputLayer.getNIn(), 1);
			testInputArray.putScalar(new int[] { 0, maxPredictionIndex, 0 }, 1.0);
		}
		return output;
	}

	private String timeStamp(long start) {
		long now = System.currentTimeMillis();
		long delta = now - start;
		double seconds = (double) delta / 1000.0;
		return String.format("%f secs: ", seconds);
	}

}
