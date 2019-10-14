/**
 */
package com.specmate.model.testspecification;

import com.specmate.model.base.IContentElement;
import com.specmate.model.base.IPositionable;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Robot Step</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.testspecification.RobotStep#getExpectedOutcome <em>Expected Outcome</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.RobotStep#getReferencedTestParameters <em>Referenced Test Parameters</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.testspecification.TestspecificationPackage#getRobotStep()
 * @model
 * @generated
 */
public interface RobotStep extends IContentElement, IPositionable {
	/**
	 * Returns the value of the '<em><b>Expected Outcome</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Expected Outcome</em>' attribute.
	 * @see #setExpectedOutcome(String)
	 * @see com.specmate.model.testspecification.TestspecificationPackage#getRobotStep_ExpectedOutcome()
	 * @model
	 * @generated
	 */
	String getExpectedOutcome();

	/**
	 * Sets the value of the '{@link com.specmate.model.testspecification.RobotStep#getExpectedOutcome <em>Expected Outcome</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expected Outcome</em>' attribute.
	 * @see #getExpectedOutcome()
	 * @generated
	 */
	void setExpectedOutcome(String value);

	/**
	 * Returns the value of the '<em><b>Referenced Test Parameters</b></em>' reference list.
	 * The list contents are of type {@link com.specmate.model.testspecification.TestParameter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Referenced Test Parameters</em>' reference list.
	 * @see com.specmate.model.testspecification.TestspecificationPackage#getRobotStep_ReferencedTestParameters()
	 * @model
	 * @generated
	 */
	EList<TestParameter> getReferencedTestParameters();

} // RobotStep
