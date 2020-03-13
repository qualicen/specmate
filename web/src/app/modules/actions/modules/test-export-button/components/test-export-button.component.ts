import { LowerCasePipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { saveAs } from 'file-saver';
import { TestSpecification } from '../../../../../model/TestSpecification';
import { Export } from '../../../../../model/Export';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { IContainer } from '../../../../../model/IContainer';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { ValidationService } from 'src/app/modules/forms/modules/validation/services/validation.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'test-export-button',
    templateUrl: 'test-export-button.component.html',
    styleUrls: ['test-export-button.component.css']
})

export class TestExportButton {

    private _element: IContainer;
    private _contents: IContainer[];

    private _lang: string;

    private static UTF8_BOM = '\ufeff';

    @Input()
    public set element(element: IContainer) {
        if (!element) {
            return;
        }
        this._element = element;
    }

    @Input()
    public set language(lang: string) {
        this._lang = lang;
    }

    constructor(private dataService: SpecmateDataService,
        private translate: TranslateService,
        private modal: ConfirmationModal,
        private validation: ValidationService) { }

    ngOnInit(): void {
        this.dataService.readContents(this._element.url).then((contents: IContainer[]) => this._contents = contents);
    }

    public async getskeleton(): Promise<void> {
        if (!this.enabled) {
            return;
        }

        const data: Export = await this.dataService.performQuery(this._element.url, 'export',
            {
                language: new LowerCasePipe().transform(this._lang)
            });

        if (data === null || data === undefined) {
            // no failure but no content --> export to background system
            this.modal.openOk(this.translate.instant('successful'), this.translate.instant('exportSuccessful'));

        } else {
            // there is content --> save it (not modal needed in this case)
            saveAs(new Blob([TestExportButton.UTF8_BOM + data.content], { type: 'text/plain;charset=utf-8' }), data.name);
        }
    }

    public get language(): string {
        return this._lang;
    }

    public get enabled(): boolean {
        if (this._element === undefined) {
            return false;
        }

        return this.isValid();
    }


    private isValid(): boolean {
        return this.validation.isValid(this._element) && this.validation.allValid(this._contents);
    }

}
