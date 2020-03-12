/**
 */
package com.specmate.model.auth;

import org.eclipse.emf.cdo.CDOObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Project</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.auth.AuthProject#getName <em>Name</em>}</li>
 *   <li>{@link com.specmate.model.auth.AuthProject#getOauthUrl <em>Oauth Url</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.auth.AuthPackage#getAuthProject()
 * @model
 * @extends CDOObject
 * @generated
 */
public interface AuthProject extends CDOObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see com.specmate.model.auth.AuthPackage#getAuthProject_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link com.specmate.model.auth.AuthProject#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Oauth Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Oauth Url</em>' attribute.
	 * @see #setOauthUrl(String)
	 * @see com.specmate.model.auth.AuthPackage#getAuthProject_OauthUrl()
	 * @model
	 * @generated
	 */
	String getOauthUrl();

	/**
	 * Sets the value of the '{@link com.specmate.model.auth.AuthProject#getOauthUrl <em>Oauth Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Oauth Url</em>' attribute.
	 * @see #getOauthUrl()
	 * @generated
	 */
	void setOauthUrl(String value);

} // AuthProject
