import { ElementValidatorBase } from './element-validator-base';
import { IContainer } from '../model/IContainer';
import { ValidationResult } from './validation-result';
import { ValidationErrorSeverity } from './validation-error-severity';
import { ValidationMessage } from './validation-message';
import { Type } from '../util/type';
import { ModelImage } from '../model/ModelImage';
import { SpecmateDataService } from '../modules/data/modules/data-service/services/specmate-data.service';

export class TextLengthValidator extends ElementValidatorBase<IContainer> {
    public async validate(element: IContainer, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        const keys = Object.keys(element);
        for (let i = 0; i < keys.length; i++) {
            const key = keys[i];
            const currentAttribute = element[key];
            if (typeof currentAttribute === 'string' && !Type.is(element, ModelImage)) {
                if (currentAttribute.length >= 4000) {
                    return new ValidationResult(ValidationMessage.ERROR_TEXT_TOO_LONG,
                        false, [element], ValidationErrorSeverity.SAVE_DISABLED);
                }
            }
        }
        return ValidationResult.VALID;
    }
}
