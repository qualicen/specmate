import { CEGModel } from '../../model/CEGModel';
import { CEGNode } from '../../model/CEGNode';
import { IContainer } from '../../model/IContainer';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';
import { ValidNameValidator } from '../valid-name-validator';
import { ValidationUtil } from '../validation-util';
import { ValidationErrorSeverity } from '../validation-error-severity';

/** 
 * Detects nodes in a model that have a character in the variable name that is not allowed for
 * element names. As from the variable name Specmate contstruct Testparameters with the same name, this 
 * would cause problems.
 */
@Validator(CEGModel)
export class InvalidNodeVariableValidator extends ElementValidatorBase<CEGNode> {

    public validate(element: CEGNode, contents: IContainer[]): ValidationResult {

        let nodes: CEGNode[] =
            contents.filter((element: IContainer) => Type.is(element, CEGNode)).map((element: IContainer) => element as CEGNode);
        let invalidNodes: CEGNode[] = [];
        for(let node of nodes){
            if(!this.isValidNode(node)){
                invalidNodes.push(node);
            }
        }
        if(invalidNodes.length>0){
            let message = ValidationMessage.ERROR_INVALID_VARIABLE;
            return new ValidationResult(message, false, invalidNodes, ValidationErrorSeverity.SAVE_DISABLED);
       }
        
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
