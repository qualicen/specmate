import { CEGModel } from '../../model/CEGModel';
import { CEGNode } from '../../model/CEGNode';
import { IContainer } from '../../model/IContainer';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';
import { ValidationUtil } from '../validation-util';
import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';

@Validator(CEGModel)
export class DuplicateNodeValidator extends ElementValidatorBase<CEGModel> {
    public async validate(element: CEGModel, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        const nodes = contents
            .filter((e: IContainer) => Type.is(e, CEGNode)).map(e => e as CEGNode);
        const linkingNodes = contents
            .filter((e: IContainer) => Type.is(e, CEGLinkedNode)).map(e => e as CEGLinkedNode);
        const linkedNodes = await Promise.all(
            linkingNodes
                .map(n => n.linkTo.url)
                .map(async url => await dataService.readElement(url, true))
        );
        const duplicates: Set<CEGNode> = new Set();
        for (let i = 0; i < nodes.length; i++) {
            let currentNode: CEGNode = nodes[i];
            let currentDuplicates: CEGNode[] =
                nodes.filter((otherNode: CEGNode) =>
                    ValidationUtil.compareStrTrimmed(otherNode.variable, currentNode.variable) &&
                    ValidationUtil.compareStrTrimmed(otherNode.condition, currentNode.condition) &&
                    otherNode !== currentNode &&
                    !duplicates.has(otherNode));
            currentDuplicates.forEach(node => duplicates.add(node));
        }
        const dupList = Array.from(duplicates.keys());
        if (dupList.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_DUPLICATE_NODE, false, dupList);
        }
        return ValidationResult.VALID;
    }
}
