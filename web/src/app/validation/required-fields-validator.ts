import { IContainer } from '../model/IContainer';
import { ElementValidatorBase } from './element-validator-base';
import { ValidationMessage } from './validation-message';
import { ValidationResult } from './validation-result';
import { ValidationErrorSeverity } from './validation-error-severity';
import { SpecmateDataService } from '../modules/data/modules/data-service/services/specmate-data.service';

export class RequiredFieldsValidator extends ElementValidatorBase<IContainer> {

    constructor(private fields: string[]) {
        super();
    }

    public async validate(element: IContainer, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        const missingFields: string[] = this.fields.filter((field: string) => !element[field] || element[field].length === 0);

        if (missingFields.length > 0) {
            const fieldText = '[' + missingFields.join(', ') + ']';
            const messageKey = ValidationMessage.ERROR_MISSING_FIELDS;
            let message = new ValidationMessage(messageKey, {fields: fieldText});
            if (missingFields.find(field => field === 'name')) {
                return new ValidationResult(message, false, [element],  ValidationErrorSeverity.SAVE_DISABLED);
            }
            return new ValidationResult(message, false, [element]);
        }
        return ValidationResult.VALID;
    }
}
