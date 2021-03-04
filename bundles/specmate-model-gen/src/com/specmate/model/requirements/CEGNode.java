/**
 */
package com.specmate.model.requirements;

import com.specmate.model.base.IModelNode;
import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>CEG Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.requirements.CEGNode#getType <em>Type</em>}</li>
 *   <li>{@link com.specmate.model.requirements.CEGNode#getVariable <em>Variable</em>}</li>
 *   <li>{@link com.specmate.model.requirements.CEGNode#getCondition <em>Condition</em>}</li>
 *   <li>{@link com.specmate.model.requirements.CEGNode#getLinksFrom <em>Links From</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.requirements.RequirementsPackage#getCEGNode()
 * @model annotation="http://specmate.com/form_meta disabled1='name' disabled2='description'"
 * @generated
 */
public interface CEGNode extends IModelNode {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link com.specmate.model.requirements.NodeType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see com.specmate.model.requirements.NodeType
	 * @see #setType(NodeType)
	 * @see com.specmate.model.requirements.RequirementsPackage#getCEGNode_Type()
	 * @model annotation="http://specmate.com/form_meta shortDesc='Type' longDesc='The type of a node' required='true' type='singleSelection' values='[\"AND\", \"OR\"]' position='3'"
	 * @generated
	 */
	NodeType getType();

	/**
	 * Sets the value of the '{@link com.specmate.model.requirements.CEGNode#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see com.specmate.model.requirements.NodeType
	 * @see #getType()
	 * @generated
	 */
	void setType(NodeType value);

	/**
	 * Returns the value of the '<em><b>Variable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Variable</em>' attribute.
	 * @see #setVariable(String)
	 * @see com.specmate.model.requirements.RequirementsPackage#getCEGNode_Variable()
	 * @model annotation="http://specmate.com/form_meta shortDesc='Variable' longDesc='The variable of a node' required='true' type='text' position='1' allowedPattern='^[^,;|]*$'"
	 * @generated
	 */
	String getVariable();

	/**
	 * Sets the value of the '{@link com.specmate.model.requirements.CEGNode#getVariable <em>Variable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Variable</em>' attribute.
	 * @see #getVariable()
	 * @generated
	 */
	void setVariable(String value);

	/**
	 * Returns the value of the '<em><b>Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Condition</em>' attribute.
	 * @see #setCondition(String)
	 * @see com.specmate.model.requirements.RequirementsPackage#getCEGNode_Condition()
	 * @model annotation="http://specmate.com/form_meta shortDesc='Condition' longDesc='The condition the variable has to fulfil' required='true' type='text' position='2'"
	 * @generated
	 */
	String getCondition();

	/**
	 * Sets the value of the '{@link com.specmate.model.requirements.CEGNode#getCondition <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Condition</em>' attribute.
	 * @see #getCondition()
	 * @generated
	 */
	void setCondition(String value);

	/**
	 * Returns the value of the '<em><b>Links From</b></em>' reference list.
	 * The list contents are of type {@link com.specmate.model.requirements.CEGLinkedNode}.
	 * It is bidirectional and its opposite is '{@link com.specmate.model.requirements.CEGLinkedNode#getLinkTo <em>Link To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Links From</em>' reference list.
	 * @see com.specmate.model.requirements.RequirementsPackage#getCEGNode_LinksFrom()
	 * @see com.specmate.model.requirements.CEGLinkedNode#getLinkTo
	 * @model opposite="linkTo"
	 * @generated
	 */
	EList<CEGLinkedNode> getLinksFrom();

} // CEGNode
