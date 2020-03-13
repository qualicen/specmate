import { CEGNode } from '../../model/CEGNode';
import { IContainer } from '../../model/IContainer';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';
import { ValidationUtil } from '../validation-util';
import { ValidationErrorSeverity } from '../validation-error-severity';

/**
 * Detects nodes in a model that have a character in the variable name that is not allowed for
 * element names. As from the variable name Specmate contstruct Testparameters with the same name, this
 * would cause problems.
 */

@Validator(CEGNode)
export class InvalidNodeVariableValidator extends ElementValidatorBase<CEGNode> {

    public validate(element: CEGNode, contents: IContainer[]): ValidationResult {

        if (!this.isValidNode(element)) {
            let message = ValidationMessage.ERROR_INVALID_VARIABLE;
            return new ValidationResult(message, false, [element], ValidationErrorSeverity.SAVE_DISABLED);
        }
        return ValidationResult.VALID;

    }

    /* Checks if a CEG node has a valid variable name */
    private isValidNode(element: CEGNode): boolean {
        if (element === undefined || element.variable === undefined) {
            return true;
        }
        if (!ValidationUtil.isValidName(element.variable)) {
            return false;
        }
        return true;
    }
}
