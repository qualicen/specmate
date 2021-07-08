import { Component } from '@angular/core';
import { CEGModel } from 'src/app/model/CEGModel';
import { IModelNode } from 'src/app/model/IModelNode';
import { Process } from 'src/app/model/Process';
import { Config } from '../../../../../../config/config';
import { IContainer } from '../../../../../../model/IContainer';
import { Requirement } from '../../../../../../model/Requirement';
import { TestSpecification } from '../../../../../../model/TestSpecification';
import { Strings } from '../../../../../../util/strings';
import { AdditionalInformationService } from '../services/additional-information.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'links-actions',
    templateUrl: 'links-actions.component.html',
    styleUrls: ['links-actions.component.css']
})
export class LinksActions {

    public isCollapsed = false;
    public descriptionVisible: boolean;

    constructor(private additionalInformationService: AdditionalInformationService) { }

    public get maxDescriptionLength(): number {
        return Config.TESTSPEC_DESCRIPTION_TRUNC_LENGTH;
    }

    public get element(): IModelNode {
        return this.additionalInformationService.element as IModelNode;
    }

    public get cegModel(): CEGModel {
        return this.element as any as CEGModel;
    }

    public get model(): IContainer {
        return this.additionalInformationService.model;
    }

    public get requirement(): Requirement {
        return this.additionalInformationService.requirement;
    }

    public get testSpecification(): TestSpecification {
        return this.additionalInformationService.testSpecification;
    }

    public get testSpecifications(): TestSpecification[] {
        return this.additionalInformationService.testSpecifications;
    }

    public get canHaveTestSpecifications(): boolean {
        return this.additionalInformationService.canHaveTestSpecifications;
    }

    public get canGenerateTestSpecifications(): boolean {
        return this.additionalInformationService.canGenerateTestSpecifications;
    }

    public get canAddTestSpecifications(): boolean {
        return this.additionalInformationService.canAddTestSpecifications;
    }

    public get exports(): string[] {
        return this.additionalInformationService.exports;
    }

    public get canGenerateCEGModel(): boolean {
        return this.additionalInformationService.canGenerateCEGModel;
    }

    public toggleDescription() {
        this.descriptionVisible = !this.descriptionVisible;
    }

    public get descriptionCollapsibleLabel(): string {
        if (this.shouldTruncate()) {
            return 'angle-double-right';
        }

        return 'angle-double-left';
    }

    public get requirementDescription(): string {
        if (this.requirement === undefined || this.requirement.description === undefined) {
            return '';
        }
        if (this.shouldTruncate()) {
            return Strings.truncate(this.requirement.description, this.maxDescriptionLength);
        }

        return this.requirement.description;
    }

    private shouldTruncate(): boolean {
        if (this.requirement === undefined || this.requirement.description === undefined) {
            return false;
        }
        return this.requirement.description.length > this.maxDescriptionLength && !this.descriptionVisible;
    }

    public get canTruncate(): boolean {
        return Strings.truncate(this.requirement.description, this.maxDescriptionLength) !== this.requirement.description;
    }
}
