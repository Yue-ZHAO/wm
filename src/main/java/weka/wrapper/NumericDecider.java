package weka.wrapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class NumericDecider<A extends Enum<A>> extends Decider<A,Double> {

	protected NumericDecider(
			String name,
			TypedAttribute[] attributes,
			TypedAttribute classAttribute,
			Vector<Filter> filters
	) throws Exception {
		super(name, attributes, classAttribute, filters);
	}

	public Double getDecision(Instance instance) throws Exception  {
		return this.getRawClassification(instance) ;
	}
	
	@Override
	public HashMap<Double, Double> getDecisionDistribution(Instance instance)
			throws Exception {
		
		throw new UnsupportedOperationException() ;
	}
	
	@Override
	public InstanceBuilder<A,Double> getInstanceBuilder() {
		
		return new InstanceBuilder<A,Double>(_attributes, _classAttribute, _datasetHeader) {

			public InstanceBuilder<A,Double> setClassAttribute(Double value) {
				
				if (!_classAttribute.getClassType().equals(value.getClass()))
					throw new IllegalArgumentException(_classAttribute.name() + " is not a " + value.getClass() + " attribute") ;
				
				_instance.setClassValue(value) ;
				
				return this ;
			}
		} ;
	}


}
