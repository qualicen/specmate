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
	 * The meta object id for the '{@link com.specmate.model.auth.impl.AuthProjectImpl <em>Project</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.specmate.model.auth.impl.AuthProjectImpl
	 * @see com.specmate.model.auth.impl.AuthPackageImpl#getAuthProject()
	 * @generated
	 */
	int AUTH_PROJECT = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AUTH_PROJECT__NAME = 0;

	/**
	 * The feature id for the '<em><b>Oauth Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AUTH_PROJECT__OAUTH_URL = 1;

	/**
	 * The number of structural features of the '<em>Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AUTH_PROJECT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AUTH_PROJECT_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link com.specmate.model.auth.AuthProject <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Project</em>'.
	 * @see com.specmate.model.auth.AuthProject
	 * @generated
	 */
	EClass getAuthProject();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.AuthProject#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see com.specmate.model.auth.AuthProject#getName()
	 * @see #getAuthProject()
	 * @generated
	 */
	EAttribute getAuthProject_Name();

	/**
	 * Returns the meta object for the attribute '{@link com.specmate.model.auth.AuthProject#getOauthUrl <em>Oauth Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Oauth Url</em>'.
	 * @see com.specmate.model.auth.AuthProject#getOauthUrl()
	 * @see #getAuthProject()
	 * @generated
	 */
	EAttribute getAuthProject_OauthUrl();

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
		 * The meta object literal for the '{@link com.specmate.model.auth.impl.AuthProjectImpl <em>Project</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.specmate.model.auth.impl.AuthProjectImpl
		 * @see com.specmate.model.auth.impl.AuthPackageImpl#getAuthProject()
		 * @generated
		 */
		EClass AUTH_PROJECT = eINSTANCE.getAuthProject();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AUTH_PROJECT__NAME = eINSTANCE.getAuthProject_Name();

		/**
		 * The meta object literal for the '<em><b>Oauth Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AUTH_PROJECT__OAUTH_URL = eINSTANCE.getAuthProject_OauthUrl();

	}

} //AuthPackage
