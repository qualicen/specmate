/**
 */
package com.specmate.model.processes.impl;

import com.specmate.model.base.impl.IModelConnectionImpl;

import com.specmate.model.processes.ProcessConnection;
import com.specmate.model.processes.ProcessesPackage;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Process Connection</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.processes.impl.ProcessConnectionImpl#getCondition <em>Condition</em>}</li>
 *   <li>{@link com.specmate.model.processes.impl.ProcessConnectionImpl#getLabelX <em>Label X</em>}</li>
 *   <li>{@link com.specmate.model.processes.impl.ProcessConnectionImpl#getLabelY <em>Label Y</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ProcessConnectionImpl extends IModelConnectionImpl implements ProcessConnection {
	/**
	 * The default value of the '{@link #getCondition() <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCondition()
	 * @generated
	 * @ordered
	 */
	protected static final String CONDITION_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getLabelX() <em>Label X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabelX()
	 * @generated
	 * @ordered
	 */
	protected static final double LABEL_X_EDEFAULT = 0.0;

	/**
	 * The default value of the '{@link #getLabelY() <em>Label Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabelY()
	 * @generated
	 * @ordered
	 */
	protected static final double LABEL_Y_EDEFAULT = 0.0;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ProcessConnectionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProcessesPackage.Literals.PROCESS_CONNECTION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCondition() {
		return (String)eDynamicGet(ProcessesPackage.PROCESS_CONNECTION__CONDITION, ProcessesPackage.Literals.PROCESS_CONNECTION__CONDITION, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCondition(String newCondition) {
		eDynamicSet(ProcessesPackage.PROCESS_CONNECTION__CONDITION, ProcessesPackage.Literals.PROCESS_CONNECTION__CONDITION, newCondition);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getLabelX() {
		return (Double)eDynamicGet(ProcessesPackage.PROCESS_CONNECTION__LABEL_X, ProcessesPackage.Literals.PROCESS_CONNECTION__LABEL_X, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLabelX(double newLabelX) {
		eDynamicSet(ProcessesPackage.PROCESS_CONNECTION__LABEL_X, ProcessesPackage.Literals.PROCESS_CONNECTION__LABEL_X, newLabelX);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getLabelY() {
		return (Double)eDynamicGet(ProcessesPackage.PROCESS_CONNECTION__LABEL_Y, ProcessesPackage.Literals.PROCESS_CONNECTION__LABEL_Y, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLabelY(double newLabelY) {
		eDynamicSet(ProcessesPackage.PROCESS_CONNECTION__LABEL_Y, ProcessesPackage.Literals.PROCESS_CONNECTION__LABEL_Y, newLabelY);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ProcessesPackage.PROCESS_CONNECTION__CONDITION:
				return getCondition();
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_X:
				return getLabelX();
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_Y:
				return getLabelY();
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
			case ProcessesPackage.PROCESS_CONNECTION__CONDITION:
				setCondition((String)newValue);
				return;
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_X:
				setLabelX((Double)newValue);
				return;
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_Y:
				setLabelY((Double)newValue);
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
			case ProcessesPackage.PROCESS_CONNECTION__CONDITION:
				setCondition(CONDITION_EDEFAULT);
				return;
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_X:
				setLabelX(LABEL_X_EDEFAULT);
				return;
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_Y:
				setLabelY(LABEL_Y_EDEFAULT);
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
			case ProcessesPackage.PROCESS_CONNECTION__CONDITION:
				return CONDITION_EDEFAULT == null ? getCondition() != null : !CONDITION_EDEFAULT.equals(getCondition());
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_X:
				return getLabelX() != LABEL_X_EDEFAULT;
			case ProcessesPackage.PROCESS_CONNECTION__LABEL_Y:
				return getLabelY() != LABEL_Y_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

} //ProcessConnectionImpl
