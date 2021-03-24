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
export class EmptyModelValidator extends ElementValidatorBase<CEGModel> {
    public async validate(element: CEGModel, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        const valid: boolean = contents.some((element: IContainer) => Type.is(element, CEGNode) || Type.is(element, CEGLinkedNode));
        if (valid) {
            return ValidationResult.VALID;
        }
        return new ValidationResult(ValidationMessage.ERROR_EMPTY_MODEL, false, [element]);
    }
}
