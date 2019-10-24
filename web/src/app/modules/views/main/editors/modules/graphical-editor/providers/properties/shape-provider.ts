import { Config } from '../../../../../../../../config/config';
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { IContainer } from '../../../../../../../../model/IContainer';
import { ProcessDecision } from '../../../../../../../../model/ProcessDecision';
import { ProcessEnd } from '../../../../../../../../model/ProcessEnd';
import { ProcessStart } from '../../../../../../../../model/ProcessStart';
import { ProcessStep } from '../../../../../../../../model/ProcessStep';
import { NodeNameConverterProvider } from '../conversion/node-name-converter-provider';
import { ProviderBase } from './provider-base';
import { ValuePair } from './value-pair';

export type ShapeData = {
    style: string,
    size: { width: number, height: number },
    text: string|ValuePair
};

export class ShapeProvider extends ProviderBase {

    private shapeMap: { [className: string]: ShapeData } = {};

    constructor(type: { className: string }) {
        super(type);

        this.shapeMap[CEGNode.className] = {
            style: 'shape=rectangle;rounded=1;arcSize=10;align=center;perimeter=rectanglePerimeter;',
            size: {
                width: Config.CEG_NODE_WIDTH,
                height: Config.CEG_NODE_HEIGHT
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                variable: Config.CEG_NODE_NEW_VARIABLE,
                condition: Config.CEG_NODE_NEW_CONDITION
            })
        };

        this.shapeMap[ProcessStart.className] = {
            // tslint:disable-next-line:max-line-length
            style: 'shape=ellipse;whiteSpace=wrap;html=1;aspect=fixed;align=center;perimeter=ellipsePerimeter;editable=0;',
            size: {
                width: Config.PROCESS_START_END_NODE_RADIUS * 2,
                height: Config.PROCESS_START_END_NODE_RADIUS * 2
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: 'Start'
            })
        };

        this.shapeMap[ProcessEnd.className] = {
            // tslint:disable-next-line:max-line-length
            style: 'shape=doubleEllipse;whiteSpace=wrap;html=1;aspect=fixed;align=center;perimeter=ellipsePerimeter;editable=0;',
            size: {
                width: Config.PROCESS_START_END_NODE_RADIUS * 2,
                height: Config.PROCESS_START_END_NODE_RADIUS * 2
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: 'End'
            })
        };

        this.shapeMap[ProcessStep.className] = {
            style: 'shape=rectangle;rounded=1;arcSize=10;perimeter=rectanglePerimeter;',
            size: {
                width: Config.CEG_NODE_WIDTH,
                height: Config.CEG_NODE_HEIGHT
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: Config.PROCESS_NEW_STEP_NAME
            })
        };

        this.shapeMap[ProcessDecision.className] = {
            style: 'shape=rhombus;align=center;perimeter=rhombusPerimeter;',
            size: {
                width: Config.PROCESS_DECISION_NODE_DIM,
                height: Config.PROCESS_DECISION_NODE_DIM
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: Config.PROCESS_NEW_DECISION_NAME
            })
        };
    }

    public getStyle(element: { className: string }): string {
        return this.shapeMap[element.className].style;
    }

    public getInitialSize(element: { className: string }): { width: number, height: number } {
        return this.shapeMap[element.className].size;
    }

    public getInitialText(element: { className: string }): string|ValuePair {
        return this.shapeMap[element.className].text;
    }

    public getInitialData(style: string): ShapeData {
        for (const className in this.shapeMap) {
            if (this.shapeMap[className].style === style) {
                return this.shapeMap[className];
            }
        }
        return undefined;
    }
}
