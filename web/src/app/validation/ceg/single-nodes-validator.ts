import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { CEGModel } from '../../model/CEGModel';
import { CEGNode } from '../../model/CEGNode';
import { IContainer } from '../../model/IContainer';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';

@Validator(CEGModel)
export class SingleNodesValidator extends ElementValidatorBase<CEGModel> {

    public async validate(element: CEGModel, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        let invalidNodes: IContainer[] = contents.filter((element: IContainer) => {
            if (!Type.is(element, CEGNode) && !Type.is(element, CEGLinkedNode)) {
                return false;
            }
            let node: CEGNode = element as CEGNode;
            let hasIncomingConnections: boolean = node.incomingConnections && node.incomingConnections.length > 0;
            let hasOutgoingConnections: boolean = node.outgoingConnections && node.outgoingConnections.length > 0;
            return !hasIncomingConnections && !hasOutgoingConnections;
        });

        if (invalidNodes.length === 0) {
            return ValidationResult.VALID;
        }
        return new ValidationResult(ValidationMessage.ERROR_UNCONNECTED_NODE, false, invalidNodes);
    }
}
