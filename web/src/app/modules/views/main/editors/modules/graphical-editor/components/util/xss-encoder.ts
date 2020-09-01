
export function xssEncode(text: string): string {
    let doc = (new DOMParser().parseFromString(text, 'text/html'));
    return doc.documentElement.textContent;
}
