package weka.wrapper;

import java.util.Vector;

import weka.filters.Filter;

/**
 * @author dmilne
 *
 * A helper for building {@link Decider}s.  
 *
 * @param <A> an enum of attributes (features) used by the decider
 * @param <C> the type of the class attribute
 */
public class DeciderBuilder<A extends Enum<A>> {

	private String _name ;
	
	
	private Class<A> _attributeClass ;
	private TypedAttribute[] _attributes;
	private TypedAttribute _classAttribute ;
	
	private Vector<Filter> _filters ;
	
	@SuppressWarnings("unchecked")
	private Class _defaultAttributeClass ;
	
	public DeciderBuilder(String name, Class<A> attributeClass) {
		_name = name ;
		_attributeClass = attributeClass ;
		_attributes = new TypedAttribute[attributeClass.getEnumConstants().length] ;
		_filters = new Vector<Filter>() ;
	}
	
	public DeciderBuilder<A> setDefaultAttributeTypeNumeric() {
		_defaultAttributeClass = Double.class ;
		return this ;
	}
	
	public DeciderBuilder<A> setDefaultAttributeTypeBoolean() {
		_defaultAttributeClass = Boolean.class ;
		return this ;
	}
	
	public <E extends Enum<E>> DeciderBuilder<A> setDefaultAttributeTypeEnum(Class<E> enumClass) {
		_defaultAttributeClass = enumClass ;
		return this ;
	}
		
	public DeciderBuilder<A> setAttributeTypeNumeric(A a) {
		_attributes[a.ordinal()] = TypedAttribute.getNumericAttribute(a.name(), a.ordinal()) ;		
		return this ;
	}
	
	public DeciderBuilder<A> setAttributeTypeBoolean(A a) {
		_attributes[a.ordinal()] = TypedAttribute.getBooleanAttribute(a.name(), a.ordinal()) ;
		return this ;
	}
	
	public <E extends Enum<E>> DeciderBuilder<A> setAttributeTypeEnum(A a, Class<E> enumClass) {
		
		_attributes[a.ordinal()] = TypedAttribute.getEnumAttribute(a.name(), a.ordinal(), enumClass) ;
		return this ;
	}
	
	public <E extends Enum<E>> DeciderBuilder<A> setAttributeTypeString(A a) {
		
		_attributes[a.ordinal()] = TypedAttribute.getStringAttribute(a.name(), a.ordinal()) ;
		return this ;
	}
	
	public DeciderBuilder<A> setClassAttributeTypeBoolean(String name) {
				
		_classAttribute = TypedAttribute.getBooleanAttribute(name, _attributes.length) ;
		return this ;
	}
	
	public DeciderBuilder<A> setClassAttributeTypeNumeric(String name) {
		
		_classAttribute = TypedAttribute.getNumericAttribute(name, _attributes.length) ;
		return this ;
	}
	
	public <E extends Enum<E>> DeciderBuilder setClassAttributeTypeEnum(String name, Class<E> enumClass) {
		
		_classAttribute = TypedAttribute.getEnumAttribute(name, _attributes.length, enumClass) ;
		return this ;
	}
	
	public DeciderBuilder<A> addFilter(Filter f) {
		_filters.add(f) ;
		return this ;
	}
	
	@SuppressWarnings("unchecked")
	public Decider<A,?> build() throws Exception {
		
		if (_classAttribute == null)
			throw new Exception("No class attribute set") ;
		
		for (int i=0 ; i<_attributes.length ; i++ ) {
			
			if (_attributes[i] == null) {
				A a = _attributeClass.getEnumConstants()[i] ;
				
				if (_defaultAttributeClass == null) {
					throw new Exception("Unknown attribute type for '" + a.name() + "'") ;
				} else {
					if (_defaultAttributeClass.isEnum()) 
						_attributes[i] = TypedAttribute.getEnumAttribute(a.name(), i, (Class<Enum>)_defaultAttributeClass) ;
					else if (_defaultAttributeClass.equals(Boolean.class))
						_attributes[i] = TypedAttribute.getBooleanAttribute(a.name(), i) ;
					else
						_attributes[i] = TypedAttribute.getNumericAttribute(a.name(), i) ;
				}
			}
		}

		if (_classAttribute.getClassType().equals(Double.class))
			return new NumericDecider(_name, _attributes, _classAttribute, _filters) ;
		
		if (_classAttribute.getClassType().equals(Boolean.class))
			return new BinaryDecider(_name, _attributes, _classAttribute, _filters) ;
		
		if (_classAttribute.getClassType().isEnum())
			return new MulticlassDecider(_name, _attributes, _classAttribute, _filters) ;
		
		throw new Exception("No decider available for class attribute type " + _classAttribute.getClassType()) ;
	}
	
}
