/**
 */
package com.specmate.model.export;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see com.specmate.model.export.ExportPackage
 * @generated
 */
public interface ExportFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ExportFactory eINSTANCE = com.specmate.model.export.impl.ExportFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Export</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Export</em>'.
	 * @generated
	 */
	Export createExport();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ExportPackage getExportPackage();

} //ExportFactory
