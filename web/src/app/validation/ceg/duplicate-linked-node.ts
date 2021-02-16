import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
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
    public async validate(element: CEGModel, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {

        const invalidNodes: CEGLinkedNode[] = [];
        let linkedNodes: CEGLinkedNode[] = contents
            .filter(element => Type.is(element, CEGLinkedNode))
            .map(element => element as CEGLinkedNode);
        for (const node of linkedNodes) {
            if (node.linkTo !== undefined) {
                if (linkedNodes.filter(element => element.linkTo !== undefined
                    && element !== node
                    && element.linkTo.url === node.linkTo.url).length > 0) {
                    invalidNodes.push(node);
                }
            }
        }
        if (invalidNodes.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_DUPLICATE_LINKED_NODE, false, invalidNodes);
        }
        return ValidationResult.VALID;
    }
}
