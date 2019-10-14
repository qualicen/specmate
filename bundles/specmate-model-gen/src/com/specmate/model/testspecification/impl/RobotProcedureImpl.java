/**
 */
package com.specmate.model.testspecification.impl;

import com.specmate.model.base.BasePackage;
import com.specmate.model.base.IContentElement;
import com.specmate.model.base.IDescribed;
import com.specmate.model.base.IExternal;
import com.specmate.model.base.INamed;

import com.specmate.model.testspecification.RobotProcedure;
import com.specmate.model.testspecification.TestspecificationPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.emf.internal.cdo.CDOObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Robot Procedure</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#getId <em>Id</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#getName <em>Name</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#getContents <em>Contents</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#getExtId <em>Ext Id</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#getExtId2 <em>Ext Id2</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#getSource <em>Source</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#isLive <em>Live</em>}</li>
 *   <li>{@link com.specmate.model.testspecification.impl.RobotProcedureImpl#isIsRegressionTest <em>Is Regression Test</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RobotProcedureImpl extends CDOObjectImpl implements RobotProcedure {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getExtId() <em>Ext Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtId()
	 * @generated
	 * @ordered
	 */
	protected static final String EXT_ID_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getExtId2() <em>Ext Id2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtId2()
	 * @generated
	 * @ordered
	 */
	protected static final String EXT_ID2_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected static final String SOURCE_EDEFAULT = null;

	/**
	 * The default value of the '{@link #isLive() <em>Live</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isLive()
	 * @generated
	 * @ordered
	 */
	protected static final boolean LIVE_EDEFAULT = false;

	/**
	 * The default value of the '{@link #isIsRegressionTest() <em>Is Regression Test</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsRegressionTest()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_REGRESSION_TEST_EDEFAULT = false;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RobotProcedureImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TestspecificationPackage.Literals.ROBOT_PROCEDURE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected int eStaticFeatureCount() {
		return 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return (String)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__ID, BasePackage.Literals.IID__ID, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__ID, BasePackage.Literals.IID__ID, newId);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return (String)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__NAME, BasePackage.Literals.INAMED__NAME, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__NAME, BasePackage.Literals.INAMED__NAME, newName);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return (String)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION, BasePackage.Literals.IDESCRIBED__DESCRIPTION, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION, BasePackage.Literals.IDESCRIBED__DESCRIPTION, newDescription);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<IContentElement> getContents() {
		return (EList<IContentElement>)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__CONTENTS, BasePackage.Literals.ICONTAINER__CONTENTS, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getExtId() {
		return (String)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID, BasePackage.Literals.IEXTERNAL__EXT_ID, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExtId(String newExtId) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID, BasePackage.Literals.IEXTERNAL__EXT_ID, newExtId);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getExtId2() {
		return (String)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2, BasePackage.Literals.IEXTERNAL__EXT_ID2, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExtId2(String newExtId2) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2, BasePackage.Literals.IEXTERNAL__EXT_ID2, newExtId2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSource() {
		return (String)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__SOURCE, BasePackage.Literals.IEXTERNAL__SOURCE, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSource(String newSource) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__SOURCE, BasePackage.Literals.IEXTERNAL__SOURCE, newSource);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isLive() {
		return (Boolean)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__LIVE, BasePackage.Literals.IEXTERNAL__LIVE, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLive(boolean newLive) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__LIVE, BasePackage.Literals.IEXTERNAL__LIVE, newLive);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIsRegressionTest() {
		return (Boolean)eDynamicGet(TestspecificationPackage.ROBOT_PROCEDURE__IS_REGRESSION_TEST, TestspecificationPackage.Literals.ROBOT_PROCEDURE__IS_REGRESSION_TEST, true, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIsRegressionTest(boolean newIsRegressionTest) {
		eDynamicSet(TestspecificationPackage.ROBOT_PROCEDURE__IS_REGRESSION_TEST, TestspecificationPackage.Literals.ROBOT_PROCEDURE__IS_REGRESSION_TEST, newIsRegressionTest);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TestspecificationPackage.ROBOT_PROCEDURE__CONTENTS:
				return ((InternalEList<?>)getContents()).basicRemove(otherEnd, msgs);
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
			case TestspecificationPackage.ROBOT_PROCEDURE__ID:
				return getId();
			case TestspecificationPackage.ROBOT_PROCEDURE__NAME:
				return getName();
			case TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION:
				return getDescription();
			case TestspecificationPackage.ROBOT_PROCEDURE__CONTENTS:
				return getContents();
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID:
				return getExtId();
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2:
				return getExtId2();
			case TestspecificationPackage.ROBOT_PROCEDURE__SOURCE:
				return getSource();
			case TestspecificationPackage.ROBOT_PROCEDURE__LIVE:
				return isLive();
			case TestspecificationPackage.ROBOT_PROCEDURE__IS_REGRESSION_TEST:
				return isIsRegressionTest();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TestspecificationPackage.ROBOT_PROCEDURE__ID:
				setId((String)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__NAME:
				setName((String)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__CONTENTS:
				getContents().clear();
				getContents().addAll((Collection<? extends IContentElement>)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID:
				setExtId((String)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2:
				setExtId2((String)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__SOURCE:
				setSource((String)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__LIVE:
				setLive((Boolean)newValue);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__IS_REGRESSION_TEST:
				setIsRegressionTest((Boolean)newValue);
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
			case TestspecificationPackage.ROBOT_PROCEDURE__ID:
				setId(ID_EDEFAULT);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__CONTENTS:
				getContents().clear();
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID:
				setExtId(EXT_ID_EDEFAULT);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2:
				setExtId2(EXT_ID2_EDEFAULT);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__SOURCE:
				setSource(SOURCE_EDEFAULT);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__LIVE:
				setLive(LIVE_EDEFAULT);
				return;
			case TestspecificationPackage.ROBOT_PROCEDURE__IS_REGRESSION_TEST:
				setIsRegressionTest(IS_REGRESSION_TEST_EDEFAULT);
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
			case TestspecificationPackage.ROBOT_PROCEDURE__ID:
				return ID_EDEFAULT == null ? getId() != null : !ID_EDEFAULT.equals(getId());
			case TestspecificationPackage.ROBOT_PROCEDURE__NAME:
				return NAME_EDEFAULT == null ? getName() != null : !NAME_EDEFAULT.equals(getName());
			case TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? getDescription() != null : !DESCRIPTION_EDEFAULT.equals(getDescription());
			case TestspecificationPackage.ROBOT_PROCEDURE__CONTENTS:
				return !getContents().isEmpty();
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID:
				return EXT_ID_EDEFAULT == null ? getExtId() != null : !EXT_ID_EDEFAULT.equals(getExtId());
			case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2:
				return EXT_ID2_EDEFAULT == null ? getExtId2() != null : !EXT_ID2_EDEFAULT.equals(getExtId2());
			case TestspecificationPackage.ROBOT_PROCEDURE__SOURCE:
				return SOURCE_EDEFAULT == null ? getSource() != null : !SOURCE_EDEFAULT.equals(getSource());
			case TestspecificationPackage.ROBOT_PROCEDURE__LIVE:
				return isLive() != LIVE_EDEFAULT;
			case TestspecificationPackage.ROBOT_PROCEDURE__IS_REGRESSION_TEST:
				return isIsRegressionTest() != IS_REGRESSION_TEST_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == INamed.class) {
			switch (derivedFeatureID) {
				case TestspecificationPackage.ROBOT_PROCEDURE__NAME: return BasePackage.INAMED__NAME;
				default: return -1;
			}
		}
		if (baseClass == IDescribed.class) {
			switch (derivedFeatureID) {
				case TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION: return BasePackage.IDESCRIBED__DESCRIPTION;
				default: return -1;
			}
		}
		if (baseClass == IExternal.class) {
			switch (derivedFeatureID) {
				case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID: return BasePackage.IEXTERNAL__EXT_ID;
				case TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2: return BasePackage.IEXTERNAL__EXT_ID2;
				case TestspecificationPackage.ROBOT_PROCEDURE__SOURCE: return BasePackage.IEXTERNAL__SOURCE;
				case TestspecificationPackage.ROBOT_PROCEDURE__LIVE: return BasePackage.IEXTERNAL__LIVE;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == INamed.class) {
			switch (baseFeatureID) {
				case BasePackage.INAMED__NAME: return TestspecificationPackage.ROBOT_PROCEDURE__NAME;
				default: return -1;
			}
		}
		if (baseClass == IDescribed.class) {
			switch (baseFeatureID) {
				case BasePackage.IDESCRIBED__DESCRIPTION: return TestspecificationPackage.ROBOT_PROCEDURE__DESCRIPTION;
				default: return -1;
			}
		}
		if (baseClass == IExternal.class) {
			switch (baseFeatureID) {
				case BasePackage.IEXTERNAL__EXT_ID: return TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID;
				case BasePackage.IEXTERNAL__EXT_ID2: return TestspecificationPackage.ROBOT_PROCEDURE__EXT_ID2;
				case BasePackage.IEXTERNAL__SOURCE: return TestspecificationPackage.ROBOT_PROCEDURE__SOURCE;
				case BasePackage.IEXTERNAL__LIVE: return TestspecificationPackage.ROBOT_PROCEDURE__LIVE;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

} //RobotProcedureImpl
