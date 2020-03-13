import { IContainer } from '../../../../../../../model/IContainer';
import { ConverterBase } from './converter-base';
import { Type } from 'src/app/util/type';
import { ProcessConnection } from 'src/app/model/ProcessConnection';


type ProcessNodeOrConnection = { name: string } | { condition: string };
export class ProcessNodeToNameConverter extends ConverterBase<ProcessNodeOrConnection, string> {
    public convertTo(item: IContainer): string {
        if (Type.is(item, ProcessConnection)) {
            return (item as ProcessConnection).condition;
        }
        return item.name;
    }

    public convertFrom(value: string, item?: ProcessNodeOrConnection): ProcessNodeOrConnection {
        if (item['condition'] !== undefined) {
            return { condition: value };
        }
        return { name: value };
    }
}
