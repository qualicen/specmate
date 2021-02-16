import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { IContainer } from '../../model/IContainer';
import { Process } from '../../model/Process';
import { ProcessDecision } from '../../model/ProcessDecision';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';

@Validator(Process)
export class NodeSingleOutgoingConnectionValidator extends ElementValidatorBase<Process> {
    public async validate(element: Process, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        const invalidNodes: IContainer[] = contents
            .filter((element: IContainer) => !Type.is(element, ProcessDecision))
            .filter((element: IContainer) =>
                (<any>element).outgoingConnections && (<any>element).outgoingConnections.length > 1);
        if (invalidNodes.length > 0) {
            return new ValidationResult(ValidationMessage.ERROR_PROCESS_NODE_MULTIPLE_OUTGOING_CONNECTIONS, false, invalidNodes);
        }
        return ValidationResult.VALID;
    }
}
