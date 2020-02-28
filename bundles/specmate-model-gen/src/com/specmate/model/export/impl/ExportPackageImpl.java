/**
 */
package com.specmate.model.export.impl;

import com.specmate.model.administration.AdministrationPackage;

import com.specmate.model.administration.impl.AdministrationPackageImpl;

import com.specmate.model.base.BasePackage;

import com.specmate.model.base.impl.BasePackageImpl;

import com.specmate.model.batch.BatchPackage;

import com.specmate.model.batch.impl.BatchPackageImpl;

import com.specmate.model.export.Export;
import com.specmate.model.export.ExportFactory;
import com.specmate.model.export.ExportPackage;

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
public class ExportPackageImpl extends EPackageImpl implements ExportPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass exportEClass = null;

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
	 * @see com.specmate.model.export.ExportPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ExportPackageImpl() {
		super(eNS_URI, ExportFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ExportPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ExportPackage init() {
		if (isInited) return (ExportPackage)EPackage.Registry.INSTANCE.getEPackage(ExportPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredExportPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ExportPackageImpl theExportPackage = registeredExportPackage instanceof ExportPackageImpl ? (ExportPackageImpl)registeredExportPackage : new ExportPackageImpl();

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

		// Create package meta-data objects
		theExportPackage.createPackageContents();
		theBasePackage.createPackageContents();
		theRequirementsPackage.createPackageContents();
		theTestspecificationPackage.createPackageContents();
		theProcessesPackage.createPackageContents();
		theHistoryPackage.createPackageContents();
		theAdministrationPackage.createPackageContents();
		theBatchPackage.createPackageContents();

		// Initialize created meta-data
		theExportPackage.initializePackageContents();
		theBasePackage.initializePackageContents();
		theRequirementsPackage.initializePackageContents();
		theTestspecificationPackage.initializePackageContents();
		theProcessesPackage.initializePackageContents();
		theHistoryPackage.initializePackageContents();
		theAdministrationPackage.initializePackageContents();
		theBatchPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theExportPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ExportPackage.eNS_URI, theExportPackage);
		return theExportPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExport() {
		return exportEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExport_Type() {
		return (EAttribute)exportEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExport_Content() {
		return (EAttribute)exportEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExportFactory getExportFactory() {
		return (ExportFactory)getEFactoryInstance();
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
		exportEClass = createEClass(EXPORT);
		createEAttribute(exportEClass, EXPORT__TYPE);
		createEAttribute(exportEClass, EXPORT__CONTENT);
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

		// Obtain other dependent packages
		BasePackage theBasePackage = (BasePackage)EPackage.Registry.INSTANCE.getEPackage(BasePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		exportEClass.getESuperTypes().add(theBasePackage.getINamed());

		// Initialize classes, features, and operations; add parameters
		initEClass(exportEClass, Export.class, "Export", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getExport_Type(), ecorePackage.getEString(), "type", null, 0, 1, Export.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExport_Content(), ecorePackage.getEString(), "content", null, 0, 1, Export.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //ExportPackageImpl
