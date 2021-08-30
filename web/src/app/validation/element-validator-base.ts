import { IContainer } from '../model/IContainer';
import { SpecmateDataService } from '../modules/data/modules/data-service/services/specmate-data.service';
import { ValidationResult } from './validation-result';

export class ElementValidatorBase<T extends IContainer> {
    public async validate(element: T, contents: IContainer[], dataService: SpecmateDataService): Promise<ValidationResult> {
        throw new Error('Method not implemented');
    }
}
