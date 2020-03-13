import { CEGModel } from '../../model/CEGModel';
import { CEGNode } from '../../model/CEGNode';
import { IContainer } from '../../model/IContainer';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';

type IO = ('input' | 'output');

@Validator(CEGModel)
export class DuplicateIOVariableValidator extends ElementValidatorBase<CEGModel> {
    public validate(element: CEGModel, contents: IContainer[]): ValidationResult {

        const nodeMap: { [variable: string]: IContainer[] } = {};
        const typeMap: { [variable: string]: IO[] } = {};
        let invalidNodes: IContainer[] = [];

        for (let content of contents) {
            if (!Type.is(content, CEGNode)) {
                continue;
            }

            const node: CEGNode = content as CEGNode;

            let type: IO;
            if (!node.incomingConnections || node.incomingConnections.length <= 0) {
                type = 'input';
            } else if (!node.outgoingConnections || node.outgoingConnections.length <= 0) {
                type = 'output';
            }

            if (typeMap[node.variable.trim().toLowerCase()] === undefined) {
                typeMap[node.variable.trim().toLowerCase()] = [];
            }
            if (typeMap[node.variable.trim().toLowerCase()].indexOf(type) < 0) {
                typeMap[node.variable.trim().toLowerCase()].push(type);
            }

            if (nodeMap[node.variable.trim().toLowerCase()] === undefined) {
                nodeMap[node.variable.trim().toLowerCase()] = [];
            }
            nodeMap[node.variable.trim().toLowerCase()].push(node);
        }

        for (const variable in typeMap) {
            if (typeMap[variable].length > 1) {
                invalidNodes = invalidNodes.concat(nodeMap[variable]);
            }
        }

        if (invalidNodes.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_DUPLICATE_IO_VARIABLE, false, invalidNodes);
        }
        return ValidationResult.VALID;
    }
}
