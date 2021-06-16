import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SimpleModal } from 'src/app/modules/notification/modules/modals/services/simple-modal.service';
import { DataServiceError } from './data-service-error';
import { SpecmateDataService } from './specmate-data.service';

/**
 * Handles errors in the data-service and displays a modal in case of errors.
 */
@Injectable()
export class DataServiceErrorsService {
    constructor(private dataService: SpecmateDataService, private modal: SimpleModal, private translate: TranslateService) {
        this.dataService.error.subscribe((error: DataServiceError) => {
            this.modal.openOk(this.translate.instant(error.title), this.translate.instant(error.message));
            console.error(error.error);
        });
    }
}
