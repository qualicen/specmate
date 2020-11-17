export class Search {
    public static processSearchQuery(query: string): string {        // Escape all lucene query special characters.
        return '*' + query.replace(/[+\-\&\(\)\[\]\{\}\|!^~"*]/g, '\\$&') + '*';
    }
}
