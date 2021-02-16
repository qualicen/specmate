import { Validator } from '../validator-decorator';
import { Process } from 'src/app/model/Process';
import { ElementValidatorBase } from '../element-validator-base';
import { IContainer } from 'src/app/model/IContainer';
import { ValidationResult } from '../validation-result';
import { IModelNode } from 'src/app/model/IModelNode';
import { Type } from '../../util/type';
import { ProcessDecision } from 'src/app/model/ProcessDecision';
import { ProcessConnection } from 'src/app/model/ProcessConnection';
import { ValidationMessage } from '../validation-message';
import { ValidationUtil } from '../validation-util';
import { ValidationErrorSeverity } from '../validation-error-severity';
import { SpecmateType } from 'src/app/util/specmate-type';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';

@Validator(Process)
export class InvalidConditionTextValidator extends ElementValidatorBase<Process> {
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
            !ValidationUtil.isValidName(connection.condition));

        if (invalidElements.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_PROCESS_INVALID_CONDITION_TEXT,
              false, invalidElements, ValidationErrorSeverity.SAVE_DISABLED);
        }
        return ValidationResult.VALID;
    }
}
