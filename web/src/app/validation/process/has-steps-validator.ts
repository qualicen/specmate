import { SpecmateType } from 'src/app/util/specmateType';
import { IContainer } from '../../model/IContainer';
import { IModelNode } from '../../model/IModelNode';
import { Process } from '../../model/Process';
import { ProcessStep } from '../../model/ProcessStep';
import { Type } from '../../util/type';
import { ElementValidatorBase } from '../element-validator-base';
import { ValidationMessage } from '../validation-message';
import { ValidationResult } from '../validation-result';
import { Validator } from '../validator-decorator';

@Validator(Process)
export class HasStepsValidator extends ElementValidatorBase<Process> {
    public validate(element: Process, contents: IContainer[]): ValidationResult {
        let processNodes: IModelNode[] =
            contents.filter((element: IContainer) =>
                SpecmateType.isNodeOfProcess(element)) as IModelNode[];
        let processSteps: IModelNode[] = processNodes.filter((element: IModelNode) => Type.is(element, ProcessStep));
        if (processSteps.length === 0) {
            return new ValidationResult(ValidationMessage.ERROR_NO_STEPS, false, [element]);
        }
        return ValidationResult.VALID;
    }
}
