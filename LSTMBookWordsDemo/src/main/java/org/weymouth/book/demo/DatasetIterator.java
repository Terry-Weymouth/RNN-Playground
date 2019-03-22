package org.weymouth.book.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

class DatasetIterator {
	
	long layerSize;
	int batchSize;
	int inputDataSampleLength;
	List<Integer> inputWordIndexList;
	List<Integer> indexList;
	
	DatasetIterator(List<Integer> inputWordIndexList, int batchSize, long layerSize) {
		this.layerSize = layerSize;
		this.batchSize = batchSize;
		this.inputWordIndexList = inputWordIndexList;

		inputDataSampleLength = inputWordIndexList.size() - 1;
		System.out.println("inputWordIndexList.size = " + inputWordIndexList.size());
		System.out.println("datalength = " + inputDataSampleLength);
		indexList = new ArrayList<Integer>();
		for (int i = 0; i < inputDataSampleLength; i = i + batchSize) {
			indexList.add(new Integer(i));
		}
		for (Integer i: indexList) {
			System.out.println(i);
		}
		System.out.println("inputWordIndexList.size() = " + inputWordIndexList.size());
		Collections.shuffle(indexList);
	}
	
	public boolean hasNext() {
		return !indexList.isEmpty();
	}

	public DataSet next() {		
		// input layer size == output layer size!
		System.out.println("layerSize: " + layerSize);
		System.out.println(layerSize * batchSize);

        //Note the order here:
        // dimension 0 = number of examples in minibatch
        // dimension 1 = size of each vector (i.e., number of characters)
        // dimension 2 = length of each time series/example
        //Why 'f' order here? See http://deeplearning4j.org/usingrnns.html#data section "Alternative: Implementing a custom DataSetIterator"
		// INDArray input = Nd4j.create(new int[]{currMinibatchSize,layerSize,exampleLength}, 'f');
		// INDArray labels = Nd4j.create(new int[]{currMinibatchSize,layerSize,exampleLength}, 'f');

		int index = indexList.get(0);
		indexList.remove(0);
		int currMinibatchSize = Math.min(batchSize, inputDataSampleLength - index);
		
		INDArray inputArray = Nd4j.zeros(currMinibatchSize, layerSize, batchSize);
		INDArray inputLabels = Nd4j.zeros(currMinibatchSize, layerSize, batchSize);
		
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