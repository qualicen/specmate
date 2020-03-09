/**
 */
package com.specmate.model.base;

import org.eclipse.emf.cdo.CDOObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IRecycled</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.base.IRecycled#isIsRecycled <em>Is Recycled</em>}</li>
 *   <li>{@link com.specmate.model.base.IRecycled#isHasRecycledChildren <em>Has Recycled Children</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.base.BasePackage#getIRecycled()
 * @model interface="true" abstract="true"
 * @extends CDOObject
 * @generated
 */
public interface IRecycled extends CDOObject {
	/**
	 * Returns the value of the '<em><b>Is Recycled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Recycled</em>' attribute.
	 * @see #setIsRecycled(boolean)
	 * @see com.specmate.model.base.BasePackage#getIRecycled_IsRecycled()
	 * @model
	 * @generated
	 */
	boolean isIsRecycled();

	/**
	 * Sets the value of the '{@link com.specmate.model.base.IRecycled#isIsRecycled <em>Is Recycled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Recycled</em>' attribute.
	 * @see #isIsRecycled()
	 * @generated
	 */
	void setIsRecycled(boolean value);

	/**
	 * Returns the value of the '<em><b>Has Recycled Children</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Recycled Children</em>' attribute.
	 * @see #setHasRecycledChildren(boolean)
	 * @see com.specmate.model.base.BasePackage#getIRecycled_HasRecycledChildren()
	 * @model
	 * @generated
	 */
	boolean isHasRecycledChildren();

	/**
	 * Sets the value of the '{@link com.specmate.model.base.IRecycled#isHasRecycledChildren <em>Has Recycled Children</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Recycled Children</em>' attribute.
	 * @see #isHasRecycledChildren()
	 * @generated
	 */
	void setHasRecycledChildren(boolean value);

} // IRecycled
