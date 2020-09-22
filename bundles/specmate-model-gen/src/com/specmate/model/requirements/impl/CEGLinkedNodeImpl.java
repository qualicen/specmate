/**
 */
package com.specmate.model.requirements.impl;

import com.specmate.model.base.impl.IModelNodeImpl;

import com.specmate.model.requirements.CEGLinkedNode;
import com.specmate.model.requirements.CEGNode;
import com.specmate.model.requirements.RequirementsPackage;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>CEG Linked Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.requirements.impl.CEGLinkedNodeImpl#getLinkTo <em>Link To</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CEGLinkedNodeImpl extends IModelNodeImpl implements CEGLinkedNode {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CEGLinkedNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RequirementsPackage.Literals.CEG_LINKED_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CEGNode getLinkTo() {
		return (CEGNode)eDynamicGet(RequirementsPackage.CEG_LINKED_NODE__LINK_TO, RequirementsPackage.Literals.CEG_LINKED_NODE__LINK_TO, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CEGNode basicGetLinkTo() {
		return (CEGNode)eDynamicGet(RequirementsPackage.CEG_LINKED_NODE__LINK_TO, RequirementsPackage.Literals.CEG_LINKED_NODE__LINK_TO, false, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetLinkTo(CEGNode newLinkTo, NotificationChain msgs) {
		msgs = eDynamicInverseAdd((InternalEObject)newLinkTo, RequirementsPackage.CEG_LINKED_NODE__LINK_TO, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLinkTo(CEGNode newLinkTo) {
		eDynamicSet(RequirementsPackage.CEG_LINKED_NODE__LINK_TO, RequirementsPackage.Literals.CEG_LINKED_NODE__LINK_TO, newLinkTo);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RequirementsPackage.CEG_LINKED_NODE__LINK_TO:
				CEGNode linkTo = basicGetLinkTo();
				if (linkTo != null)
					msgs = ((InternalEObject)linkTo).eInverseRemove(this, RequirementsPackage.CEG_NODE__LINKS_FROM, CEGNode.class, msgs);
				return basicSetLinkTo((CEGNode)otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RequirementsPackage.CEG_LINKED_NODE__LINK_TO:
				return basicSetLinkTo(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RequirementsPackage.CEG_LINKED_NODE__LINK_TO:
				if (resolve) return getLinkTo();
				return basicGetLinkTo();
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
			case RequirementsPackage.CEG_LINKED_NODE__LINK_TO:
				setLinkTo((CEGNode)newValue);
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
			case RequirementsPackage.CEG_LINKED_NODE__LINK_TO:
				setLinkTo((CEGNode)null);
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
			case RequirementsPackage.CEG_LINKED_NODE__LINK_TO:
				return basicGetLinkTo() != null;
		}
		return super.eIsSet(featureID);
	}

} //CEGLinkedNodeImpl
