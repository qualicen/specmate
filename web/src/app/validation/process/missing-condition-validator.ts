import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { SpecmateType } from 'src/app/util/specmate-type';
import { IContainer } from '../../model/IContainer';
import { IModelNode } from '../../model/IModelNode';
import { Process } from '../../model/Process';
import { ProcessConnection } from '../../model/ProcessConnection';
import { ProcessDecision } from '../../model/ProcessDecision';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';

@Validator(Process)
export class MissingConditionValidator extends ElementValidatorBase<Process> {
    public async validate(element: Process, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        let processNodes: IModelNode[] =
            contents.filter((element: IContainer) =>
            SpecmateType.isNodeOfProcess(element)) as IModelNode[];

        let processConnections: ProcessConnection[] =
            contents.filter((element: IContainer) => Type.is(element, ProcessConnection)) as ProcessConnection[];
        let decisionNodes: ProcessDecision[] =
            processNodes.filter((element: IModelNode) => Type.is(element, ProcessDecision)) as ProcessDecision[];
        let decisionConnections: ProcessConnection[] =
            processConnections.filter((connection: ProcessConnection) =>
                decisionNodes.find((node: ProcessDecision) => node.url === connection.source.url) !== undefined);

        let invalidElements: IContainer[] =
            decisionConnections.filter((connection: ProcessConnection) =>
                connection.condition === undefined || connection.condition === null || connection.condition === '');

        if (invalidElements.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_MISSING_CONDITION, false, invalidElements);
        }
        return ValidationResult.VALID;
    }
}
