package weka.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * A machine-learned decision function, backed by Weka. 
 * <p>
 * @note You should not try to create a Decider directly. Use a {@link DeciderBuilder} instead.
 * 
 * @param <A> An enumeration of attributes (features) used to inform the decision function
 * @param <C> The expected output (this must either be Double, Boolean, or some kind of enum)
 */
public abstract class Decider<A extends Enum<A>,C> {
	
	private String _name ;
	
	protected TypedAttribute[] _attributes ;
	protected TypedAttribute _classAttribute ;
	
	protected Instances _datasetHeader ;
	
	protected Classifier _classifier = null ;
	
	protected Decider(String name, TypedAttribute[] attributes, TypedAttribute classAttribute, Vector<Filter> filters) throws Exception {
		_name = name ;
		_attributes = attributes ;
		_classAttribute = classAttribute ;
		
		_datasetHeader = createDatasetHeader() ;
	}

	/**
	 * Returns the name of the decision function
	 * 
	 * @return the name of the decision function
	 */
	public String getName() {
		return _name ;
	}
	
	/**
	 * You must either {@link #load} or {@link #build} before you can call {@link #getDecision(Instance)} or {@link #getDecisionDistribution(Instance)}
	 * This method checks that this has been done.
	 * 
	 * @return true if this is ready to make decisions.
	 */
	public boolean isReady() {
		return (_classifier != null) ;
	}

	/**
	 * Makes a decision for the given instance.
	 * <p>
	 * This function is not available until {@link #isReady() isReady} returns true.
	 * <p>
	 * You should typically use an {@link #getInstanceBuilder() instance builder} to generate 
	 * the instance that is passed into this method.
	 * 
	 * @param instance a set of attribute values
	 * @return the decision
	 * @throws Exception
	 */
	public abstract C getDecision(Instance instance) throws Exception ;
	
	
	/**
	 * Returns a HashMap associating possible decision outputs with a confidence value in that decision.
	 * This information can be used, for example, to identify cases when the result of {@link #getDecision(Instance)}  
	 * is clear-cut, or a close tie with other possible decisions.  
	 * <p>
	 * The method by which these values are calculated and their usefulness depends on 
	 * the classifier.
	 * <p>
	 * This function is not available until {@link #isReady() isReady} returns true.
	 * <p>
	 * You should typically use an {@link #getInstanceBuilder() instance builder} to generate 
	 * the instance that is passed into this method.
	 * 
	 * @param instance 
	 * @return A HashMap associating possible decisions with confidence values.
	 * @throws Exception
	 */
	public abstract HashMap<C,Double> getDecisionDistribution(Instance instance) throws Exception ;
	
	protected double[] getRawDistributionForInstance(Instance instance) throws Exception {
		
		throwIfCannotClassify(instance) ;
		
		return _classifier.distributionForInstance(instance) ;
	}
	
	protected double getRawClassification(Instance instance) throws Exception {
		
		throwIfCannotClassify(instance) ;
		
		instance.setDataset(_datasetHeader) ;
		
		return _classifier.classifyInstance(instance) ;
	}
	
	protected void throwIfCannotClassify(Instance instance) throws Exception {

		if (_classifier == null) 
			throw new Exception("Decider must be trained or loaded") ;

		if (!instance.dataset().equalHeaders(_datasetHeader))
			throw new Exception("Unexpected attributes") ;

		if (!instance.classIsMissing())
			throw new Exception("Class attribute already known; nothing to decide") ;

	}
	
	/**
	 * Returns a new (empty) dataset that can (when filled) be used to train or test this decider.
	 * 
	 * @return a new (empty) dataset for this decider.
	 */
	public Dataset<A,C> createNewDataset() {
		return new Dataset<A,C>(this) ;
	}
	
	
	/**
	 * Trains the decider using the given classifier and training data. 
	 */
	public void train(Classifier classifier, Dataset<A,C> trainingData) throws Exception {
		
		//TODO: this should apply filters to dataset
		
		if (trainingData == null || trainingData.numInstances() == 0) 
			throw new Exception("You must load or build training data before building classifier") ;

		_classifier = classifier ;
		classifier.buildClassifier(trainingData) ;
	} 
	
	public void test(Dataset<A,C> data) {
		
		
	}
	
	/**
	 * Loads a prebuilt decider from file 
	 * 
	 * @param file 
	 * @throws Exception 
	 */
	public void load(File file) throws Exception {
		
		//TODO: this should also load header info and filters, and check that header info is as expected.
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		_classifier = (Classifier) ois.readObject();
		ois.close();
		
		if (_classifier == null)
			throw new Exception(file.getPath() +  " does not contain a valid classifier for " + _name + " decider.") ;
	}


	/**
	 * Serialises the classifier and saves it to the given file.
	 * 
	 * @param file the file to save the classifier to
	 * @throws IOException if the file cannot be written to
	 */
	public void save(File file) throws IOException {
		
		//TODO: this should also save header info and filters. 
		
		Logger.getLogger(Decider.class).info("saving classifier") ;

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(_classifier);
		oos.flush();
		oos.close();
	}
	
	protected Instances createDatasetHeader() {
		
		FastVector attrs = new FastVector() ;
		for (TypedAttribute attr: _attributes)
			attrs.addElement(attr) ;
		
		attrs.addElement(_classAttribute) ;
		
		Instances header = new Instances(_name, attrs, 0) ;
		header.setClass(_classAttribute) ;
		
		return header ;
	}
	
	/**
	 * Returns a builder for constructing an instance that this decider can make a decision for.
	 *
	 * @return a builder for constructing an instance for this decider can make a decision for.
	 */
	public abstract InstanceBuilder<A,C> getInstanceBuilder() ;
	
}
