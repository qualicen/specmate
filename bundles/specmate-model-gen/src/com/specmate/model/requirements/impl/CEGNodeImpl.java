/**
 */
package com.specmate.model.requirements.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.InternalEList;

import com.specmate.model.base.impl.IModelNodeImpl;
import com.specmate.model.requirements.CEGLinkedNode;
import com.specmate.model.requirements.CEGNode;
import com.specmate.model.requirements.NodeType;
import com.specmate.model.requirements.RequirementsPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>CEG
 * Node</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link com.specmate.model.requirements.impl.CEGNodeImpl#getType
 * <em>Type</em>}</li>
 * <li>{@link com.specmate.model.requirements.impl.CEGNodeImpl#getVariable
 * <em>Variable</em>}</li>
 * <li>{@link com.specmate.model.requirements.impl.CEGNodeImpl#getCondition
 * <em>Condition</em>}</li>
 * <li>{@link com.specmate.model.requirements.impl.CEGNodeImpl#getLinksFrom
 * <em>Links From</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CEGNodeImpl extends IModelNodeImpl implements CEGNode {
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final NodeType TYPE_EDEFAULT = NodeType.AND;

	/**
	 * The default value of the '{@link #getVariable() <em>Variable</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see #getVariable()
	 * @generated
	 * @ordered
	 */
	protected static final String VARIABLE_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getCondition() <em>Condition</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see #getCondition()
	 * @generated
	 * @ordered
	 */
	protected static final String CONDITION_EDEFAULT = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	protected CEGNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RequirementsPackage.Literals.CEG_NODE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public NodeType getType() {
		return (NodeType) eDynamicGet(RequirementsPackage.CEG_NODE__TYPE, RequirementsPackage.Literals.CEG_NODE__TYPE,
				true, true);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public void setType(NodeType newType) {
		eDynamicSet(RequirementsPackage.CEG_NODE__TYPE, RequirementsPackage.Literals.CEG_NODE__TYPE, newType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public String getVariable() {
		return (String) eDynamicGet(RequirementsPackage.CEG_NODE__VARIABLE,
				RequirementsPackage.Literals.CEG_NODE__VARIABLE, true, true);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public void setVariable(String newVariable) {
		eDynamicSet(RequirementsPackage.CEG_NODE__VARIABLE, RequirementsPackage.Literals.CEG_NODE__VARIABLE,
				newVariable);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public String getCondition() {
		return (String) eDynamicGet(RequirementsPackage.CEG_NODE__CONDITION,
				RequirementsPackage.Literals.CEG_NODE__CONDITION, true, true);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public void setCondition(String newCondition) {
		eDynamicSet(RequirementsPackage.CEG_NODE__CONDITION, RequirementsPackage.Literals.CEG_NODE__CONDITION,
				newCondition);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<CEGLinkedNode> getLinksFrom() {
		return (EList<CEGLinkedNode>) eDynamicGet(RequirementsPackage.CEG_NODE__LINKS_FROM,
				RequirementsPackage.Literals.CEG_NODE__LINKS_FROM, true, true);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case RequirementsPackage.CEG_NODE__LINKS_FROM:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getLinksFrom()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case RequirementsPackage.CEG_NODE__LINKS_FROM:
			return ((InternalEList<?>) getLinksFrom()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case RequirementsPackage.CEG_NODE__TYPE:
			return getType();
		case RequirementsPackage.CEG_NODE__VARIABLE:
			return getVariable();
		case RequirementsPackage.CEG_NODE__CONDITION:
			return getCondition();
		case RequirementsPackage.CEG_NODE__LINKS_FROM:
			return getLinksFrom();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case RequirementsPackage.CEG_NODE__TYPE:
			setType((NodeType) newValue);
			return;
		case RequirementsPackage.CEG_NODE__VARIABLE:
			setVariable((String) newValue);
			return;
		case RequirementsPackage.CEG_NODE__CONDITION:
			setCondition((String) newValue);
			return;
		case RequirementsPackage.CEG_NODE__LINKS_FROM:
			getLinksFrom().clear();
			getLinksFrom().addAll((Collection<? extends CEGLinkedNode>) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case RequirementsPackage.CEG_NODE__TYPE:
			setType(TYPE_EDEFAULT);
			return;
		case RequirementsPackage.CEG_NODE__VARIABLE:
			setVariable(VARIABLE_EDEFAULT);
			return;
		case RequirementsPackage.CEG_NODE__CONDITION:
			setCondition(CONDITION_EDEFAULT);
			return;
		case RequirementsPackage.CEG_NODE__LINKS_FROM:
			getLinksFrom().clear();
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case RequirementsPackage.CEG_NODE__TYPE:
			return getType() != TYPE_EDEFAULT;
		case RequirementsPackage.CEG_NODE__VARIABLE:
			return VARIABLE_EDEFAULT == null ? getVariable() != null : !VARIABLE_EDEFAULT.equals(getVariable());
		case RequirementsPackage.CEG_NODE__CONDITION:
			return CONDITION_EDEFAULT == null ? getCondition() != null : !CONDITION_EDEFAULT.equals(getCondition());
		case RequirementsPackage.CEG_NODE__LINKS_FROM:
			return !getLinksFrom().isEmpty();
		}
		return super.eIsSet(featureID);
	}

	@Override
	/** @generated not */
	public String toString() {
		return getVariable() + "-" + getCondition();
	}

}
// CEGNodeImpl
