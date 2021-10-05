import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { saveAs } from 'file-saver';

@Component({
    moduleId: module.id.toString(),
    selector: 'documentation-export-button',
    templateUrl: 'documentation-export-button.component.html',
    styleUrls: ['documentation-export-button.component.css']
})
export class DocumentationExportButton {
    constructor(
        private translate: TranslateService) {
    }

    public download(): void {
        let filename = 'Specmate Documentation - ' + this.translate.currentLang + '.pdf';
        let filepath = '../../../../../../assets/documentation/' + filename;
        saveAs(filepath, filename);
    }
}
