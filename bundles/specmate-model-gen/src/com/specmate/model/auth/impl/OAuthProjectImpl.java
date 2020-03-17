/**
 */
package com.specmate.model.auth.impl;

import com.specmate.model.auth.AuthPackage;
import com.specmate.model.auth.OAuthProject;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.internal.cdo.CDOObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OAuth Project</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.auth.impl.OAuthProjectImpl#getName <em>Name</em>}</li>
 *   <li>{@link com.specmate.model.auth.impl.OAuthProjectImpl#getOauthUrl <em>Oauth Url</em>}</li>
 *   <li>{@link com.specmate.model.auth.impl.OAuthProjectImpl#getOauthTokenUrl <em>Oauth Token Url</em>}</li>
 *   <li>{@link com.specmate.model.auth.impl.OAuthProjectImpl#getOauthClientId <em>Oauth Client Id</em>}</li>
 *   <li>{@link com.specmate.model.auth.impl.OAuthProjectImpl#getOauthClientSecret <em>Oauth Client Secret</em>}</li>
 *   <li>{@link com.specmate.model.auth.impl.OAuthProjectImpl#getOauthRedirectUrl <em>Oauth Redirect Url</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OAuthProjectImpl extends CDOObjectImpl implements OAuthProject {
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
	 * The default value of the '{@link #getOauthTokenUrl() <em>Oauth Token Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOauthTokenUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String OAUTH_TOKEN_URL_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getOauthClientId() <em>Oauth Client Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOauthClientId()
	 * @generated
	 * @ordered
	 */
	protected static final String OAUTH_CLIENT_ID_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getOauthClientSecret() <em>Oauth Client Secret</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOauthClientSecret()
	 * @generated
	 * @ordered
	 */
	protected static final String OAUTH_CLIENT_SECRET_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getOauthRedirectUrl() <em>Oauth Redirect Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOauthRedirectUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String OAUTH_REDIRECT_URL_EDEFAULT = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OAuthProjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AuthPackage.Literals.OAUTH_PROJECT;
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
		return (String)eDynamicGet(AuthPackage.OAUTH_PROJECT__NAME, AuthPackage.Literals.IAUTH_PROJECT__NAME, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		eDynamicSet(AuthPackage.OAUTH_PROJECT__NAME, AuthPackage.Literals.IAUTH_PROJECT__NAME, newName);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOauthUrl() {
		return (String)eDynamicGet(AuthPackage.OAUTH_PROJECT__OAUTH_URL, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_URL, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOauthUrl(String newOauthUrl) {
		eDynamicSet(AuthPackage.OAUTH_PROJECT__OAUTH_URL, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_URL, newOauthUrl);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOauthTokenUrl() {
		return (String)eDynamicGet(AuthPackage.OAUTH_PROJECT__OAUTH_TOKEN_URL, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_TOKEN_URL, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOauthTokenUrl(String newOauthTokenUrl) {
		eDynamicSet(AuthPackage.OAUTH_PROJECT__OAUTH_TOKEN_URL, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_TOKEN_URL, newOauthTokenUrl);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOauthClientId() {
		return (String)eDynamicGet(AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_ID, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_CLIENT_ID, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOauthClientId(String newOauthClientId) {
		eDynamicSet(AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_ID, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_CLIENT_ID, newOauthClientId);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOauthClientSecret() {
		return (String)eDynamicGet(AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_SECRET, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_CLIENT_SECRET, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOauthClientSecret(String newOauthClientSecret) {
		eDynamicSet(AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_SECRET, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_CLIENT_SECRET, newOauthClientSecret);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOauthRedirectUrl() {
		return (String)eDynamicGet(AuthPackage.OAUTH_PROJECT__OAUTH_REDIRECT_URL, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_REDIRECT_URL, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOauthRedirectUrl(String newOauthRedirectUrl) {
		eDynamicSet(AuthPackage.OAUTH_PROJECT__OAUTH_REDIRECT_URL, AuthPackage.Literals.OAUTH_PROJECT__OAUTH_REDIRECT_URL, newOauthRedirectUrl);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case AuthPackage.OAUTH_PROJECT__NAME:
				return getName();
			case AuthPackage.OAUTH_PROJECT__OAUTH_URL:
				return getOauthUrl();
			case AuthPackage.OAUTH_PROJECT__OAUTH_TOKEN_URL:
				return getOauthTokenUrl();
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_ID:
				return getOauthClientId();
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_SECRET:
				return getOauthClientSecret();
			case AuthPackage.OAUTH_PROJECT__OAUTH_REDIRECT_URL:
				return getOauthRedirectUrl();
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
			case AuthPackage.OAUTH_PROJECT__NAME:
				setName((String)newValue);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_URL:
				setOauthUrl((String)newValue);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_TOKEN_URL:
				setOauthTokenUrl((String)newValue);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_ID:
				setOauthClientId((String)newValue);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_SECRET:
				setOauthClientSecret((String)newValue);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_REDIRECT_URL:
				setOauthRedirectUrl((String)newValue);
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
			case AuthPackage.OAUTH_PROJECT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_URL:
				setOauthUrl(OAUTH_URL_EDEFAULT);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_TOKEN_URL:
				setOauthTokenUrl(OAUTH_TOKEN_URL_EDEFAULT);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_ID:
				setOauthClientId(OAUTH_CLIENT_ID_EDEFAULT);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_SECRET:
				setOauthClientSecret(OAUTH_CLIENT_SECRET_EDEFAULT);
				return;
			case AuthPackage.OAUTH_PROJECT__OAUTH_REDIRECT_URL:
				setOauthRedirectUrl(OAUTH_REDIRECT_URL_EDEFAULT);
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
			case AuthPackage.OAUTH_PROJECT__NAME:
				return NAME_EDEFAULT == null ? getName() != null : !NAME_EDEFAULT.equals(getName());
			case AuthPackage.OAUTH_PROJECT__OAUTH_URL:
				return OAUTH_URL_EDEFAULT == null ? getOauthUrl() != null : !OAUTH_URL_EDEFAULT.equals(getOauthUrl());
			case AuthPackage.OAUTH_PROJECT__OAUTH_TOKEN_URL:
				return OAUTH_TOKEN_URL_EDEFAULT == null ? getOauthTokenUrl() != null : !OAUTH_TOKEN_URL_EDEFAULT.equals(getOauthTokenUrl());
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_ID:
				return OAUTH_CLIENT_ID_EDEFAULT == null ? getOauthClientId() != null : !OAUTH_CLIENT_ID_EDEFAULT.equals(getOauthClientId());
			case AuthPackage.OAUTH_PROJECT__OAUTH_CLIENT_SECRET:
				return OAUTH_CLIENT_SECRET_EDEFAULT == null ? getOauthClientSecret() != null : !OAUTH_CLIENT_SECRET_EDEFAULT.equals(getOauthClientSecret());
			case AuthPackage.OAUTH_PROJECT__OAUTH_REDIRECT_URL:
				return OAUTH_REDIRECT_URL_EDEFAULT == null ? getOauthRedirectUrl() != null : !OAUTH_REDIRECT_URL_EDEFAULT.equals(getOauthRedirectUrl());
		}
		return super.eIsSet(featureID);
	}

} //OAuthProjectImpl
