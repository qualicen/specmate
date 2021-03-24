/**
 */
package com.specmate.migration.test.attributeadded.testmodel.artefact;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagram Link</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.specmate.migration.test.attributeadded.testmodel.artefact.DiagramLink#getLinkedDiagram <em>Linked Diagram</em>}</li>
 * </ul>
 *
 * @see com.specmate.migration.test.attributeadded.testmodel.artefact.ArtefactPackage#getDiagramLink()
 * @model
 * @generated
 */
public interface DiagramLink extends EObject {
	/**
	 * Returns the value of the '<em><b>Linked Diagram</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link com.specmate.migration.test.attributeadded.testmodel.artefact.Diagram#getLinkfrom <em>Linkfrom</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Linked Diagram</em>' reference.
	 * @see #setLinkedDiagram(Diagram)
	 * @see com.specmate.migration.test.attributeadded.testmodel.artefact.ArtefactPackage#getDiagramLink_LinkedDiagram()
	 * @see com.specmate.migration.test.attributeadded.testmodel.artefact.Diagram#getLinkfrom
	 * @model opposite="linkfrom"
	 * @generated
	 */
	Diagram getLinkedDiagram();

	/**
	 * Sets the value of the '{@link com.specmate.migration.test.attributeadded.testmodel.artefact.DiagramLink#getLinkedDiagram <em>Linked Diagram</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Linked Diagram</em>' reference.
	 * @see #getLinkedDiagram()
	 * @generated
	 */
	void setLinkedDiagram(Diagram value);

} // DiagramLink
