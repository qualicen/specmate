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
 *   <li>{@link com.specmate.model.base.IRecycled#isRecycled <em>Recycled</em>}</li>
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
	 * Returns the value of the '<em><b>Recycled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Recycled</em>' attribute.
	 * @see #setRecycled(boolean)
	 * @see com.specmate.model.base.BasePackage#getIRecycled_Recycled()
	 * @model
	 * @generated
	 */
	boolean isRecycled();

	/**
	 * Sets the value of the '{@link com.specmate.model.base.IRecycled#isRecycled <em>Recycled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Recycled</em>' attribute.
	 * @see #isRecycled()
	 * @generated
	 */
	void setRecycled(boolean value);

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
