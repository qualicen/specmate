import { Injectable } from '@angular/core';
import '../../../../../../../../assets/robot/keywords.json';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class RobotKeywordService {
    constructor(private http: HttpClient) {
        const filePath = '../../../../../../../../assets/robot/keywords.json';
        this.http.get(filePath).subscribe(data => this.updateKeywords(data as Keyword[]));
    }

    private _loaded = false;
    private _keywords: Keyword[] = [];
    private _maxParameterCount = 0;

    private updateKeywords(data: Keyword[]) {
        this._keywords = data;
        this._maxParameterCount = this._keywords.map(k => k.parameters.length)
                                                .reduce((a, b) => Math.max(a, b), 0);
        this._loaded = true;
    }

    public get maxParameterCount(): number {
        return this._maxParameterCount;
    }

    public get isLoaded(): boolean {
        return this._loaded;
    }

    public getKeywordCount() {
        return this._keywords.length;
    }

    public getKeyword(index: number) {
        return this._keywords[index];
    }

    public getKeywordNames(): string[] {
        return this._keywords.map(keyword => keyword.keyword);
    }
}

interface Keyword {
    keyword: string;
    parameters: string[];
}
