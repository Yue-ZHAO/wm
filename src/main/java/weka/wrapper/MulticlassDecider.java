package weka.wrapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import weka.core.Instance;
import weka.filters.Filter;


/**
 * @author dmilne
 *
 * A {@link Decider} that chooses between two classes (true and false).
 *
 * @param <A> An enumeration of attributes (features) used to inform the decider
 * @param <C> 
 * 
 */
public class MulticlassDecider<A extends Enum<A>, C extends Enum<C>> extends Decider<A,C> {

	
	private C[] possibleClassValues ;
	
	
	protected MulticlassDecider(
			String name, 
			TypedAttribute[] attributesByName, 
			TypedAttribute classAttribute,
			Vector<Filter> filters
	) throws Exception {
		super(name, attributesByName, classAttribute, filters);
		
		possibleClassValues = (C[])_classAttribute.getClassType().getEnumConstants() ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public C getDecision(Instance instance) throws Exception {
		
		int predictionOrdinal = (int)this.getRawClassification(instance) ;
		
		return possibleClassValues[predictionOrdinal] ;
	}

	@Override
	public HashMap<C, Double> getDecisionDistribution(Instance instance)
			throws Exception {
		
		double[] rawDist = this.getRawDistributionForInstance(instance) ;
		
		HashMap<C,Double> dist = new HashMap<C,Double>() ;
		
		for (int i=0 ; i<rawDist.length ; i++) {
			dist.put(possibleClassValues[i], rawDist[i]) ;
		}
		
		return dist ;
	}

	@Override
	public InstanceBuilder<A,C> getInstanceBuilder() {
		
		return new InstanceBuilder<A,C>(_attributes, _classAttribute, _datasetHeader) {

			public InstanceBuilder<A,C> setClassAttribute(C value) {
				
				if (!_classAttribute.getClassType().equals(value.getClass()))
					throw new IllegalArgumentException(_classAttribute.name() + " is not a " + value.getClass() + " attribute") ;
				
				_instance.setClassValue(value.ordinal()) ;
				
				return this ;
			}
		} ;
	}

	

}
