import { SpecmateType } from 'src/app/util/specmate-type';
import { IContainer } from '../../model/IContainer';
import { IModelNode } from '../../model/IModelNode';
import { Process } from '../../model/Process';
import { ProcessEnd } from '../../model/ProcessEnd';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';

@Validator(Process)
export class NodeNoOutgoingValidator extends ElementValidatorBase<Process> {
    public validate(element: Process, contents: IContainer[]): ValidationResult {
        let processNodes: IModelNode[] =
            contents.filter((element: IContainer) => SpecmateType.isNodeOfProcess(element)) as IModelNode[];
        let nodesWithoutOutgoing: IContainer[] =
            processNodes.filter((element: IModelNode) =>
                (!element.outgoingConnections ||
                    (element.outgoingConnections && element.outgoingConnections.length === 0)) &&
                !Type.is(element, ProcessEnd));
        if (nodesWithoutOutgoing.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_NODE_WITHOUT_OUTGOING, false, nodesWithoutOutgoing);
        }
        return ValidationResult.VALID;
    }
}
