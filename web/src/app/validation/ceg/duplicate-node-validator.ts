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
import { Url } from 'src/app/util/url';

@Validator(CEGModel)
export class DuplicateNodeValidator extends ElementValidatorBase<CEGModel> {
    public async validate(element: CEGModel, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        const nodes = contents
            .filter((e: IContainer) => Type.is(e, CEGNode)).map(e => e as CEGNode);

        const linkingNodesMap: { [linkToUrl: string]: CEGLinkedNode } = {};

        const linkingNodes = contents.filter((e: IContainer) => Type.is(e, CEGLinkedNode));
        const linkedNodes = await Promise.all(
            linkingNodes
                .map(e => e as CEGLinkedNode)
                .filter(n => n.linkTo !== undefined)
                .map(async linkingNode => {
                    const linkedNode = (await dataService.readElement(linkingNode.linkTo.url, true)) as CEGNode;
                    linkingNodesMap[linkingNode.linkTo.url] = linkingNode;
                    return linkedNode;
                })
        );

        const allNodes = nodes.concat(linkedNodes);

        const duplicates: Set<IContainer> = new Set();
        for (let i = 0; i < allNodes.length; i++) {
            let currentNode: CEGNode = allNodes[i];
            let currentDuplicates: CEGNode[] =
                allNodes.filter((otherNode: CEGNode) =>
                    ValidationUtil.compareStrTrimmed(otherNode.variable, currentNode.variable) &&
                    ValidationUtil.compareStrTrimmed(otherNode.condition, currentNode.condition) &&
                    otherNode !== currentNode &&
                    !duplicates.has(otherNode));
            
            // If the duplicate is not in the current model (is a linking node), we return the acually linked node.
            currentDuplicates.map(duplicate => {
                if (!Url.isParent(element.url, duplicate.url)) {
                    return linkingNodesMap[duplicate.url];
                }
                return duplicate;
            }).forEach(node => duplicates.add(node));
        }
        const dupList = Array.from(duplicates.keys());
        if (dupList.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_DUPLICATE_NODE, false, dupList);
        }
        return ValidationResult.VALID;
    }
}
