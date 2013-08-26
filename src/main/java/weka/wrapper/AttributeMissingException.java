package weka.wrapper;

import weka.core.Attribute;

/**
 * @author dmilne
 *	An Exception that is thrown when an instance should declare an attribute value, but does not 
 */
public class AttributeMissingException extends Exception {

	private static final long serialVersionUID = 7245091581174862992L;

	/**
	 * Constructs a new AttributeMissingException for the given attribute
	 * 
	 * @param attribute the missing attribute
	 */
	public AttributeMissingException(Attribute attribute) {
		super("'" + attribute.name() + "' attribute is missing") ;
	}
}
