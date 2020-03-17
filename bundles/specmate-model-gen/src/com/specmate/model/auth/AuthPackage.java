/**
 */
package com.specmate.model.auth;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see com.specmate.model.auth.AuthFactory
 * @model kind="package"
 * @generated
 */
public interface AuthPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "auth";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://specmate.com/20200228/model/auth";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "com.specmate.model.auth";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	AuthPackage eINSTANCE = com.specmate.model.auth.impl.AuthPackageImpl.init();

	/**
	 * The meta object id for the '{@link com.specmate.model.auth.IAuthProject <em>IAuth Project</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.specmate.model.auth.IAuthProject
	 * @see com.specmate.model.auth.impl.AuthPackageImpl#getIAuthProject()
	 * @generated
	 */
	int IAUTH_PROJECT = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAUTH_PROJECT__NAME = 0;

	/**
	 * The number of structural features of the '<em>IAuth Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAUTH_PROJECT_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>IAuth Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAUTH_PROJECT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link com.specmate.model.auth.impl.UserPasswordAuthProjectImpl <em>User Password Auth Project</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.specmate.model.auth.impl.UserPasswordAuthProjectImpl
	 * @see com.specmate.model.auth.impl.AuthPackageImpl#getUserPasswordAuthProject()
	 * @generated
	 */
	int USER_PASSWORD_AUTH_PROJECT = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PASSWORD_AUTH_PROJECT__NAME = IAUTH_PROJECT__NAME;

	/**
	 * The number of structural features of the '<em>User Password Auth Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PASSWORD_AUTH_PROJECT_FEATURE_COUNT = IAUTH_PROJECT_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>User Password Auth Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PASSWORD_AUTH_PROJECT_OPERATION_COUNT = IAUTH_PROJECT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link com.specmate.model.auth.impl.OAuthProjectImpl <em>OAuth Project</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.specmate.model.auth.impl.OAuthProjectImpl
	 * @see com.specmate.model.auth.impl.AuthPackageImpl#getOAuthProject()
	 * @generated
	 */
	int OAUTH_PROJECT = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT__NAME = IAUTH_PROJECT__NAME;

	/**
	 * The feature id for the '<em><b>Oauth Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT__OAUTH_URL = IAUTH_PROJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Oauth Token Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT__OAUTH_TOKEN_URL = IAUTH_PROJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Oauth Client Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT__OAUTH_CLIENT_ID = IAUTH_PROJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Oauth Client Secret</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT__OAUTH_CLIENT_SECRET = IAUTH_PROJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Oauth Redirect Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT__OAUTH_REDIRECT_URL = IAUTH_PROJECT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>OAuth Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT_FEATURE_COUNT = IAUTH_PROJECT_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>OAuth Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OAUTH_PROJECT_OPERATION_COUNT = IAUTH_PROJECT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link com.specmate.model.auth.IAuthProject <em>IAuth Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IAuth Project</em>'.
	 * @see com.specmate.model.auth.IAuthProject
	 * @generated
	 */
	EClass getIAuthProject();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.IAuthProject#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see com.specmate.model.auth.IAuthProject#getName()
	 * @see #getIAuthProject()
	 * @generated
	 */
	EAttribute getIAuthProject_Name();

	/**
	 * Returns the meta object for class '{@link com.specmate.model.auth.UserPasswordAuthProject <em>User Password Auth Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>User Password Auth Project</em>'.
	 * @see com.specmate.model.auth.UserPasswordAuthProject
	 * @generated
	 */
	EClass getUserPasswordAuthProject();

	/**
	 * Returns the meta object for class '{@link com.specmate.model.auth.OAuthProject <em>OAuth Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OAuth Project</em>'.
	 * @see com.specmate.model.auth.OAuthProject
	 * @generated
	 */
	EClass getOAuthProject();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.OAuthProject#getOauthUrl <em>Oauth Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Oauth Url</em>'.
	 * @see com.specmate.model.auth.OAuthProject#getOauthUrl()
	 * @see #getOAuthProject()
	 * @generated
	 */
	EAttribute getOAuthProject_OauthUrl();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.OAuthProject#getOauthTokenUrl <em>Oauth Token Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Oauth Token Url</em>'.
	 * @see com.specmate.model.auth.OAuthProject#getOauthTokenUrl()
	 * @see #getOAuthProject()
	 * @generated
	 */
	EAttribute getOAuthProject_OauthTokenUrl();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.OAuthProject#getOauthClientId <em>Oauth Client Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Oauth Client Id</em>'.
	 * @see com.specmate.model.auth.OAuthProject#getOauthClientId()
	 * @see #getOAuthProject()
	 * @generated
	 */
	EAttribute getOAuthProject_OauthClientId();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.OAuthProject#getOauthClientSecret <em>Oauth Client Secret</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Oauth Client Secret</em>'.
	 * @see com.specmate.model.auth.OAuthProject#getOauthClientSecret()
	 * @see #getOAuthProject()
	 * @generated
	 */
	EAttribute getOAuthProject_OauthClientSecret();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.OAuthProject#getOauthRedirectUrl <em>Oauth Redirect Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Oauth Redirect Url</em>'.
	 * @see com.specmate.model.auth.OAuthProject#getOauthRedirectUrl()
	 * @see #getOAuthProject()
	 * @generated
	 */
	EAttribute getOAuthProject_OauthRedirectUrl();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	AuthFactory getAuthFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link com.specmate.model.auth.IAuthProject <em>IAuth Project</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.specmate.model.auth.IAuthProject
		 * @see com.specmate.model.auth.impl.AuthPackageImpl#getIAuthProject()
		 * @generated
		 */
		EClass IAUTH_PROJECT = eINSTANCE.getIAuthProject();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAUTH_PROJECT__NAME = eINSTANCE.getIAuthProject_Name();

		/**
		 * The meta object literal for the '{@link com.specmate.model.auth.impl.UserPasswordAuthProjectImpl <em>User Password Auth Project</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.specmate.model.auth.impl.UserPasswordAuthProjectImpl
		 * @see com.specmate.model.auth.impl.AuthPackageImpl#getUserPasswordAuthProject()
		 * @generated
		 */
		EClass USER_PASSWORD_AUTH_PROJECT = eINSTANCE.getUserPasswordAuthProject();

		/**
		 * The meta object literal for the '{@link com.specmate.model.auth.impl.OAuthProjectImpl <em>OAuth Project</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.specmate.model.auth.impl.OAuthProjectImpl
		 * @see com.specmate.model.auth.impl.AuthPackageImpl#getOAuthProject()
		 * @generated
		 */
		EClass OAUTH_PROJECT = eINSTANCE.getOAuthProject();

		/**
		 * The meta object literal for the '<em><b>Oauth Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OAUTH_PROJECT__OAUTH_URL = eINSTANCE.getOAuthProject_OauthUrl();

		/**
		 * The meta object literal for the '<em><b>Oauth Token Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OAUTH_PROJECT__OAUTH_TOKEN_URL = eINSTANCE.getOAuthProject_OauthTokenUrl();

		/**
		 * The meta object literal for the '<em><b>Oauth Client Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OAUTH_PROJECT__OAUTH_CLIENT_ID = eINSTANCE.getOAuthProject_OauthClientId();

		/**
		 * The meta object literal for the '<em><b>Oauth Client Secret</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OAUTH_PROJECT__OAUTH_CLIENT_SECRET = eINSTANCE.getOAuthProject_OauthClientSecret();

		/**
		 * The meta object literal for the '<em><b>Oauth Redirect Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OAUTH_PROJECT__OAUTH_REDIRECT_URL = eINSTANCE.getOAuthProject_OauthRedirectUrl();

	}

} //AuthPackage
