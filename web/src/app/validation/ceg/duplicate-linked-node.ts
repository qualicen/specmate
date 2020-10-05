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
export class DublicateLinkedNodeValidator extends ElementValidatorBase<CEGModel> {
    public validate(element: CEGModel, contents: IContainer[]): ValidationResult {

        const invalidNodes: CEGLinkedNode[] = [];
        let linkedNodes: CEGLinkedNode[] = contents
            .filter(element => Type.is(element, CEGLinkedNode))
            .map(element => element as CEGLinkedNode);
        for (const node of linkedNodes) {
            if (linkedNodes.filter(element => element.linkTo.url === node.linkTo.url).length > 1) {
                invalidNodes.push(node);
            }
        }
        if (invalidNodes.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_DUPLICATE_LINKED_NODE, false, invalidNodes);
        }
        return ValidationResult.VALID;
    }
}
