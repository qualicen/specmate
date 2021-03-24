import { IContainer } from '../model/IContainer';
import { SpecmateDataService } from '../modules/data/modules/data-service/services/specmate-data.service';
import { ElementValidatorBase } from './element-validator-base';
import { ValidationErrorSeverity } from './validation-error-severity';
import { ValidationMessage } from './validation-message';
import { ValidationResult } from './validation-result';
import { ValidationUtil } from './validation-util';

export class ValidNameValidator extends ElementValidatorBase<IContainer> {

    public async validate(element: IContainer, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        if (element === undefined || element.name === undefined || element.name === null) {
            return ValidationResult.VALID;
        }
        if (!ValidationUtil.isValidName(element.name)) {
            let message = ValidationMessage.ERROR_INVALID_NAME;
            return new ValidationResult(message, false, [element], ValidationErrorSeverity.SAVE_DISABLED);
        }
        return ValidationResult.VALID;
    }
}
