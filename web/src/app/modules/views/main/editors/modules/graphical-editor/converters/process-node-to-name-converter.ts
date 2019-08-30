import { IContainer } from '../../../../../../../model/IContainer';
import { ConverterBase } from './converter-base';

export class ProcessNodeToNameConverter extends ConverterBase<{ name: string }, string> {
    public convertTo(item: IContainer): string {
        return item.name;
    }

    public convertFrom(value: string): { name: string } {
        return { name: value };
    }
}
