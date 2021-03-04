package com.specmate.dbprovider.api.migration;

import java.util.Date;

import com.specmate.common.exception.SpecmateException;

/**
 * This interface defines operations that reflect changes of object attributes
 * in an EMF Model. Implementations for specific database providers determine
 * how these changes map to particular SQL dialects and features.
 */
public interface IAttributeToSQLMapper {

	/**
	 * Creates a new String attribute.
	 *
	 * @param objectName    the name of the object where the attribute is added
	 * @param attributeName the name of the attribute that is added
	 * @param defaultValue  the value that is stored in the database if the value in
	 *                      the object is not defined
	 * @param isDerived     true if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewStringAttribute(String objectName, String attributeName, String defaultValue, boolean isDerived)
			throws SpecmateException;

	default void migrateNewStringAttribute(String objectName, String attributeName, String defaultValue)
			throws SpecmateException {
		migrateNewStringAttribute(objectName, attributeName, defaultValue, false);
	}

	/**
	 * Creates a new Boolean attribute.
	 *
	 * @param objectName    the name of the object where the attribute is added
	 * @param attributeName the name of the attribute that is added
	 * @param defaultValue  the value that is stored in the database if the value in
	 *                      the object is not defined
	 * @param isDerived     true if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewBooleanAttribute(String objectName, String attributeName, Boolean defaultValue, boolean isDerived)
			throws SpecmateException;

	default void migrateNewBooleanAttribute(String objectName, String attributeName, Boolean defaultValue)
			throws SpecmateException {
		migrateNewBooleanAttribute(objectName, attributeName, defaultValue, false);
	}

	/**
	 * Creates a new Integer attribute.
	 *
	 * @param objectName    the name of the object where the attribute is added
	 * @param attributeName the name of the attribute that is added
	 * @param defaultValue  the value that is stored in the database if the value in
	 *                      the object is not defined
	 * @param isDerived     true if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewIntegerAttribute(String objectName, String attributeName, Integer defaultValue, boolean isDerived)
			throws SpecmateException;

	default void migrateNewIntegerAttribute(String objectName, String attributeName, Integer defaultValue)
			throws SpecmateException {
		migrateNewIntegerAttribute(objectName, attributeName, defaultValue, false);
	}

	/**
	 * Creates a new Double attribute.
	 *
	 * @param objectName    the name of the object where the attribute is added
	 * @param attributeName the name of the attribute that is added
	 * @param defaultValue  the value that is stored in the database if the value in
	 *                      the object is not defined
	 * @param isDerivced    true if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewDoubleAttribute(String objectName, String attributeName, Double defaultValue, boolean isDerivced)
			throws SpecmateException;

	default void migrateNewDoubleAttribute(String objectName, String attributeName, Double defaultValue)
			throws SpecmateException {
		migrateNewDoubleAttribute(objectName, attributeName, defaultValue, false);
	}

	/**
	 * Creates a new Long attribute.
	 *
	 * @param objectName    the name of the object where the attribute is added
	 * @param attributeName the name of the attribute that is added
	 * @param defaultValue  the value that is stored in the database if the value in
	 *                      the object is not defined
	 * @param isDerived     true, if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewLongAttribute(String objectName, String attributeName, Long defaultValue, boolean isDerived)
			throws SpecmateException;

	default void migrateNewLongAttribute(String objectName, String attributeName, Long defaultValue)
			throws SpecmateException {
		migrateNewLongAttribute(objectName, attributeName, defaultValue, false);
	}

	/**
	 * Creates a new Date attribute.
	 *
	 * @param objectName    the name of the object where the attribute is added
	 * @param attributeName the name of the attribute that is added
	 * @param defaultValue  the value that is stored in the database if the value in
	 *                      the object is not defined
	 * @param isDerived     true, if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewDateAttribute(String objectName, String attributeName, Date defaultValue, boolean isDerived)
			throws SpecmateException;

	default void migrateNewDateAttribute(String objectName, String attributeName, Date defaultValue)
			throws SpecmateException {
		migrateNewDateAttribute(objectName, attributeName, defaultValue, false);
	}

	/**
	 * Creates an attribute that represents a reference to another object.
	 *
	 * @param objectName    the name of the object where the reference is added
	 * @param attributeName the name of the reference attribute that is added
	 * @param isDerived     true if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewObjectReferenceNtoM(String objectName, String attributeName, boolean isDerived, String derivedFrom)
			throws SpecmateException;

	default void migrateNewObjectReferenceNtoM(String objectName, String attributeName) throws SpecmateException {
		migrateNewObjectReferenceNtoM(objectName, attributeName, false, null);
	}

	/**
	 * Creates an attribute that represents a reference to another object. It is a 1
	 * to n reference, thus no extra table is created.
	 *
	 * @param objectName    the name of the object where the reference is added
	 * @param attributeName the name of the reference attribute that is added
	 * @param isDerived     true if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewObjectReferenceOneToN(String objectName, String attributeName, boolean isDerived)
			throws SpecmateException;

	default void migrateNewObjectReferenceOneToN(String objectName, String attributeName) throws SpecmateException {
		migrateNewObjectReferenceOneToN(objectName, attributeName, false);
	}

	/**
	 * Creates an attribute that represents a reference to a string list.
	 *
	 * @param objectName    the name of the object where the reference is added
	 * @param attributeName the name of the reference attribute that is added
	 * @param isDerived     true, if the attribute is a derived attribute
	 * @throws SpecmateException
	 */
	void migrateNewStringReference(String objectName, String attributeName, boolean isDerived, String derivedFrom)
			throws SpecmateException;

	default void migrateNewStringReference(String objectName, String attributeName) throws SpecmateException {
		migrateNewStringReference(objectName, attributeName, false, null);
	}

	/**
	 * Renames an attribute.
	 *
	 * @param objectName       the name of the object where the attribute is added
	 * @param oldAttributeName the old attribute name
	 * @param newAttributeName the new attribute name
	 * @throws SpecmateException
	 */
	void migrateRenameAttribute(String objectName, String oldAttributeName, String newAttributeName)
			throws SpecmateException;

	/**
	 * Changes the data type of an attribute if a conversion is allowed.
	 *
	 * @param objectName    the name of the object where the attribute is changed
	 * @param attributeName the name of the attribute whose type is changed
	 * @param targetType    the target data type
	 * @throws SpecmateException
	 */
	void migrateChangeType(String objectName, String attributeName, IDataType targetType) throws SpecmateException;

	/**
	 * Adapts the index of the containing feature column
	 *
	 * @param objectName the name of the object where the attribute is changed
	 * @param count      the delta to adapt
	 * @throws SpecmateException
	 */
	void adaptContainingFeatureIndex(String objectName, int i) throws SpecmateException;

}