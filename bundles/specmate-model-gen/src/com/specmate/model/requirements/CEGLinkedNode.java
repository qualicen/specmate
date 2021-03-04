/**
 */
package com.specmate.model.requirements;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>CEG Linked Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.model.requirements.CEGLinkedNode#getLinkTo <em>Link To</em>}</li>
 * </ul>
 *
 * @see com.specmate.model.requirements.RequirementsPackage#getCEGLinkedNode()
 * @model annotation="http://specmate.com/form_meta disabled1='name' disabled2='description'"
 * @generated
 */
public interface CEGLinkedNode extends CEGNode {
	/**
	 * Returns the value of the '<em><b>Link To</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link com.specmate.model.requirements.CEGNode#getLinksFrom <em>Links From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Link To</em>' reference.
	 * @see #setLinkTo(CEGNode)
	 * @see com.specmate.model.requirements.RequirementsPackage#getCEGLinkedNode_LinkTo()
	 * @see com.specmate.model.requirements.CEGNode#getLinksFrom
	 * @model opposite="linksFrom"
	 * @generated
	 */
	CEGNode getLinkTo();

	/**
	 * Sets the value of the '{@link com.specmate.model.requirements.CEGLinkedNode#getLinkTo <em>Link To</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Link To</em>' reference.
	 * @see #getLinkTo()
	 * @generated
	 */
	void setLinkTo(CEGNode value);

} // CEGLinkedNode
