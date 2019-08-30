import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { ProcessEndFactory } from '../../../../../../../../factory/process-end-factory';
import { IContainer } from '../../../../../../../../model/IContainer';
import { Process } from '../../../../../../../../model/Process';
import { ProcessEnd } from '../../../../../../../../model/ProcessEnd';
import { SpecmateDataService } from '../../../../../../../data/modules/data-service/services/specmate-data.service';
import { SelectedElementService } from '../../../../../../side/modules/selected-element/services/selected-element.service';
import { CreateNodeToolBase } from '../create-node-tool-base';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';

export class EndTool extends CreateNodeToolBase<ProcessEnd> {

    protected modelType: { className: string; } = Process;

    public name = 'tools.addEnd';
    public icon = 'plus';
    public style = new ShapeProvider(Process).getStyle(ProcessEnd);

    constructor(parent: IContainer,
        dataService: SpecmateDataService,
        selectedElementService: SelectedElementService) {
        super(dataService, selectedElementService, parent);
    }

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<ProcessEnd> {
        return new ProcessEndFactory(coords, this.dataService);
    }
}
