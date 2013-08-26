package weka.wrapper;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public abstract class InstanceBuilder<A extends Enum,C> {
	
	/**
	 * An option of how to respond to possible problems
	 */
	public enum BuildResponse { 
		
		/**
		 * Don't do anything, allow to continue
		 */
		IGNORE, 
		
		/**
		 * Print out a warning, but otherwise allow to continue
		 */
		WARN,
		
		/**
		 * Throw an exception
		 */
		THROW_ERROR
		
	} ;

	private TypedAttribute[] _attributes ;
	private TypedAttribute _classAttribute ;
	
	protected Instance _instance ;
		
	BuildResponse _attrMissingResponse = BuildResponse.IGNORE ;
	BuildResponse _classAttrMissingResponse = BuildResponse.IGNORE ;
	
	protected InstanceBuilder(TypedAttribute[] attributes, TypedAttribute classAttribute, Instances dataset) {
		
		_attributes = attributes ;
		_classAttribute = classAttribute ;
		
		_instance = new Instance(attributes.length + 1) ;
		_instance.setDataset(dataset) ;
	}
	
	
	public InstanceBuilder<A,C> setAttributeMissingResponse(BuildResponse r) {
		_attrMissingResponse = r ;
		return this ;
	}
	
	public InstanceBuilder<A,C> setClassAttributeMissingResponse(BuildResponse r) {
		_classAttrMissingResponse = r ;
		return this ;
	}
	
	
	public InstanceBuilder<A,C> setAttribute(A a, Double v) {
		
		TypedAttribute attr = _attributes[a.ordinal()] ;
		
		if (!attr.getClassType().equals(Double.class))
			throw new IllegalArgumentException("'" + a + "' is not a numeric attribute") ;
		
		if (v == null)
			_instance.setValue(attr, Instance.missingValue()) ;
		else
			_instance.setValue(attr, v) ;
		
		return this ;
	}
	
	public InstanceBuilder<A,C> setAttribute(A a, Float v) {
		
		TypedAttribute attr = _attributes[a.ordinal()] ;
		
		if (!attr.getClassType().equals(Double.class))
			throw new IllegalArgumentException("'" + a + "' is not a numeric attribute") ;
		
		if (v == null)
			_instance.setValue(attr, Instance.missingValue()) ;
		else
			_instance.setValue(attr, v) ;
		
		return this ;
		
	}
	
	public InstanceBuilder<A,C> setAttribute(A a, Integer v) {
		
		TypedAttribute attr = _attributes[a.ordinal()] ;
		
		if (!attr.getClassType().equals(Double.class))
			throw new IllegalArgumentException("'" + a + "' is not a numeric attribute") ;
		
		if (v == null)
			_instance.setValue(attr, Instance.missingValue()) ;
		else
			_instance.setValue(attr, v) ;
		
		return this ;
	}
	
	public InstanceBuilder<A,C> setAttribute(A a, Boolean v) {
		
		TypedAttribute attr = _attributes[a.ordinal()] ;
		
		if (!attr.getClassType().equals(Boolean.class))
			throw new IllegalArgumentException("'" + a + "' is not a Boolean attribute") ;
		
		if (v == null)
			_instance.setValue(attr, Instance.missingValue()) ;
		else if (v)
			_instance.setValue(attr, 0) ;
		else
			_instance.setValue(attr, 1) ;
		
		return this ;
	}
	
	public InstanceBuilder<A,C> setAttribute(A a, String v) {
		
		TypedAttribute attr = _attributes[a.ordinal()] ;
		
		if (!attr.getClassType().equals(v.getClass()))
			throw new IllegalArgumentException("'" + a + "' is not a " + v.getClass() + " attribute") ;
		
		if (v == null) {
			_instance.setValue(attr, Instance.missingValue()) ;
		} else {
			int index = attr.addStringValue(v) ;	
			_instance.setValue(attr, index) ;
		}
		return this ;
	}
	
	
	public InstanceBuilder<A,C> setAttribute(A a, Enum v) {
		
		TypedAttribute attr = _attributes[a.ordinal()] ;
		
		if (!attr.getClassType().equals(v.getClass()))
			throw new IllegalArgumentException("'" + a + "' is not a " + v.getClass() + " attribute") ;
		
		_instance.setValue(attr, v.ordinal()) ;
		
		return this ;
	}
	
	public abstract InstanceBuilder<A,C> setClassAttribute(C value) ;
	
	public InstanceBuilder<A,C> setWeight(Double weight) {
		
		_instance.setWeight(weight) ;
		
		return this ;
	}
	
	public InstanceBuilder<A,C> replaceAllMissingValuesWith(Double value) {
		
		int c = _instance.classIndex() ;
		
		for (int i=0 ; i<_instance.numValues();i++) {
			if (_instance.isMissing(i) && i != c)
				_instance.setValue(i, value) ;
		}
		
		return this ;
	}
	
	public Instance build() throws ClassMissingException, AttributeMissingException {
		
		
		
		switch(_classAttrMissingResponse) {
		case IGNORE:
			break ;
		case THROW_ERROR:
			if (_instance.classIsMissing())
				throw new ClassMissingException(_instance.classAttribute()) ;
			break ;
		case WARN:
			if (_instance.classIsMissing())
				Logger.getLogger(InstanceBuilder.class).warn("Class attribute '" + _instance.classAttribute().name() + "' is missing") ;
			break ;
		}
		
		
		Attribute missingAttr = null ;
		for (int i=0 ; i<_instance.numAttributes()-1 ; i++) {
			if (_instance.isMissing(i)) {
				missingAttr = _instance.attribute(i) ;
				break;
			}
		}
		
		switch(_attrMissingResponse) {
		case IGNORE:
			break ;
		case THROW_ERROR:
			if (missingAttr != null)
				throw new AttributeMissingException(missingAttr) ;
			break ;
		case WARN:
			if (_instance.hasMissingValue())
				Logger.getLogger(InstanceBuilder.class).warn("Attribute '" + missingAttr.name() + "' is missing") ;
			break ;	
		}
		
		return _instance ;
	}
	
}
