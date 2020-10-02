import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { CEGModel } from '../../model/CEGModel';
import { IContainer } from '../../model/IContainer';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';

type IO = ('input' | 'output');

@Validator(CEGModel)
export class EmptyLinkedNodeValidator extends ElementValidatorBase<CEGModel> {
    public validate(element: CEGModel, contents: IContainer[]): ValidationResult {

        const invalidNodes: CEGLinkedNode[] = [];

        for (let i = 0; i < contents.length; i++) {
            const currentElement = contents[i];
            if (Type.is(currentElement, CEGLinkedNode)) {
                const currentNode = currentElement as CEGLinkedNode;
                if (currentNode.linkTo === undefined) {
                    invalidNodes.push(currentNode);
                }
            }
        }
        if (invalidNodes.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_EMPTY_CEG_LINKED_NODE, false, invalidNodes);
        }
        return ValidationResult.VALID;
    }
}
