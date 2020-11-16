import { Component, Input } from '@angular/core';
import { SpecmateType } from 'src/app/util/specmateType';
import { NavigatorService } from '../../../../navigation/modules/navigator/services/navigator.service';
import { ValidationService } from '../services/validation.service';

@Component({
  moduleId: module.id.toString(),
  selector: 'validate-button',
  templateUrl: 'validate-button.component.html',
  styleUrls: ['validate-button.component.css']
})

export class ValidateButton {

  @Input()
  public textEnabled = true;
  private commitCounter = 0;

  constructor(
    private validationService: ValidationService,
    private dataService: SpecmateDataService,
    private navigator: NavigatorService) {
  }

  public validate(): Promise<void> {
    this.commitCounter = this.dataService.countCommits;
    return this.validationService.validateCurrent();
  }

  public cancelEvent(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }


  public get isValidationButtonVisible(): boolean {
        return this.navigator.currentElement && (SpecmateType.isModel(this.navigator.currentElement));
  }

  public get busy(): boolean {
    return this.validationService.isValidating;
  }
}
