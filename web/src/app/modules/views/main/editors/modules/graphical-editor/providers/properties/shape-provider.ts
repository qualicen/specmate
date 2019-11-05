import { CEGConnection } from 'src/app/model/CEGConnection';
import { Type } from 'src/app/util/type';
import { Config } from '../../../../../../../../config/config';
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { ProcessDecision } from '../../../../../../../../model/ProcessDecision';
import { ProcessEnd } from '../../../../../../../../model/ProcessEnd';
import { ProcessStart } from '../../../../../../../../model/ProcessStart';
import { ProcessStep } from '../../../../../../../../model/ProcessStep';
import { EditorStyle } from '../../components/editor-components/editor-style';
import { NodeNameConverterProvider } from '../conversion/node-name-converter-provider';
import { CEGmxModelNode } from './ceg-mx-model-node';
import { ProviderBase } from './provider-base';

export type ShapeData = {
    style: string,
    size: { width: number, height: number },
    text: string | CEGmxModelNode
};

export class ShapeProvider extends ProviderBase {

    private shapeMap: { [className: string]: ShapeData } = {};
    private styles: ((element: { className: string }) => ShapeData)[] = [];

    constructor(type: { className: string }) {
        super(type);

        this.shapeMap[CEGNode.className] = {
            style: EditorStyle.BASE_CEG_NODE_STYLE,
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
            style: EditorStyle.BASE_PROCESS_START_STYLE,
            size: {
                width: Config.PROCESS_START_END_NODE_RADIUS * 2,
                height: Config.PROCESS_START_END_NODE_RADIUS * 2
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: 'Start'
            })
        };

        this.shapeMap[ProcessEnd.className] = {
            style: EditorStyle.BASE_PROCESS_END_STYLE,
            size: {
                width: Config.PROCESS_START_END_NODE_RADIUS * 2,
                height: Config.PROCESS_START_END_NODE_RADIUS * 2
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: 'End'
            })
        };

        this.shapeMap[ProcessStep.className] = {
            style: EditorStyle.BASE_PROCESS_STEP_STYLE,
            size: {
                width: Config.CEG_NODE_WIDTH,
                height: Config.CEG_NODE_HEIGHT
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: Config.PROCESS_NEW_STEP_NAME
            })
        };

        this.shapeMap[ProcessDecision.className] = {
            style: EditorStyle.BASE_PROCESS_DECISION_STYLE,
            size: {
                width: Config.PROCESS_DECISION_NODE_DIM,
                height: Config.PROCESS_DECISION_NODE_DIM
            },
            text: new NodeNameConverterProvider(type).nodeNameConverter.convertTo({
                name: Config.PROCESS_NEW_DECISION_NAME
            })
        };

        this.styles.push((element: { className: string }) => this.shapeMap[element.className]);

        this.styles.push((element: { className: string }) => {
            if (Type.is(element, CEGConnection) && (element as CEGConnection).negate === true) {
                return {
                    size: undefined,
                    style: EditorStyle.ADDITIONAL_CEG_CONNECTION_NEGATED_STYLE,
                    text: undefined
                };
            }
        });
    }

    private getShapeData(element: { className: string }): ShapeData[] {
        return this.styles.map(fn => fn(element)).filter(shapeData => shapeData !== undefined);
    }

    public getStyle(element: { className: string }): string {
        return this.getShapeData(element).map(shapeData => shapeData.style).join(';');
    }

    public getInitialSize(element: { className: string }): { width: number, height: number } {
        return this.getShapeData(element).find(shapeData => shapeData.size !== undefined).size;
    }

    public getInitialText(element: { className: string }): string | CEGmxModelNode {
        return this.getShapeData(element).find(shapeData => shapeData.text !== undefined).text;
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
