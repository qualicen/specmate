import { Type } from 'src/app/util/type';
import { CEGModel } from 'src/app/model/CEGModel';
import { IContainer } from 'src/app/model/IContainer';
import { Process } from 'src/app/model/Process';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { ModelImage } from 'src/app/model/ModelImage';
import { ModelImageFactory } from 'src/app/factory/model-image-factory';
import { Id } from 'src/app/util/id';
import * as saveAsPng from 'save-svg-as-png';
import { Injectable } from '@angular/core';

@Injectable()
export class ModelImageService {
    constructor(private dataService: SpecmateDataService) {
    }

    // Create a png out of the dom, which draws the graph
    public async createModelImage(element: IContainer) {
        if (Type.is(element, CEGModel) || Type.is(element, Process)) {
            let model = element as CEGModel | Process;
            let svg: SVGSVGElement = document.getElementById('mxGraphContainer').getElementsByTagName('svg')[0];
            let minWidth = svg.style.minWidth;
            let minHeight = svg.style.minHeight;
            svg.style.width = '0px';
            svg.style.height = '0px';

            // Scales the model to 200 x 200 px (or smaller), because the space in the db is limited to 32.000 characters
            let maxWidth = parseInt(minWidth.substring(0, minWidth.length - 2));
            let maxHeight = parseInt(minHeight.substring(0, minHeight.length - 2));
            let factor = 0.0 + 200 / maxWidth / 1.25;
            maxHeight = Math.min(maxWidth, maxHeight);
            let pngAsBase64: string = await saveAsPng.svgAsPngUri(svg, { 'scale': factor, 'height': maxHeight, 'encoderOptions': 1.0 });
            while (pngAsBase64.length > 32000) {
                factor = factor / 2;
                pngAsBase64 = await saveAsPng.svgAsPngUri(svg, { 'scale': factor, 'height': maxHeight, 'encoderOptions': 1.0 });
            }

            // Load the existing modelImage or create a new one
            let modelImage: ModelImage;
            modelImage = await this.dataService.performOperations(model.url, 'listModelImage', undefined, true);
            if (modelImage === null || modelImage === undefined) {
                modelImage = await new ModelImageFactory(this.dataService).create(model, false);
            }
            this.dataService.readElementComplete(modelImage);
            modelImage.imageData = pngAsBase64;
            await this.dataService.updateElement(modelImage, true, Id.uuid);
        }
    }

}
