/**
 */
package com.specmate.model.processes;

import com.specmate.model.base.IModelConnection;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Process Connection</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.processes.ProcessConnection#getCondition <em>Condition</em>}</li>
 *   <li>{@link com.specmate.model.processes.ProcessConnection#getLabelX <em>Label X</em>}</li>
 *   <li>{@link com.specmate.model.processes.ProcessConnection#getLabelY <em>Label Y</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.processes.ProcessesPackage#getProcessConnection()
 * @model annotation="http://specmate.com/form_meta disabled1='name'"
 * @generated
 */
public interface ProcessConnection extends IModelConnection {
	/**
	 * Returns the value of the '<em><b>Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Condition</em>' attribute.
	 * @see #setCondition(String)
	 * @see com.specmate.model.processes.ProcessesPackage#getProcessConnection_Condition()
	 * @model annotation="http://specmate.com/form_meta shortDesc='Condition' longDesc='The condition the variable has to fulfil' required='false' type='text' position='2'"
	 * @generated
	 */
	String getCondition();

	/**
	 * Sets the value of the '{@link com.specmate.model.processes.ProcessConnection#getCondition <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Condition</em>' attribute.
	 * @see #getCondition()
	 * @generated
	 */
	void setCondition(String value);

	/**
	 * Returns the value of the '<em><b>Label X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Label X</em>' attribute.
	 * @see #setLabelX(double)
	 * @see com.specmate.model.processes.ProcessesPackage#getProcessConnection_LabelX()
	 * @model
	 * @generated
	 */
	double getLabelX();

	/**
	 * Sets the value of the '{@link com.specmate.model.processes.ProcessConnection#getLabelX <em>Label X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Label X</em>' attribute.
	 * @see #getLabelX()
	 * @generated
	 */
	void setLabelX(double value);

	/**
	 * Returns the value of the '<em><b>Label Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Label Y</em>' attribute.
	 * @see #setLabelY(double)
	 * @see com.specmate.model.processes.ProcessesPackage#getProcessConnection_LabelY()
	 * @model
	 * @generated
	 */
	double getLabelY();

	/**
	 * Sets the value of the '{@link com.specmate.model.processes.ProcessConnection#getLabelY <em>Label Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Label Y</em>' attribute.
	 * @see #getLabelY()
	 * @generated
	 */
	void setLabelY(double value);

} // ProcessConnection
