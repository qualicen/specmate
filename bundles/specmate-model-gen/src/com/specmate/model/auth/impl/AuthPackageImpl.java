/**
 */
package com.specmate.model.auth.impl;

import com.specmate.model.administration.AdministrationPackage;

import com.specmate.model.administration.impl.AdministrationPackageImpl;

import com.specmate.model.auth.AuthFactory;
import com.specmate.model.auth.AuthPackage;
import com.specmate.model.auth.IAuthProject;
import com.specmate.model.auth.OAuthProject;
import com.specmate.model.auth.UserPasswordAuthProject;
import com.specmate.model.base.BasePackage;

import com.specmate.model.base.impl.BasePackageImpl;

import com.specmate.model.batch.BatchPackage;

import com.specmate.model.batch.impl.BatchPackageImpl;

import com.specmate.model.export.ExportPackage;

import com.specmate.model.export.impl.ExportPackageImpl;

import com.specmate.model.history.HistoryPackage;

import com.specmate.model.history.impl.HistoryPackageImpl;

import com.specmate.model.processes.ProcessesPackage;

import com.specmate.model.processes.impl.ProcessesPackageImpl;

import com.specmate.model.requirements.RequirementsPackage;

import com.specmate.model.requirements.impl.RequirementsPackageImpl;

import com.specmate.model.testspecification.TestspecificationPackage;

import com.specmate.model.testspecification.impl.TestspecificationPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class AuthPackageImpl extends EPackageImpl implements AuthPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iAuthProjectEClass = null;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass userPasswordAuthProjectEClass = null;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oAuthProjectEClass = null;
	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see com.specmate.model.auth.AuthPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private AuthPackageImpl() {
		super(eNS_URI, AuthFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link AuthPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static AuthPackage init() {
		if (isInited) return (AuthPackage)EPackage.Registry.INSTANCE.getEPackage(AuthPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredAuthPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		AuthPackageImpl theAuthPackage = registeredAuthPackage instanceof AuthPackageImpl ? (AuthPackageImpl)registeredAuthPackage : new AuthPackageImpl();

		isInited = true;

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(BasePackage.eNS_URI);
		BasePackageImpl theBasePackage = (BasePackageImpl)(registeredPackage instanceof BasePackageImpl ? registeredPackage : BasePackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(RequirementsPackage.eNS_URI);
		RequirementsPackageImpl theRequirementsPackage = (RequirementsPackageImpl)(registeredPackage instanceof RequirementsPackageImpl ? registeredPackage : RequirementsPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TestspecificationPackage.eNS_URI);
		TestspecificationPackageImpl theTestspecificationPackage = (TestspecificationPackageImpl)(registeredPackage instanceof TestspecificationPackageImpl ? registeredPackage : TestspecificationPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ProcessesPackage.eNS_URI);
		ProcessesPackageImpl theProcessesPackage = (ProcessesPackageImpl)(registeredPackage instanceof ProcessesPackageImpl ? registeredPackage : ProcessesPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(HistoryPackage.eNS_URI);
		HistoryPackageImpl theHistoryPackage = (HistoryPackageImpl)(registeredPackage instanceof HistoryPackageImpl ? registeredPackage : HistoryPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(AdministrationPackage.eNS_URI);
		AdministrationPackageImpl theAdministrationPackage = (AdministrationPackageImpl)(registeredPackage instanceof AdministrationPackageImpl ? registeredPackage : AdministrationPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(BatchPackage.eNS_URI);
		BatchPackageImpl theBatchPackage = (BatchPackageImpl)(registeredPackage instanceof BatchPackageImpl ? registeredPackage : BatchPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ExportPackage.eNS_URI);
		ExportPackageImpl theExportPackage = (ExportPackageImpl)(registeredPackage instanceof ExportPackageImpl ? registeredPackage : ExportPackage.eINSTANCE);

		// Create package meta-data objects
		theAuthPackage.createPackageContents();
		theBasePackage.createPackageContents();
		theRequirementsPackage.createPackageContents();
		theTestspecificationPackage.createPackageContents();
		theProcessesPackage.createPackageContents();
		theHistoryPackage.createPackageContents();
		theAdministrationPackage.createPackageContents();
		theBatchPackage.createPackageContents();
		theExportPackage.createPackageContents();

		// Initialize created meta-data
		theAuthPackage.initializePackageContents();
		theBasePackage.initializePackageContents();
		theRequirementsPackage.initializePackageContents();
		theTestspecificationPackage.initializePackageContents();
		theProcessesPackage.initializePackageContents();
		theHistoryPackage.initializePackageContents();
		theAdministrationPackage.initializePackageContents();
		theBatchPackage.initializePackageContents();
		theExportPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theAuthPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(AuthPackage.eNS_URI, theAuthPackage);
		return theAuthPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIAuthProject() {
		return iAuthProjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAuthProject_Name() {
		return (EAttribute)iAuthProjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getUserPasswordAuthProject() {
		return userPasswordAuthProjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOAuthProject() {
		return oAuthProjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOAuthProject_OauthUrl() {
		return (EAttribute)oAuthProjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOAuthProject_OauthTokenUrl() {
		return (EAttribute)oAuthProjectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOAuthProject_OauthClientId() {
		return (EAttribute)oAuthProjectEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOAuthProject_OauthClientSecret() {
		return (EAttribute)oAuthProjectEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOAuthProject_OauthRedirectUrl() {
		return (EAttribute)oAuthProjectEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AuthFactory getAuthFactory() {
		return (AuthFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		iAuthProjectEClass = createEClass(IAUTH_PROJECT);
		createEAttribute(iAuthProjectEClass, IAUTH_PROJECT__NAME);

		userPasswordAuthProjectEClass = createEClass(USER_PASSWORD_AUTH_PROJECT);

		oAuthProjectEClass = createEClass(OAUTH_PROJECT);
		createEAttribute(oAuthProjectEClass, OAUTH_PROJECT__OAUTH_URL);
		createEAttribute(oAuthProjectEClass, OAUTH_PROJECT__OAUTH_TOKEN_URL);
		createEAttribute(oAuthProjectEClass, OAUTH_PROJECT__OAUTH_CLIENT_ID);
		createEAttribute(oAuthProjectEClass, OAUTH_PROJECT__OAUTH_CLIENT_SECRET);
		createEAttribute(oAuthProjectEClass, OAUTH_PROJECT__OAUTH_REDIRECT_URL);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		userPasswordAuthProjectEClass.getESuperTypes().add(this.getIAuthProject());
		oAuthProjectEClass.getESuperTypes().add(this.getIAuthProject());

		// Initialize classes, features, and operations; add parameters
		initEClass(iAuthProjectEClass, IAuthProject.class, "IAuthProject", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIAuthProject_Name(), ecorePackage.getEString(), "name", null, 0, 1, IAuthProject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(userPasswordAuthProjectEClass, UserPasswordAuthProject.class, "UserPasswordAuthProject", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(oAuthProjectEClass, OAuthProject.class, "OAuthProject", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOAuthProject_OauthUrl(), ecorePackage.getEString(), "oauthUrl", null, 0, 1, OAuthProject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOAuthProject_OauthTokenUrl(), ecorePackage.getEString(), "oauthTokenUrl", null, 0, 1, OAuthProject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOAuthProject_OauthClientId(), ecorePackage.getEString(), "oauthClientId", null, 0, 1, OAuthProject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOAuthProject_OauthClientSecret(), ecorePackage.getEString(), "oauthClientSecret", null, 0, 1, OAuthProject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOAuthProject_OauthRedirectUrl(), ecorePackage.getEString(), "oauthRedirectUrl", null, 0, 1, OAuthProject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //AuthPackageImpl
