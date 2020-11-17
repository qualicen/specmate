import { CEGConnection } from '../model/CEGConnection';
import { CEGModel } from '../model/CEGModel';
import { CEGNode } from '../model/CEGNode';
import { IContainer } from '../model/IContainer';
import { Process } from '../model/Process';
import { ProcessConnection } from '../model/ProcessConnection';
import { ProcessDecision } from '../model/ProcessDecision';
import { ProcessEnd } from '../model/ProcessEnd';
import { ProcessStart } from '../model/ProcessStart';
import { ProcessStep } from '../model/ProcessStep';
import { Type } from './type';

export class SpecmateType {

    /**
     * Element is a model: CEGModel, Process
     */
    public static isModel(element: IContainer): boolean {
        return Type.is(element, CEGModel)
            || Type.is(element, Process);
    }

    /**
     * Element is a node: CEGNode, ProcessStart, ProcessEnd, ProcessStep, ProcessDecision
     */
    public static isNode(element: IContainer): boolean {
        return this.isNodeOfCEG(element)
            || this.isNodeOfProcess(element);
    }
    /**
     * Element is a node of a processmodel:  ProcessStart, ProcessEnd, ProcessStep, ProcessDecision
     */
    public static isNodeOfProcess(element: IContainer): boolean {
        return Type.is(element, ProcessStart)
            || Type.is(element, ProcessEnd)
            || Type.is(element, ProcessStep)
            || Type.is(element, ProcessDecision);
    }

    /**
     * Element is a node of a cegmodel: CEGNode
     */
    public static isNodeOfCEG(element: IContainer): boolean {
        return Type.is(element, CEGNode);
    }

    /**
     * Element is a connection: CEGConnection, ProcessConnection
     */
    public static isConnection(element: IContainer): boolean {
        return Type.is(element, CEGConnection)
            || Type.is(element, ProcessConnection);
    }


    /**
     * Element is a graphical element in a model:
     * CEGNode, ProcessStart, ProcessEnd, ProcessStep, ProcessDecision, CEGConnection, ProcessConnection
     */
    public static isModelElement(element: IContainer): boolean {
        return this.isNode(element)
            || this.isConnection(element);
    }
}
