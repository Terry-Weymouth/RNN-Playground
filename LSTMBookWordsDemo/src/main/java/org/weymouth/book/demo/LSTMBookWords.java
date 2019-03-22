package org.weymouth.book.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import org.w3c.dom.ranges.RangeException;

public class LSTMBookWords {

	public static final String PUNCTUATION = "\\\"',.?;()[]{}:!-" + System.getProperty("line.separator");

	private List<String> inputWordList;
	private Map<String, Integer> wordCountMap;
	private List<String> wordKeyList;
	private Map<String, Integer> wordIndexMap;
	private List<Integer> inputWordIndexList;

	public static void main(String[] args) throws Exception {
		(new LSTMBookWords()).exec();
	}

	private void exec() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("HayslopeGrange-EmmaLeslie.txt").getFile());
		List<String> lines = Files.readAllLines(file.toPath());
		StringBuffer collect = new StringBuffer();
		for (int i = 87; i < 3075; i++) {
			// Note: in this doc, there are no lines ending in a hyphenated (continued) word
			// checked with this code...
			// String line = lines.get(i);
			// if (line.endsWith("-") && ! line.endsWith("--"))
			// 	System.out.println(line);
			collect.append(lines.get(i) + " ");
		}
		String inputData = collect.substring(0, collect.length()-1);
		inputData = whiteSpaceSeperatePunctuationAndCleanData(inputData);
		inputWordList = makeInputWordList(inputData);
		wordCountMap = makePunctuationAndWordCountMap(inputWordList);
		wordKeyList = new ArrayList<String>(wordCountMap.keySet());
		wordIndexMap = makeWordKeyMap(wordKeyList);
		inputWordIndexList = makeInputIndexList(wordIndexMap, inputWordList);
		
//		for (int i = 0; i < inputWordList.size(); i++) {
//			System.out.println(String.format("%s: %d - %s", inputWordList.get(i), inputWordIndexList.get(i), wordKeyList.get(inputWordIndexList.get(i))));
//			if (! inputWordList.get(i).equals(wordKeyList.get(inputWordIndexList.get(i))))
//				throw new Exception( "Unequal workds at index " + i);
//		}
//		System.out.println(wordKeyList.size());
//		System.out.println(inputWordList.size());
//		System.out.println(inputWordIndexList.size());
		
		int inputAndOutputLayersize = wordKeyList.size();
		int middleLayerSize = 2*inputAndOutputLayersize;
		
		LSTM.Builder lstmBuilder = new LSTM.Builder()
				.activation(Activation.TANH)
				.nIn(inputAndOutputLayersize)
				.nOut(middleLayerSize); // Hidden
		LSTM inputLayer = lstmBuilder.build();

		RnnOutputLayer.Builder outputBuilder = new RnnOutputLayer.Builder()
				.lossFunction(LossFunctions.LossFunction.MSE)
				.activation(Activation.SOFTMAX)
				.nIn(middleLayerSize) // Hidden
				.nOut(inputAndOutputLayersize);
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

		System.out.println("Starting network.init()");
		network.init();
		System.out.println("Done with network.init()");
		
		
		int startTestIndex = wordIndexMap.get("."); // start test generated with end of sentence
		int batchSize = 32;
		
		long start = System.currentTimeMillis();
		for(int z=0;z<1000;z++) {
			System.out.println(timeStamp(start) + "Start epoch: " + z);
			
			DatasetIterator datasetSource = new DatasetIterator(batchSize, inputLayer.getNIn());
			while (datasetSource.hasNext()) { 
				DataSet dataset = datasetSource.next();
			    network.fit(dataset);
	
			    INDArray testInputArray = Nd4j.zeros(1,inputLayer.getNIn(),1);
			    testInputArray.putScalar(new int[]{0,startTestIndex,0},1.0);
			    
			    if ((z+1)%20 == 0) {
				    System.out.println(timeStamp(start) + z + " Starting listing of example evaluation");
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
				        String word = wordKeyList.get(maxPredictionIndex);
				        output += " " + word;
					    testInputArray = Nd4j.zeros(1,inputLayer.getNIn(),1);
					    testInputArray.putScalar(new int[]{0,maxPredictionIndex,0},1.0);
				    }
				    System.out.println(timeStamp(start) + z + " > A" + output + "\n----------\n");
			    }
			}
		}

	}

	private String timeStamp(long start) {
		long now = System.currentTimeMillis();
		long delta = now - start;
		double seconds = (double) delta / 1000.0;
		return String.format("%f secs: ", seconds);
	}

	private String whiteSpaceSeperatePunctuationAndCleanData(String inputData) {
		// Somewhat imperfect. For example "son-in-law" becomes 5 words: son - in - law.
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < inputData.length(); i++) {
			String probe = inputData.substring(i, i + 1);
			if (StringUtils.containsAny(PUNCTUATION, probe))
				output.append(" " + probe + " ");
			else
				output.append(probe);
		}
		String ret = output.toString();
		int len = ret.length();
		ret = ret.replaceAll("  ", " ");
		while (len < ret.length()) {
			len = ret.length();
			ret = ret.replaceAll("  ", " ");
		}
		return ret;
	}

	private List<String> makeInputWordList(String input) {
		String[] candidates = input.split(" ");
		List<String> ret = new ArrayList<String>();
		for (String c : candidates) {
			if (c.isEmpty())
				continue;
			ret.add(c);
		}
		return ret;
	}

	private Map<String, Integer> makePunctuationAndWordCountMap(List<String> inputList) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String word : inputList) {
			Integer count = map.get(word);
			if (count == null) {
				count = new Integer(1);
			} else {
				count = new Integer(1 + count.intValue());
			}
			map.put(word, count);
		}
		return map;
	}

	private Map<String, Integer> makeWordKeyMap(List<String> keys) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			map.put(key, i);
		}
		return map;
	}

	private List<Integer> makeInputIndexList(Map<String, Integer> map, List<String> docWords) {
		List<Integer> list = new ArrayList<Integer>();
		for (String word : docWords) {
			list.add(map.get(word));
		}
		return list;
	}
	
	private class DatasetIterator {
		
		long layerSize;
		int batchSize;
		int dataLength;
		List<Integer> indexList;
		
		DatasetIterator(int batchSize, long layerSize) {
			this.layerSize = layerSize;
			this.batchSize = batchSize;
	
			dataLength = inputWordIndexList.size() - 1;
			System.out.println("inputWordIndexList.size = " + inputWordIndexList.size());
			System.out.println("datalength = " + dataLength);
			indexList = new ArrayList<Integer>();
			for (int i = 0; i < dataLength; i = i + batchSize) {
				indexList.add(new Integer(i));
			}
			Collections.shuffle(indexList);
		}
		
		public boolean hasNext() {
			return !indexList.isEmpty();
		}

		public DataSet next() {		
			// input layer size == output layer size!
			System.out.println("layerSize: " + layerSize);
			System.out.println(layerSize * batchSize);
			
			INDArray inputArray = Nd4j.zeros(1, layerSize, batchSize);
			INDArray inputLabels = Nd4j.zeros(1, layerSize, batchSize);
			
			int index = indexList.get(0);
			indexList.remove(0);
			int items = Math.min(batchSize, dataLength - index);
			System.out.println("Index: " + index);
			System.out.println("Items: " + items);
			for(int i=index;i<(index+items);i++) {
			    int wordIndex1 = inputWordIndexList.get(i);
			    System.out.println("wordIndex1 = " + wordIndex1);
			    inputArray.putScalar(new int[]{0, wordIndex1, i}, 1);

			    int wordIndex2 = inputWordIndexList.get(i+1);
			    System.out.println("wordIndex2 = " + wordIndex2);
			    inputLabels.putScalar(new int[]{0, wordIndex2, i}, 1);
			}

			DataSet dataset = new DataSet(inputArray, inputLabels);
			return dataset;
		}

	}

}
