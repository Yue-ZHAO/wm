package weka.wrapper;


import weka.core.Attribute;
import weka.core.FastVector;

public class TypedAttribute extends Attribute{
	
	Class _classType ;

	public TypedAttribute(String name, int index, Class c) {
		super(name, index) ;
		_classType = c ;
	}

	public TypedAttribute(String name, FastVector values, int index, Class c) {
		super(name, values, index) ;
		_classType = c ;
	}

	public Class getClassType() {
		return _classType ;
	}

	public static TypedAttribute getNumericAttribute(String name, int index) {
		return new TypedAttribute(name, index, Double.class) ;
	}
	
	public static TypedAttribute getBooleanAttribute(String name, int index) {

		FastVector vals = new FastVector() ;
		vals.addElement("TRUE") ;
		vals.addElement("FALSE") ;

		return new TypedAttribute(name, vals, index, Boolean.class) ;
	}

	public static <E extends Enum<E>> TypedAttribute getEnumAttribute(String name, int index, Class<E> enumClass) {

		FastVector vals = new FastVector() ;
		for (E val:enumClass.getEnumConstants())
			vals.addElement(val.name()) ;

		return new TypedAttribute(name, vals, index, enumClass) ;
	}
	
	public static TypedAttribute getStringAttribute(String name, int index) {
		return new TypedAttribute(name, (FastVector)null, index, String.class) ;		
	}

}
