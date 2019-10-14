import { Injectable } from '@angular/core';
import '../../../../../../../../assets/robot/keywords.json';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class RobotKeywordService {

    constructor(private http: HttpClient) {
        const filePath = '../../../../../../../../assets/robot/keywords.json';
        this.http.get(filePath).subscribe(data => this._keywords = data as Keyword[]);
    }

    private _keywords: Keyword[] = [];

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
