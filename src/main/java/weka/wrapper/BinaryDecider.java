package weka.wrapper;

import java.util.HashMap;
import java.util.Vector;

import weka.core.Instance;
import weka.filters.Filter;



/**
 * @author David Milne
 *
 * A {@link Decider} that chooses between two classes (true and false).
 *
 * @param <A> An enumeration of attributes (features) used to inform the decider
 */
public class BinaryDecider<A extends Enum<A>> extends Decider<A, Boolean>{

	protected BinaryDecider(String name,
			TypedAttribute[] attributes,
			TypedAttribute classAttribute,
			Vector<Filter> filters
	) throws Exception {
		super(name, attributes, classAttribute, filters);
	}
	
	@Override
	public Boolean getDecision(Instance instance) throws Exception {	
		return this.getRawClassification(instance) == 0 ;
	}
	
	@Override
	public HashMap<Boolean, Double> getDecisionDistribution(Instance instance)
			throws Exception {
		
		double[] rawDist = this.getRawDistributionForInstance(instance) ;
		
		HashMap<Boolean,Double> dist = new HashMap<Boolean,Double>() ;
		
		dist.put(true, rawDist[0]) ;
		dist.put(false, rawDist[1]) ;
		
		return dist ;
	}

	@Override
	public InstanceBuilder<A,Boolean> getInstanceBuilder() {
		
		return new InstanceBuilder<A,Boolean>(_attributes, _classAttribute, _datasetHeader) {

			public InstanceBuilder<A,Boolean> setClassAttribute(Boolean value) {
				
				if (!_classAttribute.getClassType().equals(Boolean.class))
					throw new IllegalArgumentException(_classAttribute.name() + " is not a Boolean attribute") ;
				
				if (value)
					_instance.setClassValue(0) ;
				else
					_instance.setClassValue(1) ;
				
				return this ;
			}
		} ;
	}



	
	
	
}
