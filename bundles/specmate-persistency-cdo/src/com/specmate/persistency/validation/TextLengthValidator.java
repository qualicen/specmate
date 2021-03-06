package com.specmate.persistency.validation;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.specmate.common.exception.SpecmateValidationException;
import com.specmate.model.base.ModelImage;
import com.specmate.persistency.event.EChangeKind;

public class TextLengthValidator extends ValidatorBase {
	public static final int MAX_LENGTH = 4000;

	@Override
	public void changedObject(EObject object, EStructuralFeature feature, EChangeKind changeKind, Object oldValue,
			Object newValue, String objectClassName) throws SpecmateValidationException {
		checkLength(feature.getName(), newValue, object);
	}

	@Override
	public void removedObject(EObject object) throws SpecmateValidationException {
		// Nothing to check
	}

	@Override
	public void newObject(EObject object, String id, String className, Map<EStructuralFeature, Object> featureMap)
			throws SpecmateValidationException {

		for (Map.Entry<EStructuralFeature, Object> entry : featureMap.entrySet()) {
			checkLength(entry.getKey().getName(), entry.getValue(), object);
		}
	}

	private void checkLength(String featureName, Object o, EObject obj) throws SpecmateValidationException {
		if (o instanceof String && !(obj instanceof ModelImage)) {
			String v = (String) o;
			if (v.length() >= MAX_LENGTH) {
				throw new SpecmateValidationException(
						"The content of attribute " + featureName + " is too large (" + v.length()
								+ "). The maximum length is " + MAX_LENGTH + ".",
						getValidatorName(), getObjectName(obj));
			}
		}
	}

}
