/**
 */
package com.specmate.model.base;

import org.eclipse.emf.cdo.CDOObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IModel</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.base.IModel#getImage <em>Image</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.base.BasePackage#getIModel()
 * @model interface="true" abstract="true"
 * @extends CDOObject
 * @generated
 */
public interface IModel extends CDOObject {
	/**
	 * Returns the value of the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image</em>' reference.
	 * @see #setImage(ModelImage)
	 * @see com.specmate.model.base.BasePackage#getIModel_Image()
	 * @model
	 * @generated
	 */
	ModelImage getImage();

	/**
	 * Sets the value of the '{@link com.specmate.model.base.IModel#getImage <em>Image</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image</em>' reference.
	 * @see #getImage()
	 * @generated
	 */
	void setImage(ModelImage value);

} // IModel
