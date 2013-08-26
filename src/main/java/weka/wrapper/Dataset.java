package weka.wrapper;

import java.io.File;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;



/**
 * A data set for training or testing a decider.
 * <p>
 * You cannot construct data sets directly. Instead use {@link Decider#createNewDataset()}.
 *
 * @param <A> An enumeration of attributes (features) used to inform the decider
 * @param <C> The class attribute type (this must either be Double, Boolean, or some kind of enum)
 */
public class Dataset<A extends Enum<A>,C> extends Instances {

	private static final long serialVersionUID = 4144187957200070770L;
	
	private Decider<A,C> _decider  ;
	
	protected Dataset(Decider<A,C> decider) {
		
		super(decider.createDatasetHeader()) ;
		
		_decider = decider ;
	}
	
	/**
	 * Saves this data set to file, in ARFF format.
	 * 
	 * @param file a writable file
	 * @throws Exception if the file is not writable, or if there is no data to save
	 */
	public void save(File file) throws Exception {
		
		if (numInstances() == 0)
			throw new Exception("There is no data to save") ;
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(this);
		saver.setFile(file);
		saver.writeBatch();
	}
	
	
	/**
	 * Loads this data set from an ARFF file
	 * 
	 * @param file the readable ARFF file
	 * @throws Exception if the file is not readable, or does not describe the expected attributes
	 */
	public void load(File file) throws Exception {
		
		ArffLoader loader = new ArffLoader() ;
		loader.setFile(file) ;
		
		//check that loaded data matches expected attributes
		Instances structure = loader.getStructure() ;
		
		if (structure.numAttributes() != _decider._attributes.length + 1)
			throw new Exception("Expected " + (_decider._attributes.length + 1) + " attributes but found " + structure.numAttributes() + " instead.") ;			
		
		int index = 0 ;
		for (TypedAttribute attr : _decider._attributes) {
			String fileAttr = structure.attribute(index).name();
			if (!attr.name().equals(fileAttr))
				throw new Exception("Expected attribute " + index + "-" + attr.name() + " but found " + fileAttr + " instead.") ;			
		
			//TODO: check attribute types
			
			index ++ ;
		}
		
		Instance nextInstance ;
		while ((nextInstance = loader.getNextInstance(structure)) != null)
			this.add(nextInstance) ;		
		
		setClass(_decider._classAttribute) ;
	}
	
	
	/**
	 * Returns a builder for constructing an instance for this data set.
	 *
	 * @return a builder for constructing an instance for this data set.
	 */
	public InstanceBuilder<A,C> getInstanceBuilder() {
		
		return _decider.getInstanceBuilder() ;
	}	
}
