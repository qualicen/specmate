import { Component } from '@angular/core';
import { IContainer } from '../../../../../../model/IContainer';
import { ValidationService } from '../../../../../forms/modules/validation/services/validation.service';
import { AdditionalInformationService } from '../../links-actions/services/additional-information.service';
import { Type } from '../../../../../../util/type';
import { CEGConnection } from '../../../../../../model/CEGConnection';
import { ProcessConnection } from '../../../../../../model/ProcessConnection';
import { CEGNode } from '../../../../../../model/CEGNode';
import { ProcessStart } from '../../../../../../model/ProcessStart';
import { ProcessEnd } from '../../../../../../model/ProcessEnd';

@Component({
    moduleId: module.id.toString(),
    selector: 'errors-warnings',
    templateUrl: 'errors-warnings.component.html',
    styleUrls: ['errors-warnings.component.css']
})
export class ErrorsWarings {

    private _isCollapsed = false;
    public set isCollapsed(collapsed: boolean) {
        this._isCollapsed = collapsed;
    }

    public get isCollapsed(): boolean {
        if (!this.validationResults) {
            return true;
        }
        return this._isCollapsed;
    }

    public get model(): IContainer {
        return this.additionalInformationService.element;
    }

    public visible = true;
    constructor(private validationService: ValidationService,
        private additionalInformationService: AdditionalInformationService) { }

    public get validationResults(): { name: string, message: string }[] {
        const validationResults = this.validationService
            .getValidationResults(this.additionalInformationService.element);


        return validationResults.map(result => {
            return {
                name: this.getName(result.element),
                message: result.message
            };
        });
    }

    private getName(element: IContainer): string {
        if (Type.is(element, CEGNode)) {
            return (element as CEGNode).variable + ' ' + (element as CEGNode).condition;
        }
        if (Type.is(element, CEGConnection)) {
            return CEGConnection.className;
        }
        if (Type.is(element, ProcessStart)) {
            return ProcessStart.className;
        }
        if (Type.is(element, ProcessEnd)) {
            return ProcessEnd.className;
        }
        if (Type.is(element, ProcessConnection)) {
            return (element as ProcessConnection).condition;
        }
        return element.name;
    }

    // Errors and Warnings shall not be shown in folder view
    public get showErrors(): boolean {
        if (this.model !== undefined
            && this.model.className === 'Folder') {
            return false;
        }
        return true;
    }

}
