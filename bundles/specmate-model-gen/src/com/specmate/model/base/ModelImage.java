/**
 */
package com.specmate.model.base;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Model Image</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.base.ModelImage#getImageData <em>Image Data</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.base.BasePackage#getModelImage()
 * @model annotation="http://specmate.com/form_meta disabled1='name' disabled2='description'"
 *        annotation="http://specmate.com/notLoadingOnList"
 * @generated
 */
public interface ModelImage extends IContentElement {
	/**
	 * Returns the value of the '<em><b>Image Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image Data</em>' attribute.
	 * @see #setImageData(String)
	 * @see com.specmate.model.base.BasePackage#getModelImage_ImageData()
	 * @model
	 * @generated
	 */
	String getImageData();

	/**
	 * Sets the value of the '{@link com.specmate.model.base.ModelImage#getImageData <em>Image Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image Data</em>' attribute.
	 * @see #getImageData()
	 * @generated
	 */
	void setImageData(String value);

} // ModelImage
