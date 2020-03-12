/**
 */
package com.specmate.model.auth.impl;

import com.specmate.model.auth.AuthPackage;
import com.specmate.model.auth.AuthProject;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.internal.cdo.CDOObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Project</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.auth.impl.AuthProjectImpl#getName <em>Name</em>}</li>
 *   <li>{@link com.specmate.model.auth.impl.AuthProjectImpl#getOauthUrl <em>Oauth Url</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AuthProjectImpl extends CDOObjectImpl implements AuthProject {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getOauthUrl() <em>Oauth Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOauthUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String OAUTH_URL_EDEFAULT = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AuthProjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AuthPackage.Literals.AUTH_PROJECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected int eStaticFeatureCount() {
		return 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return (String)eDynamicGet(AuthPackage.AUTH_PROJECT__NAME, AuthPackage.Literals.AUTH_PROJECT__NAME, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		eDynamicSet(AuthPackage.AUTH_PROJECT__NAME, AuthPackage.Literals.AUTH_PROJECT__NAME, newName);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOauthUrl() {
		return (String)eDynamicGet(AuthPackage.AUTH_PROJECT__OAUTH_URL, AuthPackage.Literals.AUTH_PROJECT__OAUTH_URL, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOauthUrl(String newOauthUrl) {
		eDynamicSet(AuthPackage.AUTH_PROJECT__OAUTH_URL, AuthPackage.Literals.AUTH_PROJECT__OAUTH_URL, newOauthUrl);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case AuthPackage.AUTH_PROJECT__NAME:
				return getName();
			case AuthPackage.AUTH_PROJECT__OAUTH_URL:
				return getOauthUrl();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case AuthPackage.AUTH_PROJECT__NAME:
				setName((String)newValue);
				return;
			case AuthPackage.AUTH_PROJECT__OAUTH_URL:
				setOauthUrl((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case AuthPackage.AUTH_PROJECT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case AuthPackage.AUTH_PROJECT__OAUTH_URL:
				setOauthUrl(OAUTH_URL_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case AuthPackage.AUTH_PROJECT__NAME:
				return NAME_EDEFAULT == null ? getName() != null : !NAME_EDEFAULT.equals(getName());
			case AuthPackage.AUTH_PROJECT__OAUTH_URL:
				return OAUTH_URL_EDEFAULT == null ? getOauthUrl() != null : !OAUTH_URL_EDEFAULT.equals(getOauthUrl());
		}
		return super.eIsSet(featureID);
	}

} //AuthProjectImpl
