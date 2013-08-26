package weka.wrapper;

import weka.core.Attribute;

/**
 * An exception that is thrown when an instance should specify the value of the class attribute, but does not
 */
public class ClassMissingException extends Exception {
	
	private static final long serialVersionUID = 6014132659662261670L;

	/**
	 * Constructs a new ClassMissingException for the given class attribute
	 * 
	 * @param classAttribute the class attribute
	 */
	public ClassMissingException(Attribute classAttribute) {
		super("Class attribute '" + classAttribute.name() + "' is missing") ;
	}
}
